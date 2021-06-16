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

package io.firkin.kstreams.normalizer.errors.v1.avsc;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;

public class ExampleTwoErrorProcessor<KIn, ExampleOneError, KOut, NormalizedError> implements Processor {
  @Override
  public void process(Record record) {

  }

  public static class Supplier<KIn, ExampleOneError, KOut, NormalizedError> implements ProcessorSupplier {

    @Override
    public Processor<KIn, ExampleOneError, KOut, NormalizedError> get() {
      return new ExampleTwoErrorProcessor<KIn, ExampleOneError, KOut, NormalizedError>();
    }
  }
}
