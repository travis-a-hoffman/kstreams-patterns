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

import io.confluent.kafka.schemaregistry.ParsedSchema;

public class SchemaRegistrationException extends Error {

  /**
   * This field holds the exception if the
   * SchemaRegistrationException(Throwable thrown) constructor was
   * used to instantiate the object
   *
   * @serial
   *
   */
  private Throwable exception;

  private ParsedSchema schema;

  private String definition;

  private String type; // e.g. AvroSchema.TYPE = "AVRO", ProtoBuf.TYPE="PROTOBUF", etc.

  /**
   * Constructs an <code>SchemaRegistrationException</code> with
   * <code>null</code> as its detail message string and with no saved
   * throwable object.
   * A detail message is a String that describes this particular exception.
   */
  public SchemaRegistrationException() {
    initCause(null);  // Disallow subsequent initCause
  }

  /**
   * Constructs a new <code>SchemaRegistrationException</code> class by
   * saving a reference to the <code>Throwable</code> object thrown for
   * later retrieval by the {@link #getException()} method. The detail
   * message string is set to <code>null</code>.
   *
   * @param thrown The exception thrown
   */
  public SchemaRegistrationException(Throwable thrown) {
    initCause(null);  // Disallow subsequent initCause
    this.exception = thrown;
  }

  /**
   * Constructs an SchemaRegistrationException with the specified detail
   * message string.  A detail message is a String that describes this
   * particular exception. The detail message string is saved for later
   * retrieval by the {@link Throwable#getMessage()} method. There is no
   * saved throwable object.
   *
   *
   * @param s the detail message
   */
  public SchemaRegistrationException(String s) {
    super(s);
    initCause(null);  // Disallow subsequent initCause
  }

  /**
   * Constructs an SchemaRegistrationException with the specified detail
   * message string.  A detail message is a String that describes this
   * particular exception. The detail message string is saved for later
   * retrieval by the {@link Throwable#getMessage()} method. There is no
   * saved throwable object.
   *
   *
   * @param thrown The exception thrown
   * @param type The parsed schema type
   * @param schema The parsed schema
   * @param definition The schema definition
   */
  public SchemaRegistrationException(Throwable thrown, String type, ParsedSchema schema, String definition) {
    initCause(null);  // Disallow subsequent initCause
    this.exception = thrown;
    this.type = type;
    this.schema = schema;
    this.definition = definition;
  }

  /**
   * Constructs an SchemaRegistrationException with the specified detail
   * message string.  A detail message is a String that describes this
   * particular exception. The detail message string is saved for later
   * retrieval by the {@link Throwable#getMessage()} method. There is no
   * saved throwable object.
   *
   *
   * @param s the detail message
   * @param type The parsed schema type
   * @param schema The parsed schema
   * @param definition The schema definition
   */
  public SchemaRegistrationException(String s, String type, ParsedSchema schema, String definition) {
    super(s);
    initCause(null);  // Disallow subsequent initCause
    this.type = type;
    this.schema = schema;
    this.definition = definition;
  }

  /**
   * Returns the exception that occurred during schema registration that
   * caused this error to be created.
   *
   * <p>This method predates the general-purpose exception chaining facility.
   * The {@link Throwable#getCause()} method is now the preferred means of
   * obtaining this information.
   *
   * @return the saved throwable object of this
   *         <code>SchemaRegistrationException</code>, or <code>null</code>
   *         if this <code>SchemaRegistrationException</code> has no saved
   *         throwable object.
   */
  public Throwable getException() {
    return exception;
  }

  /**
   * Returns the cause of this error (the exception that occurred
   * during schema registration that caused this error to be created).
   *
   * @return  the cause of this error or <code>null</code> if the
   *          cause is nonexistent or unknown.
   */
  public Throwable getCause() {
    return exception;
  }

  /**
   * Returns the cause of this error (the schema that failed schema
   * registration that caused this error to be created).
   *
   * @return  the schema that caused the error or <code>null</code> if the
   *          schema is nonexistent or unknown.
   */
  public ParsedSchema getParsedSchema() {
    return schema;
  }

  /**
   * Returns the cause of this error (the schema definition that failed
   * schema registration that caused this error to be created).
   *
   * @return  the schema definition that caused the error or <code>null</code>
   *          if the schema definition is nonexistent or unknown.
   */
  public String getDefinition() {
    return definition;
  }


  public String getType() {
    return type;
  }
}
