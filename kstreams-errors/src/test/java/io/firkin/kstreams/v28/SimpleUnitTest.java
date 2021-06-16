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

import io.firkin.kstreams.utils.TestUtils;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static io.firkin.kstreams.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class illustrates the basic structure of a JUnit Test which needs to interact with a single Kafka
 * broker running in a docker container on the current machine.
 */
public class SimpleUnitTest {

  // --- Kafka Stuff --------------------------------------------------------------------------------------
  private static Admin admin;
  private static KafkaProducer<String, String> producer;
  private static KafkaConsumer<String, String> consumer;

  private static final int defPartitions = 1;
  private static final short defReplication = 1;

  // --- Test Initialization ------------------------------------------------------------------------------

  @BeforeAll
  static void initializeClients() {

    admin = TestUtils.getAdminClient();

    String bootstrap = TestUtils.getBootstrapServers();
    producer = new KafkaProducer<>(
        Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
            ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString()
        ),
        new StringSerializer(),
        new StringSerializer()
    );

    consumer = new KafkaConsumer<>(
        Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
            ConsumerConfig.GROUP_ID_CONFIG, "tc-" + UUID.randomUUID(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        ),
        new StringDeserializer(),
        new StringDeserializer()
    );
  }

  // --- Test Cases ---------------------------------------------------------------------------------------

  /*
   * Simplistic happy-path test to confirm we can start up the kafka container, create a topic, publish a
   * record to it, and then consume it. This test does not use KStreams, just plain Producer/Consumers
   */
  @Test
  void testHelloWorld() throws Exception {
    assertKafkaClusterReady();
    assertAdminClientReady();

    final String topicName = "SimpleTest_testHelloWorld";
    Collection<NewTopic> topics = List.of(
        new NewTopic(topicName, defPartitions, defReplication)
    );

    admin.createTopics(topics)
        .all()
        .get(TIMEOUT, TimeUnit.SECONDS);

    consumer.subscribe(List.of(topicName));

    int recordKey = 0;
    int recordVal = 0;
    producer.send(new ProducerRecord<>(topicName,  "key-"+recordKey++, "Hello Kafka ("+recordVal+++")")).get();

    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(TIMEOUT));
    assertTrue(
        StreamSupport
            .stream(records.spliterator(), false)
            .anyMatch(e -> e.key().equals("key-0"))
    );
    consumer.unsubscribe();

    admin.deleteTopics(List.of(topicName));
  }
}
