/*
 * Copyright © 2021 Travis Hoffman (travis@firkin.io)
 * Copyright © 2021 Firkin IO (https://firkin.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.firkin.kstreams.normalizer.demo.v1;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;

import io.firkin.kstreams.normalizer.errors.v1.avsc.ExampleOneError;
import io.firkin.kstreams.normalizer.errors.v1.avsc.ExampleOneErrorProcessor;
import io.firkin.kstreams.normalizer.errors.v1.avsc.ExampleTwoError;
import io.firkin.kstreams.normalizer.errors.v1.avsc.ExampleTwoErrorProcessor;
import org.apache.avro.Schema;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.firkin.kstreams.normalizer.demo.v1.DemoUtils.loadEnvProperties;

/**
 * A sample all-in-one application which demos a single, central KStreams-based Error Normalizer
 * which:
 *
 * 1. Spins up a Kafka Cluster (using testcontainers)
 * 2. Spins up threads which generate mock Error Records into their own error topics (like DLQs).
 * 3. Spins up a KStreams application which
 * 4. Runs indefinitely
 *
 * Once running, you consume the records from "normalized_error_topic" using a tool like kafka-cat.
 */
public class CentralErrorNormalizerDemo {

  private static final Logger log = LoggerFactory.getLogger(CentralErrorNormalizerDemo.class);

  // --- Demo Configuration Stuff -----------------------------------------------------------------

  private static String bootstrap;
  private static Admin kAdmin;
  private static SchemaRegistryClient srAdmin;
  private static Topology ksTopology;
  private static ScheduledThreadPoolExecutor tPool;

//  private static Collection<NewTopic> topics;

  // Keep these in sync with the regex for matching source topics.
  static final String[] demoTopics = {
      "error_one_topic",
      "error_two_topic",
      // "error_three_topic", "error_four_topic", etc.
      "normalized_error_topic"
  };

  public static void main(String[] args) {

    try {
      // Load demo app configuration properties file
      final Properties props = overlayDefaultProperties(args.length > 0? new Properties(): loadEnvProperties(args[0]));

      kAdmin = DemoUtils.kafkaAdminClient(props);

      // Create Topics Synchronously
      //String namespace = CentralErrorNormalizerDemo.class.getName();
      List<String> topicNames = Stream.of(demoTopics)
          //.map(s-> namespace+"."+s) // TODO Make this namespaced to isolate demos.
          .collect(Collectors.toList());
      initializeTopics(topicNames);

      srAdmin = DemoUtils.schemaRegistryClient(props);

      // Create Schemas Synchronously
      initializeSchemas();

      ksTopology = initializeTopology();

      tPool = new ScheduledThreadPoolExecutor(2);

      // Create Example{One,Two}Error Producers as threads.
      startErrorProducers();

      // Create ErrorNormalizer which consumes from
      startErrorConsumer();

      // TODO Could a thread that'll print out the resultant error records, maybe even do an A/B comparison?
      //spinupNormalizedErrorConsumer();

      // TODO (Optionally) Clean up "all" topics. How to clean up all KStreams internal topics?
      //deleteTopics();
      // TODO (Optionally) Clean up "all" schemas.
      //deleteSchemas();
      // TODO Implement a shutdown handler to cleanly exit the kStreams application.
      //

      // TODO Wait here for the exit handler?
    } catch (IOException e) {
      log.error("Could not initialize properties from the environment file");
      System.exit(1);
    } catch (SchemaRegistrationException e) {
      e.printStackTrace();
    }
  }

  // --- Demo Helper Methods Stuff ----------------------------------------------------------------

  private static void initializeSchemas() throws SchemaRegistrationException {
    // Parse and Register Schemas Synchronously
    Schema e1Schema = ExampleOneError.getClassSchema();
    int e1SchemaId = parseAndRegisterSchema(e1Schema.getName(), AvroSchema.TYPE, e1Schema.toString(false), List.of());
    Schema e2Schema = ExampleTwoError.getClassSchema();
    int e2SchemaId = parseAndRegisterSchema(e2Schema.getName(), AvroSchema.TYPE, e2Schema.toString(false), List.of());
    Schema nSchema = ExampleTwoError.getClassSchema();
    int nSchemaId = parseAndRegisterSchema(nSchema.getName(), AvroSchema.TYPE, nSchema.toString(false), List.of());
  }

  private static int parseAndRegisterSchema(String subject, String type, String definition, List<SchemaReference> references)
      throws SchemaRegistrationException {
    Optional<ParsedSchema> e1Schema = srAdmin.parseSchema(
        AvroSchema.TYPE,
        ExampleOneError.getClassSchema().toString(false),
        references == null? List.of(): references
    );
    if (e1Schema.isPresent()) {
      try {
        return srAdmin.register(subject, e1Schema.get());
      } catch (IOException | RestClientException e) {
        log.error("Error registering schema {}", subject);
        throw new SchemaRegistrationException(e, AvroSchema.TYPE, e1Schema.get(), definition);
      }
    } else{
      log.error("Error parsing schema {}", subject);
      throw new SchemaRegistrationException("Error parsing schema "+subject, AvroSchema.TYPE, e1Schema.get(), definition);
    }
  }

  private static Topology initializeTopology() {

    return new Topology() // Continued below

        /*
         * When naming Sources/Sinks/Processors in topologies, it's best to not reuse names of the topics
         * as that can be confusing when debugging. Additionally topic names may change over time, which
         * will be even more confusing. Lastly, KStreams may create internal topics depending on the
         * structure of the topologies. Those names are based on the names of the components of the
         * topology. This will also lead to confusion when attempting to inspect or diagnose errors.
         */

        // There are many ways to add a list of source topics...

        // 1. With a regex
        //.addSource("error_topic_sources", Pattern.compile("error_\\p{Alnum}*_topic")) // [0-9a-zA-Z]
        // 2. With an '...' array of parameters
        //.addSource("error_topic_sources", demoTopics[0], demoTopics[1])
        // 3. With individual calls
        .addSource("error_one_topic_source", demoTopics[0])
        .addSource("error_two_topic_source", demoTopics[1])

        // There are many ways to define processors...

        // 1. One Processor per source: Basic Error(One/Two) -> NormalizeError
        .addProcessor("error_topic_one_proc", new ExampleOneErrorProcessor.Supplier(), "error_one_topic_source")
        .addProcessor("error_topic_two_proc", new ExampleTwoErrorProcessor.Supplier(), "error_two_topic_source")
        // 2. Processor for all source (upstream) topics (or processors).
        //.addProcessor("error_topic_all_proc", (k1,v1,k2,v2) -> {}, "error_topic_one_source", "error_topic_two_source")

        // We can use a store to track errors, and generate new records (e.g. synthetic errors, summary events,
        // alerts, etc.) as they're happening. This can be used to create rate-based (e.g. more than 10 errors/hr)
        // business rules for alerting business- and product-owners.

        // 1. Track the correlationIds to see how many related errors per X minutes
        // 2. Track the errors per environment
        // 3. Track the errors per application (e.g. connector)
        // 4. Track the errors by type (e.g. Recoverable vs Non-recoverable)

        // There are many ways to add sink topics...

        // 1. With an '...' array of parameters
        //.addSink("error_normalized_topic_sink", demoTopics[2], "error_topic_processor_one", "error_topic_processor_two")
        // 2. With individual calls
        .addSink("error_normalized_topic_sink", demoTopics[2], "error_topic_processor_one")
        .addSink("error_normalized_topic_sink", demoTopics[2], "error_topic_processor_two");
  }

  private static void startErrorConsumer() {

  }

  private static void startErrorProducers() {

    // After 1000ms, per 1000ms; produce one random ExampleOneError.
    tPool.scheduleAtFixedRate(new ExampleOneProducer(), 1000, 1000, TimeUnit.MILLISECONDS);
    // After 1000ms, per 1000ms; produce one random ExampleOneError.
    tPool.scheduleAtFixedRate(new ExampleTwoProducer(), 1000, 1000, TimeUnit.MILLISECONDS);
  }

  private static void startErrorConsumers() {
    // TODO Start a thread which consumes from normalized_error_topic
  }

  private static final int        defPartitions = 1;
  private static final short      defReplication = 1;
  private static final long       defTimeout = 5L;
  private static final TimeUnit   defTimeUnit = TimeUnit.SECONDS;

  private static CreateTopicsResult initializeTopics(Collection<String> names) {

    Collection<NewTopic> topicsToCreate = names.stream()
        .map(s -> new NewTopic(s, defPartitions, defReplication))
        .collect(Collectors.toUnmodifiableList());

    CreateTopicsResult result = null;
    try {
      result = kAdmin.createTopics(topicsToCreate);
          result.all().get(defTimeout, defTimeUnit);
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      log.error("error creating topics", e);
    }

    return result;
  }

  private static Properties overlayDefaultProperties(final Properties baseProperties) {

    final Properties rv = new Properties();
    rv.putAll(baseProperties);

    rv.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "error-normalizer");
    rv.putIfAbsent(StreamsConfig.CLIENT_ID_CONFIG, "error-normalizer");
    rv.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
    rv.putIfAbsent(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    rv.putIfAbsent(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
    rv.putIfAbsent(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry:8081");
    rv.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    rv.putIfAbsent(StreamsConfig.STATE_DIR_CONFIG, DemoUtils.tempDirectory().getAbsolutePath());

    return rv;
  }

}
