package io.firkin.kstreams.utils;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
    System.out.println("TESTCONTAINERS: Spinning up Single-Broker Kafka Container...");
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
    System.out.println("TESTCONTAINERS: Spinning up Kafka Containers took " +d.toSeconds()+"."+d.toMillisPart()+" seconds.");

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
