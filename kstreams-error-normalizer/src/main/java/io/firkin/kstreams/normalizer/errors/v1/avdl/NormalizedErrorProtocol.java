/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package io.firkin.kstreams.normalizer.errors.v1.avdl;

/** * A protocol defining multiple Record types in a single file, which makes it
 * easier to bundle related Records, and to version schema changes across multiple
 * record types.
 *
 * Design decision: A protocol defines more than just avro types, it defines an RPC exchange
 *   of information between systems. This includes 3 things:
 *     1. Record Types - The data normally exchanged by protocol exchanges
 *     2. Error Types  - The data returned by the protocol methods upon an error
 *     3. RPC Methods  - The method calls provided in the generated client library
 *
 *   In this example, we are exploring patterns for best aligning a variety of error messages
 *   from other systems, not from an avro protocol. Unfortunately, our "Error" types are the
 *   "normal" records exchanged by this protocol, not the protocol errors.
 *
 *   Overloading the type names is regrettably confusing, this example is about demonstrating
 *   how to use an .avdl (Avro Domain-Specific Language) protocol/file to bundle related schemas
 *   together in a more easily managed (and versioned) way.
 *
 *   One of the best parts of this file is that it supports comments (like this one), which is
 *   not (easily) possible in .avsc files, which are just JSON. */
@org.apache.avro.specific.AvroGenerated
public interface NormalizedErrorProtocol {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"NormalizedErrorProtocol\",\"namespace\":\"io.firkin.kstreams.normalizer.errors.v1.avdl\",\"doc\":\"* A protocol defining multiple Record types in a single file, which makes it\\n * easier to bundle related Records, and to version schema changes across multiple\\n * record types.\\n *\\n * Design decision: A protocol defines more than just avro types, it defines an RPC exchange\\n *   of information between systems. This includes 3 things:\\n *     1. Record Types - The data normally exchanged by protocol exchanges\\n *     2. Error Types  - The data returned by the protocol methods upon an error\\n *     3. RPC Methods  - The method calls provided in the generated client library\\n *\\n *   In this example, we are exploring patterns for best aligning a variety of error messages\\n *   from other systems, not from an avro protocol. Unfortunately, our \\\"Error\\\" types are the\\n *   \\\"normal\\\" records exchanged by this protocol, not the protocol errors.\\n *\\n *   Overloading the type names is regrettably confusing, this example is about demonstrating\\n *   how to use an .avdl (Avro Domain-Specific Language) protocol/file to bundle related schemas\\n *   together in a more easily managed (and versioned) way.\\n *\\n *   One of the best parts of this file is that it supports comments (like this one), which is\\n *   not (easily) possible in .avsc files, which are just JSON.\",\"types\":[{\"type\":\"record\",\"name\":\"HelloRecord\",\"doc\":\"Records and Errors are more-or-less identical, they generate very similar code. In\\n     Java, error types will extend a Throwable, and include a value + cause in the object.\\n     The cause is not\",\"fields\":[{\"name\":\"message\",\"type\":\"string\",\"doc\":\"Message to send.\",\"default\":\"Hello, World!\"}]},{\"type\":\"record\",\"name\":\"BaseError\",\"doc\":\"Example Model for errors like those which might arise from a Source Connector.\\n     Errors of this type will need to be mapped to a common schema.\",\"fields\":[{\"name\":\"timestamp\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"},\"doc\":\"The timestamp of when the error occurred, in Unix epoch time UTC (required)\"},{\"name\":\"correlation_id\",\"type\":{\"type\":\"string\",\"logicalType\":\"uuid\"},\"doc\":\"Unique id for event tracing (required)\"},{\"name\":\"environment_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Environment where the error took place (optional)\",\"default\":null},{\"name\":\"application_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Application where the error took place (optional)\",\"default\":null},{\"name\":\"instance_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Instance where the error took place (optional)\",\"default\":null},{\"name\":\"metadata\",\"type\":[\"null\",{\"type\":\"map\",\"values\":\"string\"}],\"doc\":\"Metadata from the source system (optional)\",\"default\":null},{\"name\":\"data\",\"type\":[\"null\",\"bytes\"],\"doc\":\"Data from the source system (optional)\",\"default\":null},{\"name\":\"peters_data\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"ExampleOneError\",\"doc\":\"Example Model for errors like those which might arise from a Source Connector. Errors of this type will need to be mapped to a common schema.\",\"fields\":[{\"name\":\"timestamp\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"},\"doc\":\"The timestamp of when the error occurred, in Unix epoch time UTC (required)\"},{\"name\":\"correlation_id\",\"type\":{\"type\":\"string\",\"logicalType\":\"uuid\"},\"doc\":\"Unique id for event tracing (required)\"},{\"name\":\"environment_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Environment where the error took place (optional)\",\"default\":null},{\"name\":\"application_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Application where the error took place (optional)\",\"default\":null},{\"name\":\"instance_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Instance where the error took place (optional)\",\"default\":null},{\"name\":\"metadata\",\"type\":[\"null\",{\"type\":\"map\",\"values\":\"string\"}],\"doc\":\"Metadata from the source system (optional)\",\"default\":null},{\"name\":\"data\",\"type\":[\"null\",\"bytes\"],\"doc\":\"Data from the source system (optional)\",\"default\":null},{\"name\":\"upstream_metadata\",\"type\":{\"type\":\"map\",\"values\":\"string\"},\"doc\":\"Metadata from the upstream system (required)\",\"default\":{}},{\"name\":\"upstream_data\",\"type\":[\"null\",\"bytes\"],\"doc\":\"Data which caused the error (optional)\",\"default\":null},{\"name\":\"upstream_url\",\"type\":[\"null\",\"string\"],\"doc\":\"URL which caused the error (optional)\",\"default\":null},{\"name\":\"upstream_response\",\"type\":[\"null\",\"string\"],\"doc\":\"Response from the upstream system (optional)\",\"default\":null},{\"name\":\"kafka_cluster\",\"type\":[\"null\",\"string\"],\"doc\":\"Intended kafka cluster (optional)\",\"default\":null},{\"name\":\"success_topic\",\"type\":[\"null\",\"string\"],\"doc\":\"Intended topic for success (optional)\",\"default\":null},{\"name\":\"error_topic\",\"type\":[\"null\",\"string\"],\"doc\":\"Intended topic for error (optional)\",\"default\":null}],\"aliases\":[\"BaseError\"]},{\"type\":\"record\",\"name\":\"ExampleTwoError\",\"doc\":\"Example Error Model which comes from a sink system. This error comes from a\\n     Sink which reads from a Topic, using the record to perform an action.\",\"fields\":[{\"name\":\"timestamp\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"},\"doc\":\"The timestamp of when the error occurred, in Unix epoch time UTC (required)\"},{\"name\":\"correlation_id\",\"type\":{\"type\":\"string\",\"logicalType\":\"uuid\"},\"doc\":\"Unique id for event tracing (required)\"},{\"name\":\"environment_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Environment where the error took place (optional)\",\"default\":null},{\"name\":\"application_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Application where the error took place (optional)\",\"default\":null},{\"name\":\"instance_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Instance where the error took place (optional)\",\"default\":null},{\"name\":\"metadata\",\"type\":[\"null\",{\"type\":\"map\",\"values\":\"string\"}],\"doc\":\"Metadata from the source system (optional)\",\"default\":null},{\"name\":\"data\",\"type\":[\"null\",\"bytes\"],\"doc\":\"Data from the source system (optional)\",\"default\":null},{\"name\":\"downstream_metadata\",\"type\":[\"null\",{\"type\":\"map\",\"values\":\"string\"}],\"doc\":\"(optional) Metadata from the source system. Can be missing\",\"default\":null},{\"name\":\"original_record\",\"type\":\"bytes\",\"doc\":\"(required) original record which caused the error.\"},{\"name\":\"related_data\",\"type\":{\"type\":\"map\",\"values\":\"bytes\"},\"default\":{}},{\"name\":\"upstream_topic\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"upstream_cluster\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"source_topic\",\"type\":[\"null\",\"string\"],\"default\":null}],\"aliases\":[\"BaseError\"]},{\"type\":\"record\",\"name\":\"NormalizedError\",\"doc\":\"Example Normalized Error Model to which all other errors are (re)formatted.\",\"fields\":[{\"name\":\"timestamp\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"},\"doc\":\"The timestamp of when the error occurred, in Unix epoch time UTC (required)\"},{\"name\":\"event_datetime\",\"type\":{\"type\":\"string\",\"logicalType\":\"datetime\"},\"doc\":\"The datetime of when the error occured, in GMT (required)\"},{\"name\":\"correlation_id\",\"type\":{\"type\":\"string\",\"logicalType\":\"uuid\"},\"doc\":\"Unique id for event tracing (required)\"},{\"name\":\"environment_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Environment where the error took place (optional)\",\"default\":null},{\"name\":\"application_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Application where the error took place (optional)\",\"default\":null},{\"name\":\"instance_id\",\"type\":[\"null\",\"string\"],\"doc\":\"Instance where the error took place (optional)\",\"default\":null},{\"name\":\"metadata\",\"type\":[\"null\",{\"type\":\"map\",\"values\":\"string\"}],\"doc\":\"Metadata from the source system (optional)\",\"default\":null},{\"name\":\"data\",\"type\":[\"null\",\"bytes\"],\"doc\":\"Data from the source system (optional)\",\"default\":null},{\"name\":\"kafka_metadata\",\"type\":{\"type\":\"map\",\"values\":\"string\"},\"doc\":\"Metadata from the Kafka Record (required)\",\"default\":{}},{\"name\":\"kafka_record\",\"type\":{\"type\":\"map\",\"values\":\"bytes\"},\"doc\":\"The Record responsible for the error (required)\",\"default\":{}},{\"name\":\"kafka_schema\",\"type\":{\"type\":\"map\",\"values\":\"string\"},\"doc\":\"Schema of the Record (required)\",\"default\":{}},{\"name\":\"kafka_data\",\"type\":{\"type\":\"map\",\"values\":\"bytes\"},\"doc\":\"Additional data from Kafka (required)\",\"default\":{}},{\"name\":\"related_metadata\",\"type\":{\"type\":\"map\",\"values\":\"string\"},\"doc\":\"Additional metadata from rel","ated systems (required)\",\"default\":{}},{\"name\":\"related_data\",\"type\":{\"type\":\"map\",\"values\":\"bytes\"},\"doc\":\"Additional data from related systems (required)\",\"default\":{}}],\"aliases\":[\"BaseError\"]}],\"messages\":{}}");

  /** * A protocol defining multiple Record types in a single file, which makes it
 * easier to bundle related Records, and to version schema changes across multiple
 * record types.
 *
 * Design decision: A protocol defines more than just avro types, it defines an RPC exchange
 *   of information between systems. This includes 3 things:
 *     1. Record Types - The data normally exchanged by protocol exchanges
 *     2. Error Types  - The data returned by the protocol methods upon an error
 *     3. RPC Methods  - The method calls provided in the generated client library
 *
 *   In this example, we are exploring patterns for best aligning a variety of error messages
 *   from other systems, not from an avro protocol. Unfortunately, our "Error" types are the
 *   "normal" records exchanged by this protocol, not the protocol errors.
 *
 *   Overloading the type names is regrettably confusing, this example is about demonstrating
 *   how to use an .avdl (Avro Domain-Specific Language) protocol/file to bundle related schemas
 *   together in a more easily managed (and versioned) way.
 *
 *   One of the best parts of this file is that it supports comments (like this one), which is
 *   not (easily) possible in .avsc files, which are just JSON. */
  @org.apache.avro.specific.AvroGenerated
  public interface Callback extends NormalizedErrorProtocol {
    public static final org.apache.avro.Protocol PROTOCOL = io.firkin.kstreams.normalizer.errors.v1.avdl.NormalizedErrorProtocol.PROTOCOL;
  }
}