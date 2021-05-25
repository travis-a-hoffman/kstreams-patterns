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

package io.firkin.kstreams.normalizer;

import io.firkin.kstreams.normalizer.errors.v1.avdl.NormalizedError;
import org.apache.kafka.clients.producer.KafkaProducer;

/**
 * A Producer which uses a common Normalized Error defined in Avro, and a generated object model.
 * Rather than producing to an application-specific error topic, it produces errors directly to
 * a common error topic.
 *
 * In this pattern, every Producer is responsible to correctly implement error handling. The best way
 * to ensure consistent behavior is to use a shared library which all applications can easily apply
 * to their specific use case.
 *
 */
public class NormalizedErrorProducer {

  KafkaProducer<String, String> successtopic;
  KafkaProducer<String, NormalizedError> errorTopic;

  public static void main(String[] argv) {

  }



}
