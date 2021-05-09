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

package io.firkin.kstreams.normalizer.utils;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {

  public static final long       TIMEOUT = 5L;
  public static final TimeUnit   TIMEUNIT = TimeUnit.SECONDS;
  public static final String KAFKA_VERSION = "2.8";
  public static final String CONFLUENT_VERSION = "6.1.1";
  public static final String CONFLUENT_PLATFORM = "confluentinc/cp-kafka";

  // --- Test Container Stuff -----------------------------------------------------------------------------
  @Container
  private static KafkaContainer container;
  private static Network network;

  private static String bootstrap;
  private static AdminClient adminClient;

  static {
    // TODO This can take a minute (or more) in some cases.
    spinupContainers();
  }

  public static AdminClient getAdminClient() {
    return adminClient;
  }

  public static String getBootstrapServers() {
    return bootstrap;
  }

  // --- Container Initialization -------------------------------------------------------------------------

  /**
   * Call during "@BeforeAll" in your Test Class so that whichever one comes first.
   *
   * NOTE: TestContainers automatically cleans up / spins down all containers.
   */
  private static synchronized void spinupContainers() {
    if (network != null || container != null || bootstrap != null || adminClient != null) {
      return;
    }

    long lStart = System.currentTimeMillis();
    System.out.println("TESTCONTAINERS: Spinning up Single-Broker Kafka Cluster Container(s)...");
    network = Network.newNetwork();
    container = new KafkaContainer(DockerImageName.parse(CONFLUENT_PLATFORM)
        .withTag(CONFLUENT_VERSION))
        .withNetwork(network)
        .withEnv("KAFKA_BROKER_ID", "1")
        .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_OFFSETS_TOPIC_NUM_PARTITIONS", "1")
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1");

    try {
      Startables.deepStart(List.of(container)).get(120, TIMEUNIT);
      bootstrap = container.getBootstrapServers();
      container.start();
      assertTrue(container.isCreated() && container.isRunning());
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      fail("Could not spin up the Kafka Container", e);
    }

    adminClient = AdminClient.create(
        Map.of(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap
        ));

    long lStop = System.currentTimeMillis();
    Duration d = Duration.ofMillis(lStop - lStart);
    System.out.println("TESTCONTAINERS: Spinning up Kafka Cluster Container(s) took " +d.toSeconds()+"."+d.toMillisPart()+" seconds.");

    /*
     * Each test case will need to configure the consumer/producer for itself.
     * Here are some very simple examples. Note the use of UUIDs to prevent
     * tests from accidentally stepping on each others topics.
     *
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
     */
  }

  // --- Kafka Container Assertions

  public static void assertKafkaClusterReady() {
    assertNotNull(container);
    assertTrue(container.isRunning());
  }

  public static void assertSchemaRegistryReady() {
    assertNotNull(container);
    assertTrue(container.isRunning());
  }

}
