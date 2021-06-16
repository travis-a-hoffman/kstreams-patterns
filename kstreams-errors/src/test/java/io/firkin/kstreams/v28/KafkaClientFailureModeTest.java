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
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.errors.UnknownTopicIdException;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.firkin.kstreams.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Tests which illustrate the behavior of Producer and Consumer failure modes.
 */
public class KafkaClientFailureModeTest {

  // --- Kafka Stuff --------------------------------------------------------------------------------------
  private static Admin                           admin;
  private static KafkaProducer<String, String>   producer;
  private static KafkaConsumer<String, String>   consumer;

  private static final Duration MSEC_5 = Duration.ofMillis(5);
  private static final Duration SEC_5 = Duration.ofSeconds(5);

  private static final long TEST_TIMEOUT_MS = 50L;
  private static final int defPartitions = 1;
  private static final short defReplication = 1;

  private static final String NO_SUCH_TOPIC = "no_such_topic";

  private static UUID testRunUuid;

  // --- Test Initialization ------------------------------------------------------------------------------

  @BeforeAll
  static void initializeClients() {

    String bootstrap = getBootstrapServers();
    admin = getAdminClient();

    testRunUuid = UUID.randomUUID();

    int clientTimeoutMs = 50; // 50 might be safer...
    final String shortTimeout = Integer.toString(clientTimeoutMs);
    final String longTimeout = Integer.toString(clientTimeoutMs*2);
    final String veryLongTimeout = Integer.toString(6000);

    Map<String, String> producerConfigs = Map.ofEntries(
        Map.entry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap),
        Map.entry(ProducerConfig.CLIENT_ID_CONFIG, "p-" + testRunUuid),
        // 50ms is ok for local testing only, it is way too short for real-world usage.
        Map.entry(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, longTimeout), // delivery.timeout >= linger.ms + request.timeout
        Map.entry(ProducerConfig.LINGER_MS_CONFIG, shortTimeout),
        Map.entry(ProducerConfig.MAX_BLOCK_MS_CONFIG, shortTimeout),
        Map.entry(ProducerConfig.METADATA_MAX_AGE_CONFIG, shortTimeout),
        Map.entry(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, shortTimeout)
    );

    producer = new KafkaProducer(producerConfigs, new StringSerializer(), new StringSerializer());

    Map<String,String> consumerConfigs = Map.ofEntries(
        Map.entry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap),
        Map.entry(ConsumerConfig.CLIENT_ID_CONFIG, "c-" + testRunUuid),
        Map.entry(ConsumerConfig.GROUP_ID_CONFIG, "cg-" + testRunUuid),
        Map.entry(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false"),
        Map.entry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
        // 50ms is ok for local testing only, it is way too short for real-world usage.
        Map.entry(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, shortTimeout),
        Map.entry(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, longTimeout),
        Map.entry(ConsumerConfig.METADATA_MAX_AGE_CONFIG, shortTimeout),
        Map.entry(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, shortTimeout),
        Map.entry(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, shortTimeout),
        // Must be greater than heartbeat,
        //   greater than less than KAFKA_GROUP_MAX_SESSION_TIMEOUT_MS and,
        //   greater than KAFKA_GROUP_MIN_SESSION_TIMEOUT_MS broker configs
        Map.entry(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, veryLongTimeout)
    );

    consumer = new KafkaConsumer(consumerConfigs, new StringDeserializer(), new StringDeserializer());
  }

  // --- Kafka Producer Test Cases ------------------------------------------------------------------------

  @Test
  void testWriteToMissingTopic() {
    assertKafkaClusterReady();

    ProducerRecord<String, String> record = new ProducerRecord<>(NO_SUCH_TOPIC, "");

    assertFalse(topics().contains(NO_SUCH_TOPIC));
    assertThrows(ExecutionException.class,
        () -> {
          Future<RecordMetadata> sendFuture = producer.send(record);
          //sendFuture.wait(WAIT_5MS.toMillis());
          assertTrue(sendFuture.isDone(), "producer.send() is not yet done, may need to inject a wait()");
          assertFalse(sendFuture.isCancelled());

          // ExecutionException isn't thrown until the caller attempts to retrieve the result.
          sendFuture.get(TIMEOUT, TIMEUNIT);
          // If a tree falls in the future, and no one is around to hear it, does it happen?
        },
        "send() throws ExecutionException on missing topics"
    );
    assertFalse(topics().contains(NO_SUCH_TOPIC), "topic: "+NO_SUCH_TOPIC+" was created unexpectedly, may need to change the producer configuration");
  }

  /*
   * Helper method which retrieves topics using the admin client. Does not return internal topics.
   */
  static Set<String> topics() {
    return topics(false);
  }

  static Set<String> topics(boolean includeInternal) {
    ListTopicsOptions lto = new ListTopicsOptions();
    lto.listInternal(includeInternal);
    ListTopicsResult ltr = admin.listTopics(lto);
    try {
      return Collections.unmodifiableSet(ltr.names().get(TIMEOUT, TIMEUNIT));
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      e.printStackTrace();
      return Set.of(); // Empty Set
    }
  }

  // --- Kafka Consumer Test Cases ------------------------------------------------------------------------

  static final Duration WAIT_1MS = Duration.ofMillis(1L);
  static final Duration WAIT_5MS = Duration.ofMillis(5L);
  static final Duration WAIT_10MS = Duration.ofMillis(10L);
  static final Duration WAIT_50MS = Duration.ofMillis(50L);
  static final Duration WAIT_100MS = Duration.ofMillis(100L);
  static final Duration WAIT_500MS = Duration.ofMillis(500L);
  static final Duration WAIT_1S = Duration.ofSeconds(1L);
  static final Duration WAIT_5S = Duration.ofSeconds(5L);
  static final Duration WAIT_SHORT = Duration.ofMillis(50L);
  static final Duration WAIT_LONG = Duration.ofSeconds(5L);

  @Test
  void testSubscribeMissingTopic() throws ExecutionException, InterruptedException {
    assertKafkaClusterReady();

    assertFalse(topics().contains(NO_SUCH_TOPIC));
    Map<String, List<PartitionInfo>> topicsMap = consumer.listTopics(WAIT_50MS);
    assertFalse(topicsMap.containsKey(NO_SUCH_TOPIC));
    List<String> topicsList = List.of(NO_SUCH_TOPIC);

    /*
     * All actions on the Kafka Client, whether via Consumer or Producer are done asynchronously, except
     * for methods that explicitly call synchronously; e.g. sendSync(), commitSync(), etc.
     */
    assertDoesNotThrow(
        () -> consumer.subscribe(topicsList),
      "subscribe() does not throw an exception when subscribing to a non-existent topic."
    );

    assertTrue(consumer.subscription().containsAll(topicsList));
    assertFalse(consumer.listTopics(WAIT_50MS).containsKey(NO_SUCH_TOPIC));
  }

  @Test
  void testUnsubscribeMissingTopic() {
    assertKafkaClusterReady();
    assertFalse(topics().contains(NO_SUCH_TOPIC));
    Map<String, List<PartitionInfo>> topicsMap = consumer.listTopics(WAIT_50MS);
    assertFalse(topicsMap.containsKey(NO_SUCH_TOPIC));

    /*
     * All actions on the Kafka Client, whether via Consumer or Producer are done asynchronously, except
     * for methods that explicitly call synchronously; e.g. sendSync(), commitSync(), etc.
     */
    assertDoesNotThrow(
        () -> consumer.unsubscribe(),
        "subscribe() does not throw an exception when subscribing to a non-existent topic."
    );

    assertFalse(consumer.subscription().contains(NO_SUCH_TOPIC));
    assertFalse(consumer.listTopics(WAIT_50MS).containsKey(NO_SUCH_TOPIC));
  }

  @Test
  void testPollMissingTopic() {
    assertKafkaClusterReady();
    assertAdminClientReady();
    assertFalse(topics().contains(NO_SUCH_TOPIC));

    consumer.subscribe(List.of(NO_SUCH_TOPIC));

    try {
      Set<String> topicNames = admin.listTopics().names().get(10, TimeUnit.MILLISECONDS);
      assertFalse(topicNames.contains(NO_SUCH_TOPIC));
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      fail("Did not expect retrieving topicNames to throw exception", e);
    }

    // TODO Doesn't a consumer throw an error when poll()ing a non-existent topic?
    /*
    assertThrows(
        ExecutionException.class,
        () -> consumer.poll(WAIT_10MS),
        "poll() throws executionException"
    );

     * other methods to test:
    consumer.position();
    consumer.close();
    consumer.commitAsync();
    consumer.commitSync();
    consumer.poll();
    consumer.unsubscribe();
    consumer.beginningOffsets();
    consumer.endOffsets();
    consumer.groupMetadata();

    consumer.seek();
    consumer.seekToBeginning();
    consumer.seekToEnd();

    consumer.wakeup();
    consumer.pause();
    consumer.resume();
    consumer.close();
     */
  }


}
