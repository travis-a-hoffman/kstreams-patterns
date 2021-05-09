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

package io.firkin.kstreams.v28;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class WordCount {
  public static final String INPUT_TOPIC = "streams-plaintext-input";
  public static final String OUTPUT_TOPIC = "streams-wordcount-output";

  static Properties getStreamsConfig(final String[] args) throws IOException {
    final Properties props = new Properties();
    if (args != null && args.length > 0) {
      try (final FileInputStream fis = new FileInputStream(args[0])) {
        props.load(fis);
      }
      if (args.length > 1) {
        System.out.println("Warning: Some command line arguments were ignored. This demo only accepts an optional configuration file.");
      }
    }
    props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount");
    props.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.putIfAbsent(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
    props.putIfAbsent(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.putIfAbsent(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

    // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
    // Note: To re-run the demo, you need to use the offset reset tool:
    // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
    props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    return props;
  }

  static void createWordCountStream(final StreamsBuilder builder) {
    final KStream<String, String> source = builder.stream(INPUT_TOPIC);

    final KTable<String, Long> counts = source
        .flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))
        .groupBy((key, value) -> value)
        .count();

    // need to override value serde to Long type
    counts.toStream().to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));
  }

  public static void main(final String[] args) {
    Properties props = new Properties();
    try {
      props = getStreamsConfig(args);
    } catch (IOException e) {
      System.out.println("Could not open Streams Configuration file: "+args[0]);
      System.exit(1);
    }

    final StreamsBuilder builder = new StreamsBuilder();
    createWordCountStream(builder);
    final KafkaStreams streams = new KafkaStreams(builder.build(), props);
    final CountDownLatch latch = new CountDownLatch(1);

    // attach shutdown handler to catch control-c
    Runtime.getRuntime().addShutdownHook(new Thread("streams-wordcount-shutdown-hook") {
      @Override
      public void run() {
        streams.close();
        latch.countDown();
      }
    });

    try {
      streams.start();
      latch.await();
    } catch (final Throwable e) {
      System.exit(1);
    }
    System.exit(0);
  }
}
