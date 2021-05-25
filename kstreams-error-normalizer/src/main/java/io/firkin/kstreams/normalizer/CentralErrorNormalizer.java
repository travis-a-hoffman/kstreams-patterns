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

import io.firkin.kstreams.normalizer.errors.v1.avdl.BaseError;
import io.firkin.kstreams.normalizer.errors.v1.avdl.ExampleOneError;
import io.firkin.kstreams.normalizer.errors.v1.avdl.ExampleTwoError;
import io.firkin.kstreams.normalizer.errors.v1.avdl.NormalizedError;

//import io.firkin.kstreams.normalizer.errors.v1.avsc.BaseError;
//import io.firkin.kstreams.normalizer.errors.v1.avsc.ExampleOneError;
//import io.firkin.kstreams.normalizer.errors.v1.avsc.ExampleTwoError;
//import io.firkin.kstreams.normalizer.errors.v1.avsc.NormalizedError;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.errors.ProductionExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.apache.kafka.streams.StreamsConfig.*;

/**
 * An example of how to normalize topics from
 */
public class CentralErrorNormalizer {
  private static final String APPLICATION_ID = "central-error-normalizer";
  private static final String CLIENT_ID = "central-error-normalizer-client";

  public static void main(String[] args) {
    final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
    final Properties streamsConfig = new Properties();

    // See https://kafka.apache.org/28/documentation/streams/developer-guide/config-streams.html for details on configuring KStreams.
    // These two are required.
    streamsConfig.put(APPLICATION_ID_CONFIG, APPLICATION_ID);
    streamsConfig.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

    // The follow are good to use. CLIENT_ID lets you configure the
    streamsConfig.put(CLIENT_ID_CONFIG, CLIENT_ID);

    // If all else fails, treat both the key an value as if they are raw bytes with no schema.
    streamsConfig.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
    streamsConfig.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());

    // It's a good idea to define some kind of error handling for every KStreams application you write.
//    streamsConfig.put(DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, new CentralDeserializationExceptionHandler());
//    streamsConfig.put(DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, new CentralProductionExceptionHandler());
//    streamsConfig.put(CentralUncaughtExceptionHandler)

    final Serde<String> stringSerde = Serdes.String();
    final Serde<byte[]> byteArraySerde = Serdes.ByteArray();
    final Serde<BaseError> baseErrorSerde = Serdes.serdeFrom(BaseError.class);
    final Serde<ExampleOneError> exampleOneSerde = Serdes.serdeFrom(ExampleOneError.class);
    final Serde<ExampleTwoError> exampleTwoSerde = Serdes.serdeFrom(ExampleTwoError.class);
    final Serde<NormalizedError> normalizedErrorSerde = Serdes.serdeFrom(NormalizedError.class);

    final StreamsBuilder streamsBuilder = new StreamsBuilder();

    // To read errors from multiple topics, we (may) have to be completely agnostic about the contents
    // of the Records themselves.

    // Example 1: Read errors from one specific error topic.
    final KStream<byte[], ExampleOneError> oneErrorTopicStream =
        streamsBuilder.stream("example.one.error.topic", Consumed.with(byteArraySerde, exampleOneSerde));

    final KStream<byte[], ExampleTwoError> twoErrorTopicStream =
        streamsBuilder.stream("example.two.error.topic", Consumed.with(byteArraySerde, exampleTwoSerde));

    // Example 2: Use a Pattern or list to match multiple error topics without using a schema.
    final KStream<byte[], byte[]> allErrorTopicsStream =
        streamsBuilder.stream(Pattern.compile(".*dlq"), Consumed.with(byteArraySerde, byteArraySerde));
    final KStream<byte[], byte[]> specificListOfErrorTopicsStream =
        streamsBuilder.stream(List.of("example.one.error.topic", "example.two.error.topic"));

    // Example 3: Use a compatible Avro model which models only the common core values. a.k.a. "Duck Typing"
    final KStream<byte[], BaseError> commonErrorTopicsStream =
        streamsBuilder.stream(List.of("example.one.error.topic", "example.two.error.topic"), Consumed.with(byteArraySerde, baseErrorSerde));
  }


  class CentralDeserializationExceptionHandler implements DeserializationExceptionHandler {
    @Override
    public DeserializationHandlerResponse handle(ProcessorContext processorContext, ConsumerRecord<byte[], byte[]> consumerRecord, Exception e) {
      return null;
    }

    /**
     * Configure this class with the given key-value pairs
     *
     * @param configs
     */
    @Override
    public void configure(Map<String, ?> configs) {

    }
  }

  class CentralProductionExceptionHandler implements ProductionExceptionHandler {

    @Override
    public ProductionExceptionHandlerResponse handle(ProducerRecord<byte[], byte[]> producerRecord, Exception e) {
      return null;
    }

    /**
     * Configure this class with the given key-value pairs
     *
     * @param configs
     */
    @Override
    public void configure(Map<String, ?> configs) {

    }
  }

  /*
  class CentralUncaughtExceptionHandler implements StreamsUncaughtExceptionHandler {

    @Override
    public StreamThreadExceptionResponse handle(Throwable exception)  {

    }
  }
   */
}
