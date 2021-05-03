package io.firkin.kstreams.v28;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.kstream.KStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.firkin.kstreams.utils.TestUtils.assertKafkaClusterReady;
import static io.firkin.kstreams.utils.TestUtils.getAdminClient;
import static io.firkin.kstreams.utils.TestUtils.getBootstrapServers;

/*
 * Tests which illustrate the behavior of KStreams failure modes.
 */
public class KStreamsClientFailureModeTest {

  // --- Kafka Stuff --------------------------------------------------------------------------------------
  private static Admin                           admin;
  private static KafkaProducer<String, String>   producer;
  private static KafkaConsumer<String, String>   consumer;

  private static KStream<String, String>         kStream;

  private static final int defPartitions = 1;
  private static final short defReplication = 1;

  // --- Test Initialization ------------------------------------------------------------------------------

  @BeforeAll
  static void initializeClients() {

    String bootstrap = getBootstrapServers();
    admin = getAdminClient();

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


  // --- Kafka Producer Test Cases ------------------------------------------------------------------------

  @Test
  void testWriteToMissingTopic() {
    assertKafkaClusterReady();
  }

  // --- Kafka Consumer Test Cases ------------------------------------------------------------------------

  @Test
  void testSubscribeMissingTopic() {
    assertKafkaClusterReady();

  }

  @Test
  void testUnsubscribeMissingTopic() {
    assertKafkaClusterReady();

  }

  @Test
  void testPollMissingTopic() {

  }

}
