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

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.firkin.kstreams.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Tests which illustrate the behavior of the Kafka Admin Client failure modes.
 */
public class KafkaAdminClientFailureModeTest {

  // --- Kafka Stuff --------------------------------------------------------------------------------------
  private static Admin           admin;

  private static final int defPartitions = 1;
  private static final short defReplication = 1;

  // --- Test Initialization ------------------------------------------------------------------------------

  @BeforeAll
  static void initializeClients() {
    admin = getAdminClient();
  }

  // --- Admin Client Test Cases --------------------------------------------------------------------------

  /*
   * Simplistic happy-path test to confirm we can create and delete a topic. Basically, this
   * confirms the container is up, and the kafka broker is fully initialized.
   */
  @Test
  void testCreateTopic() throws Exception {
    assertKafkaClusterReady();
    assertAdminClientReady();

    final String topicName = "KafkaAdminClientFailureModeTest_testCreateTopic";
    Collection<NewTopic> topics = List.of(new NewTopic(topicName, defPartitions, defReplication));
    admin.createTopics(topics).all().get(TIMEOUT, TIMEUNIT);

    ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
    listTopicsOptions.listInternal(true);
    Map<String, TopicListing> topicMap = admin
        .listTopics(listTopicsOptions)
        .namesToListings()
        .get(TIMEOUT, TIMEUNIT);

    assertTrue(topicMap.containsKey(topicName));

    admin.deleteTopics(List.of(topicName)).all().get(TIMEOUT, TIMEUNIT);
    topicMap = admin
        .listTopics(listTopicsOptions)
        .namesToListings()
        .get(TIMEOUT, TIMEUNIT);
    assertFalse(topicMap.containsKey(topicName));
  }

  @Test
  void testCreateTopicThatAlreadyExists() throws Exception {
    assertKafkaClusterReady();
    assertAdminClientReady();

    final String topicName = "KafkaAdminClientFailureModeTest_testCreateTopicThatAlreadyExists";
    Collection<NewTopic> topics = List.of(new NewTopic(topicName, defPartitions, defReplication));
    admin.createTopics(topics).all().get(TIMEOUT, TIMEUNIT);

    ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
    listTopicsOptions.listInternal(false);
    Map<String, TopicListing> topicMap = admin
        .listTopics(listTopicsOptions)
        .namesToListings()
        .get(TIMEOUT, TIMEUNIT);
    assertTrue(topicMap.containsKey(topicName));

    // This call to admin.createTopics() will result in a java.util.concurrent.ExecutionException,
    // which is caused by an org.apache.kafka.common.errors.TopicExistsException
    assertThrows(ExecutionException.class,
        () -> admin.createTopics(topics).all().get(TIMEOUT, TIMEUNIT),
        "createTopics() will throw TopicExistsException when creating a pre-existing topic."
    );

    // Cleanup
    admin.deleteTopics(List.of(topicName)).all().get(TIMEOUT, TIMEUNIT);
    topicMap = admin
        .listTopics(listTopicsOptions)
        .namesToListings()
        .get(TIMEOUT, TIMEUNIT);
    assertFalse(topicMap.containsKey(topicName));
  }

  // TODO This test fails because ACLs are not configured to restrict ... or it just fails silently?
  @Test
  void testCreateTopicThatIsKafkaInternal() throws Exception {
    assertKafkaClusterReady();
    assertAdminClientReady();

    final String topicName = "__consumer_offsets";

    ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
    listTopicsOptions.listInternal(true);

    Collection<NewTopic> topics = List.of(new NewTopic(topicName, defPartitions, defReplication));
    // Does not cause an error.
    admin.createTopics(topics).all().get(TIMEOUT, TIMEUNIT);

    Map<String, TopicListing> topicMap = admin
        .listTopics(listTopicsOptions)
        .namesToListings()
        .get(TIMEOUT, TIMEUNIT);
    assertTrue(topicMap.containsKey(topicName));
  }

  /*
   * Kafka Topic Name Validation Logic:
   *
   * https://github.com/apache/kafka/blob/35d47f488b63941ac15dd3159005d6f40da6ef18/clients/src/main/java/org/apache/kafka/common/internals/Topic.java#L34-L47
   */
  @Test
  void testCreateTopicWithNullName() throws Exception {
    assertKafkaClusterReady();
    assertAdminClientReady();

    final String topicName = null;
    Collection<NewTopic> topics = List.of(new NewTopic(topicName, defPartitions, defReplication));

    assertThrows(ExecutionException.class,
        () -> admin.createTopics(topics).all().get(TIMEOUT, TIMEUNIT),
        "createTopics() will throw TopicExistsException when creating a pre-existing topic."
    );
  }

  @Test
  void testCreateTopicWithEmptyName() throws Exception {
    assertKafkaClusterReady();
    assertAdminClientReady();

    final String topicName = "";
    Collection<NewTopic> topics = List.of(new NewTopic(topicName, defPartitions, defReplication));

    assertThrows(ExecutionException.class,
        () -> admin.createTopics(topics).all().get(TIMEOUT, TIMEUNIT),
        "createTopics() will throw TopicExistsException when creating a pre-existing topic."
    );
  }

  /*
   * See: https://github.com/apache/kafka/blob/35d47f488b63941ac15dd3159005d6f40da6ef18/clients/src/main/java/org/apache/kafka/common/internals/Topic.java#L34
   */
  @Test
  void testCreateTopicWithMaxLengthName() throws Exception {
    assertKafkaClusterReady();
    assertAdminClientReady();

    final String topicName =
        "KafkaAdminClientFailureModeTest_testCreateTopicWithTooLongName" +  // 62 chars * 4 = 248
        "_0123456789012345678901234567890123456789012345678901234567890" +
        "_0123456789012345678901234567890123456789012345678901234567890" +
        "_0123456789012345678901234567890123456789012345678901234567890" +
        "a"; // 249th character

    Collection<NewTopic> topics = List.of(new NewTopic(topicName, defPartitions, defReplication));
    admin.createTopics(topics).all().get(TIMEOUT, TIMEUNIT);

    ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
    listTopicsOptions.listInternal(false);
    Map<String, TopicListing> topicMap = admin
        .listTopics(listTopicsOptions)
        .namesToListings()
        .get(TIMEOUT, TIMEUNIT);

    assertTrue(topicMap.containsKey(topicName));

    admin.deleteTopics(List.of(topicName)).all().get(TIMEOUT, TIMEUNIT);
    topicMap = admin
        .listTopics(listTopicsOptions)
        .namesToListings()
        .get(TIMEOUT, TIMEUNIT);
    assertFalse(topicMap.containsKey(topicName));
  }

  @Test
  void testCreateTopicWithTooLongName() throws Exception {
    assertKafkaClusterReady();
    assertAdminClientReady();

    final String topicName =
        "KafkaAdminClientFailureModeTest_testCreateTopicWithTooLongName" +
        "_0123456789012345678901234567890123456789012345678901234567890" +
        "_0123456789012345678901234567890123456789012345678901234567890" +
        "_0123456789012345678901234567890123456789012345678901234567890" +
        "ab"; // 62 chars * 4 = 248 + 2 = 250 character (1 character too long)

    Collection<NewTopic> topics = List.of(new NewTopic(topicName, defPartitions, defReplication));

    assertThrows(ExecutionException.class,
        () -> admin.createTopics(topics).all().get(TIMEOUT, TIMEUNIT),
        "createTopics() will throw TopicExistsException when creating a pre-existing topic."
    );
  }

  /*
   * See https://github.com/apache/kafka/blob/35d47f488b63941ac15dd3159005d6f40da6ef18/clients/src/main/java/org/apache/kafka/common/internals/Topic.java#L29
   */
  @Test
  void testCreateTopicWithIllegalCharactersInName() throws Exception {
    assertKafkaClusterReady();
    assertAdminClientReady();

    final String topicName = "KafkaAdminClientFailureModeTest!@#$%^*()+=testCreateTopicWithIllegalCharactersInName";
    Collection<NewTopic> topics = List.of(new NewTopic(topicName, defPartitions, defReplication));

    assertThrows(ExecutionException.class,
        () -> admin.createTopics(topics).all().get(TIMEOUT, TIMEUNIT),
        "createTopics() will throw ExecutionException when creating a topic with illegal characters in the name."
    );
  }

  // Without ACLs in place, it seems that any admin client can indeed delete internal topics.
  // I would have expected that all admin clients would be prevented from deleting internal kafka topics.
  // TODO: Add ACLs to demonstrate security restrictions.
  @Test
  void testDeleteTopicThatIsKafkaInternal() throws Exception {
    assertKafkaClusterReady();
    assertAdminClientReady();

    final String topicName = "__consumer_offsets";

    ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
    listTopicsOptions.listInternal(true);

    assertThrows(ExecutionException.class,
        () -> admin.deleteTopics(List.of(topicName)).all().get(TIMEOUT, TIMEUNIT),
        "delete topic __consumer_offsets should throw an UnknownTopicOrPartitionException"
    );
    Map<String, TopicListing> topicMap = admin
        .listTopics(listTopicsOptions)
        .namesToListings()
        .get(TIMEOUT, TIMEUNIT);

    // Should not have been able to actually delete the topic.
    assertFalse(topicMap.containsKey(topicName));
  }

  @Test
  void testDeleteTopicThatDoesNotExist() throws Exception {
    assertKafkaClusterReady();
    assertAdminClientReady();

    final String topicName = "KafkaAdminClientFailureModeTest_testDeleteTopicThatDoesNotExist";
    Collection<NewTopic> topics = List.of(new NewTopic(topicName, defPartitions, defReplication));
    admin.createTopics(topics).all().get(TIMEOUT, TIMEUNIT);

    ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
    listTopicsOptions.listInternal(false);
    Map<String, TopicListing> topicMap = admin
        .listTopics(listTopicsOptions)
        .namesToListings()
        .get(TIMEOUT, TIMEUNIT);

    assertTrue(topicMap.containsKey(topicName));

    admin.deleteTopics(List.of(topicName)).all().get(TIMEOUT, TIMEUNIT);
    topicMap = admin
        .listTopics(listTopicsOptions)
        .namesToListings()
        .get(TIMEOUT, TIMEUNIT);
    assertFalse(topicMap.containsKey(topicName));
  }
}
