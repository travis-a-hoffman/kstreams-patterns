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

package io.firkin.kstreams.utils;

import io.firkin.containers.SchemaRegistryContainer;
import io.firkin.containers.ZookeeperContainer;
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
  public static final String CP_KAFKA = "confluentinc/cp-kafka";
  public static final String CP_SERVER = "confluentinc/cp-server";
  public static final String CP_SCHEMA_REGISTRY = "confluentinc/cp-schema-registry";
  public static final String CP_ZOOKEEPER = "confluentinc/cp-zookeeper";

  // --- Test Container Stuff -----------------------------------------------------------------------------

  private static Network network;


//  @Container
//  private static KafkaContainer kafkaContainer;
//  @Container
//  private static SchemaRegistryContainer schemaRegistryContainer;
//  @Container
//  private static ZookeeperContainer zookeeperContainer;

  private static AdminClient adminClient;

  private static String bootstrap = "localhost:9092";
  private static String registry;
  private static String zookeeper;

  static {
    // TODO This can take a minute (or more) in some cases.
    //spinupContainers();
    initializeAdminClient();
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
  /*
  private static synchronized void spinupContainers() {
    if (network != null || kafkaContainer != null || bootstrap != null || adminClient != null) {
      return;
    }

    long lStart = System.currentTimeMillis();
    System.out.println("TESTCONTAINERS: Spinning up Single-Broker Kafka Cluster Container(s)...");
    network = Network.newNetwork();

    // TODO Make it possible to spin up multiple brokers
    kafkaContainer = new KafkaContainer(DockerImageName.parse(CP_KAFKA).withTag(CONFLUENT_VERSION))
        .withNetwork(network)
        .withEnv("KAFKA_BROKER_ID", "1")
        .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_OFFSETS_TOPIC_NUM_PARTITIONS", "1")
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "false") // Only allow admin client to create topics.
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1");

//    schemaRegistryContainer = new SchemaRegistryContainer(DockerImageName.parse(CP_SCHEMA_REGISTRY).withTag(CONFLUENT_VERSION))
//        .withNetwork(network);

//    zookeeperContainer = new ZookeeperContainer(DockerImageName.parse(CP_ZOOKEEPER).withTag(CONFLUENT_VERSION))
//        .withNetwork(network);


    try {
//      Startables.deepStart(List.of(kafkaContainer, schemaRegistryContainer, zookeeperContainer)).get(120, TIMEUNIT);
      Startables.deepStart(List.of(kafkaContainer)).get(120, TIMEUNIT);
      bootstrap = kafkaContainer.getBootstrapServers();
//      zookeeper = zookeeperContainer.getInternalUrl();
//      registry  = schemaRegistryContainer.getUrl();

//      zookeeperContainer.start();
//      assertTrue(zookeeperContainer.isCreated() && zookeeperContainer.isRunning());
      kafkaContainer.start();
      assertTrue(kafkaContainer.isCreated() && kafkaContainer.isRunning());
//      schemaRegistryContainer.start();
//      assertTrue(schemaRegistryContainer.isCreated() && schemaRegistryContainer.isRunning());
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
*/
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
  /*
  } */

  public static void initializeAdminClient() {
    adminClient = AdminClient.create(
        Map.of(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap
        ));
  }

  // --- Kafka Container Assertions

  public static void assertKafkaClusterReady() {
//    assertNotNull(kafkaContainer);
//    assertTrue(kafkaContainer.isRunning());
  }

  public static void assertSchemaRegistryReady() {
//    assertNotNull(kafkaContainer);
//    assertTrue(kafkaContainer.isRunning());
  }

  public static void assertAdminClientReady() {
    assertNotNull(adminClient, "KafkaAdmin client not ready.");
  }

}
