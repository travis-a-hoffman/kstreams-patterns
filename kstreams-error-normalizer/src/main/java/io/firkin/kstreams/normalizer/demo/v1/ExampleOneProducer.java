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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.javafaker.Faker;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.firkin.kstreams.normalizer.errors.v1.avsc.ExampleOneError;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.internals.KStreamFlatTransform;

import java.io.Closeable;
import java.util.Properties;
import java.util.Random;

import static io.firkin.kstreams.normalizer.demo.v1.DemoGeneratorUtils.uuid;

/**
 * Standalone thread/application which will publish <code>ExampleOneError</code>s to a topic
 */
public class ExampleOneProducer implements Runnable, AutoCloseable {

  private static Generator<ExampleOneError> generator = ExampleOneGenerator.generator();

  static KafkaProducer producer;            // All threads share a single producer (kafka client).
  static double errorRateMs = 0.001;        // One per second, per thread.
  static int    nThreads = 1;               // One thread
  static String topic = "error_one_topic";  // need to coordinate w/Demo application

  /**
   * As a convenience, the ExampleOneProducer may be run as a standalone application.
   *
   * @param args
   */
  public static void main(String[] args) throws InterruptedException {

    Properties clientProps = new Properties();
    clientProps.put("a", "b");

    producer = new KafkaProducer(clientProps, new StringSerializer(), new KafkaAvroSerializer());

    // TODO Add parameter: rate (float errors/min) parameter, default is 60 (1 per second)
    // TODO Add parameter: threads (int) ... max # cores
    // TODO Add parameter: topic (string)
    while (true) {
      producer.send(randomRecord());
      Thread.sleep((long) (errorRateMs / 1000.0));
    }
  }

  public static ProducerRecord<String, ExampleOneError> randomRecord() {
    return new ProducerRecord<>(topic, uuid(), randomError());
  }

  public static ExampleOneError randomError() {
    return generator.get();
  }

  /**
   * When an object implementing interface <code>Runnable</code> is used
   * to create a thread, starting the thread causes the object's
   * <code>run</code> method to be called in that separately executing
   * thread.
   * <p>
   * The general contract of the method <code>run</code> is that it may
   * take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    ExampleOneError error = randomError();
    ProducerRecord<String, ExampleOneError> record = new ProducerRecord<>("error_one_topic", error);
    producer.send(record);
  }

  /**
   * Closes this resource, relinquishing any underlying resources.
   * This method is invoked automatically on objects managed by the
   * {@code try}-with-resources statement.
   *
   * <p>While this interface method is declared to throw {@code
   * Exception}, implementers are <em>strongly</em> encouraged to
   * declare concrete implementations of the {@code close} method to
   * throw more specific exceptions, or to throw no exception at all
   * if the close operation cannot fail.
   *
   * <p> Cases where the close operation may fail require careful
   * attention by implementers. It is strongly advised to relinquish
   * the underlying resources and to internally <em>mark</em> the
   * resource as closed, prior to throwing the exception. The {@code
   * close} method is unlikely to be invoked more than once and so
   * this ensures that the resources are released in a timely manner.
   * Furthermore it reduces problems that could arise when the resource
   * wraps, or is wrapped, by another resource.
   *
   * <p><em>Implementers of this interface are also strongly advised
   * to not have the {@code close} method throw {@link
   * InterruptedException}.</em>
   * <p>
   * This exception interacts with a thread's interrupted status,
   * and runtime misbehavior is likely to occur if an {@code
   * InterruptedException} is {@linkplain Throwable#addSuppressed
   * suppressed}.
   * <p>
   * More generally, if it would cause problems for an
   * exception to be suppressed, the {@code AutoCloseable.close}
   * method should not throw it.
   *
   * <p>Note that unlike the {@link Closeable#close close}
   * method of {@link Closeable}, this {@code close} method
   * is <em>not</em> required to be idempotent.  In other words,
   * calling this {@code close} method more than once may have some
   * visible side effect, unlike {@code Closeable.close} which is
   * required to have no effect if called more than once.
   * <p>
   * However, implementers of this interface are strongly encouraged
   * to make their {@code close} methods idempotent.
   *
   * @throws Exception if this resource cannot be closed
   */
  @Override
  public void close() throws Exception {
    producer.close();
  }
}
