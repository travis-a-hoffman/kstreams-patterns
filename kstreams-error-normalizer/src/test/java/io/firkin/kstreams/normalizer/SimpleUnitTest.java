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

package io.firkin.kstreams.normalizer;

import com.github.javafaker.Faker;
import com.github.javafaker.FunnyName;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static io.firkin.kstreams.normalizer.utils.TestUtils.TIMEOUT;
import static io.firkin.kstreams.normalizer.utils.TestUtils.TIMEUNIT;
import static io.firkin.kstreams.normalizer.utils.TestUtils.assertKafkaClusterReady;
import static io.firkin.kstreams.normalizer.utils.TestUtils.getAdminClient;
import static io.firkin.kstreams.normalizer.utils.TestUtils.getBootstrapServers;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SimpleUnitTest {

  // --- Kafka Stuff --------------------------------------------------------------------------------------
  private static Admin admin;

  private static KafkaProducer<String, GenericRecord> producer;
//  private static KafkaConsumer<String, String> consumer;

//  private static SchemaRegistryContainer;

  private static final int defPartitions = 1;
  private static final short defReplication = 1;

  // --- Test Initialization ------------------------------------------------------------------------------

  @BeforeAll
  static void initializeClients() {
    admin = getAdminClient();

    String bootstrap = getBootstrapServers();

    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
    // TODO Run with a "real" schema registry.
    // props.put("schema.registry.url", "http://localhost:2181");

    // See: https://github.com/testcontainers/testcontainers-java/blob/master/modules/kafka/src/main/java/org/testcontainers/containers/KafkaContainer.java
    props.put("schema.registry.url", "mock://simple.unit.test");

   // TODO Switch To io.firkin.containers.KafkaCluster
    //    Add Schema Registry to the initialization of a cluster.
    //    props.put("schema.registry.url", "http://localhost:8081");

    producer = new KafkaProducer<>(props);
  }

  void createTopic(String topicName) {
    try {
      NewTopic topic = new NewTopic(topicName, defPartitions, defReplication);
      Collection<NewTopic> topics = List.of(new NewTopic(topicName, defPartitions, defReplication));
      admin.createTopics(topics).all().get(TIMEOUT, TIMEUNIT);
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      e.printStackTrace();
    }
  }

  /**
   * Loads a Schema from a Resource file.
   *
   * @param schemaName file name part of the.
   * @return The Schema
   */
  static Schema loadSchema(String schemaName) {
    // This relies on the build process to copy avro files from:
    //   from: src/main/avro/* -> to: io/firkin/kstreams/normalizer/errors
    String schemaPath = "io/firkin/kstreams/normalizer/errors/"+schemaName+".avsc";
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    URL schemaUrl = classLoader.getResource(schemaPath);
    System.out.println("schemaUrl: "+schemaUrl);
    try (InputStream is = classLoader.getResourceAsStream(schemaPath)) {
      if (is == null) return null;
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String avroSchemaDefinition = reader.lines().collect(Collectors.joining(System.lineSeparator()));
      return new Schema.Parser().parse(avroSchemaDefinition);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  void assertTopicReady(String topicName) {
    ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
    listTopicsOptions.listInternal(true);
    try {
      assertTrue(admin
          .listTopics(listTopicsOptions)
          .namesToListings()
          .get(TIMEOUT, TIMEUNIT)
          .containsKey(topicName));
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      fail("Topic "+topicName+" is not ready.", e);
    }
  }

  @Test
  void testHandleHelloErrorRecord() throws Exception {
    Faker faker = new Faker();
    FunnyName funny = faker.funnyName();

    final String topicName = "SimpleUnitTest_testHandleErrorRecord_helloErrorTopic";
    assertKafkaClusterReady();
    createTopic(topicName);

    assertTopicReady(topicName);

    Schema schema = loadSchema("HelloError");
    assertNotNull(schema);

    // Using Avro Schema requires a Schema Registry

    // Can use GenericRecord to avoid (de)serializing to a POJOs.
    // Could also use GenericRecordBuilder
    GenericRecord avroRecord = new GenericData.Record(schema);
    avroRecord.put("eventId", UUID.randomUUID().toString());
    avroRecord.put("correlationId", UUID.randomUUID().toString());
    avroRecord.put("timestamp", System.currentTimeMillis()); // Unix Time
    avroRecord.put("personName", funny.name());

    String key = UUID.randomUUID().toString();
    ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topicName, key, avroRecord);
    try {
      producer.send(record);
    } catch (SerializationException e) {
      // may need to do something with it...
      fail("Could not send "+record+" to Kafka", e);
    } finally {
      // When you're finished producing records, you can flush the producer to ensure it has all been written to Kafka and
      // then close the producer to free its resources.
      producer.flush();
      producer.close();
    }

//    consumer.poll(Duration.ofSeconds(TIMEOUT));
  }
}
