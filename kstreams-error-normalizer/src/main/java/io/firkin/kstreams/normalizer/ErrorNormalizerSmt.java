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

import io.confluent.connect.avro.AvroConverter;
import io.firkin.kstreams.normalizer.errors.v1.avdl.NormalizedError;

import org.apache.kafka.common.cache.Cache;
import org.apache.kafka.common.cache.LRUCache;
import org.apache.kafka.common.cache.SynchronizedCache;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;

import java.util.Map;

/**
 * A Single Message Transform which transforms denormalized Error messages into the normalized form.
 * <p/>
 * This approach is only appropriate for relatively simple transformations, which do not rely on
 * other messages or resources to perform the transformation. For example, if the source provides
 * one object structure, and the normalized form requires different names for some fields, or if
 * a field value needs to be recalculated (e.g. change timezones, or date-time representation) an
 * SMT may be a good way to handle this.
 * <p/>
 * It would not be appropriate to call out to external systems, or to attempt to use state to modify
 * the
 * <p/>
 * This should be used with caution, as we don't want to introduce errors in our error handling.
 * Doing so could introduce errors which increase false-positive rates, making it more likely that
 * failing Records will not be available to be reprocessed as intended.
 * <p/>
 * Lastly, and most importantly, this can only be used as a preprocessor on a Sink Connector.
 */
public abstract class ErrorNormalizerSmt<R extends ConnectRecord<R>> implements Transformation<R> {

  public static final String OVERVIEW_DOC = "Normalize error records to a common connect record schema.";
  public static final String PURPOSE = "Normalizing to common error schema";
  public static final ConfigDef CONFIG_DEF = new ConfigDef()
      .define(ConfigName.INPUT_SCHEMA_SOURCE_LIST,
          ConfigDef.Type.STRING,
          "io.firkin.kstreams.normalizer.errors.avdl.v1.BaseError",
          ConfigDef.Importance.HIGH, "Field name for UUID")
      .define(ConfigName.OUTPUT_SCHEMA_NORMALIZED, ConfigDef.Type.STRING, "uuid", ConfigDef.Importance.HIGH,
      "Field name for UUID");

  private Cache<Schema, Schema> inputSchemaCache;
  private Schema normalizedSchema;

  @Override
  public R apply(R record) {
    if (operatingSchema(record) == null) {
      return applyWithoutSchema(record);
    } else {
      return applyWithSchema(record);
    }
  }

  @Override
  public ConfigDef config() {
    return null;
  }

  @Override
  public void close() {

  }

  /**
   * Configure this class with the given key-value pairs
   *
   * @param props
   */
  @Override
  public void configure(Map<String, ?> props) {

    final SimpleConfig config = new SimpleConfig(CONFIG_DEF, props);
    // TODO How to build a connect.Schema from an avro.Schema?
    SchemaBuilder sb = new SchemaBuilder(Schema.Type.STRUCT);
    org.apache.avro.Schema avroSchema = NormalizedError.getClassSchema();

    inputSchemaCache = new SynchronizedCache<>(new LRUCache<Schema, Schema>(16));
  }

  protected abstract Schema operatingSchema(R record);

  protected abstract Object operatingValue(R record);

  protected abstract R normalizedRecord(R record, Schema updatedSchema, Object updatedValue);

  private R applyWithoutSchema(R record) {
    return null;
  }

  private R applyWithSchema(R record) {
    return null;
  }

  private interface ConfigName {
    String INPUT_SCHEMA_SOURCE_LIST = "error.schema.source.list";
    String OUTPUT_SCHEMA_NORMALIZED = "error.schema.normalized";
  }

  private interface ConfigDefault {
    String DEFAULT_SOURCE_SCHEMA_LIST = "io.firkin.kstreams.normalizer.errors.avdl.v1.BaseError";
    String DEFAULT_OUTPUT_SCHEMA_NORMALIZED = "io.firkin.kstreams.normalizer.errors.avdl.v1.NormalizedError";
  }

  public static class Key<R extends ConnectRecord<R>> extends ErrorNormalizerSmt<R> {

    @Override
    protected Schema operatingSchema(R record) {
      return record.keySchema();
    }

    @Override
    protected Object operatingValue(R record) {
      return record.key();
    }

    @Override
    protected R normalizedRecord(R record, Schema normalizedSchema, Object normalizedKey) {
      return record.newRecord(
          record.topic(),
          record.kafkaPartition(),
          normalizedSchema,
          normalizedKey,
          record.valueSchema(),
          record.value(),
          record.timestamp());
    }
  }

  public static class Value<R extends ConnectRecord<R>> extends ErrorNormalizerSmt<R> {

    @Override
    protected Schema operatingSchema(R record) {
      return record.valueSchema();
    }

    @Override
    protected Object operatingValue(R record) {
      return record.value();
    }

    @Override
    protected R normalizedRecord(R record, Schema normalizedSchema, Object normalizedValue) {
      return record.newRecord(
          record.topic(),
          record.kafkaPartition(),
          record.keySchema(),
          record.key(),
          normalizedSchema,
          normalizedValue,
          record.timestamp());
    }
  }
}
