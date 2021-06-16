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

package io.firkin.kstreams.normalizer.demo.v1;

import com.github.javafaker.Faker;
import io.firkin.kstreams.normalizer.errors.v1.avsc.ExampleOneError;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ExampleOneGenerator extends DemoGeneratorUtils implements Generator<ExampleOneError> {

  private Faker faker;

  // ---- Implementation for Static Interface -----------------------------------------------------

  private static final Generator<ExampleOneError> instance = new ExampleOneGenerator();

  public static Generator<ExampleOneError> generator() {
    return instance;
  }

  public static Stream<ExampleOneError> stream() {
    return Stream.generate(instance);
  }

  // ---- Constructors ----------------------------------------------------------------------------

  public ExampleOneGenerator() {
    super(-1L);
  }

  public ExampleOneGenerator(long seed) {
    super(seed);
    this.faker = new Faker(random());
  }

  // ---- Implementation for Supplier -------------------------------------------------------------

  /**
   * Gets a random ExampleOneError.
   *
   * @return a result
   */
  @Override
  public ExampleOneError get() {
    // implement this method.
    ExampleOneError response = new ExampleOneError();

    // Infrastructure details ---------
    String envId = envId();
    String appName = envId.toLowerCase() + "-" + faker.app().name();
    response.setEnvironmentId(envId());
    String appId = idString(appName);
    response.setApplicationId(idString(appName));
    response.setInstanceId(instanceId(appName + "-inst",4));

    // Kafka details ------------------
    response.setKafkaCluster(idString("kafka-cluster"));
    response.setSuccessTopic(idString("ingest-topic"));
    response.setErrorTopic(idString("ingest-errors"));

    // Upstream details --- Applicable to an error originating from a source connector
    response.setUpstreamResponse(httpResponse());
    response.setUpstreamMetadata(sourceMetadataObject());
    response.setUpstreamData(sourceDataBytes());

    // Original Record details --- Applicable to all errors
    response.setCorrelationId(uuid());
    response.setTimestamp(faker.date().past(10, TimeUnit.SECONDS).toInstant());

    response.setData(recordDataBytes());
    response.setMetadata(recordMetadataObject());

    // Downstream details --- Applicable to an error originating from a sink connector
    //response.setDownstreamResponse(httpResponse());
    //response.setDownstreamMetadata(sinkMetadataObject());
    //response.setDownstreamData(sinkDataBytes());

    // Error Record details -----------
    //response.setTopic(recordKeyBytes()); // TODO Add the key of the original error, or is it in the metadata?
    //response.setPartition(recordKeyBytes()); // TODO Add the key of the original error, or is it in the metadata?
    //response.setKey(recordKeyBytes()); // TODO Add the key of the original error, or is it in the metadata?

    return response;
  }


  // ---- Implementation for Record Parts ---------------------------------------------------------

  public ByteBuffer sourceDataBytes() {
    return ByteBuffer.wrap(toJsonBytes(null));
  }

  public ByteBuffer sourceMetadataBytes() {
    return ByteBuffer.wrap(toJsonBytes((Object)null));
  }

  public ByteBuffer sourceResponseBytes() {
    return ByteBuffer.wrap(toJsonBytes(null));
  }

  public ByteBuffer recordDataBytes() {
    return ByteBuffer.wrap(toJsonBytes(null));
  }

  public ByteBuffer recordKeyBytes() {
    return ByteBuffer.wrap(toJsonBytes(null));
  }

  public ByteBuffer recordMetadataBytes() {
    return ByteBuffer.wrap(toJsonBytes(null));
  }

  // Return types match the Error Object's specified type(s) ------

  public Map<CharSequence, CharSequence> sourceDataObject() {
    return Map.of();
  }

  public Map<CharSequence, CharSequence> sourceMetadataObject() {
    return Map.of();
  }

  public Map<CharSequence, CharSequence> sourceResponseObject() {
    return Map.of();
  }

  public Map<CharSequence, CharSequence> recordDataObject() {
    return Map.of();
  }

  public Map<CharSequence, CharSequence> recordKeyObject() {
    return Map.of();
  }

  public Map<CharSequence, CharSequence> recordMetadataObject() {
    return Map.of();
  }

}
