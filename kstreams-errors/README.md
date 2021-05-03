# KStream Demos for Error Cases

## Building

```shell
$> mvn clean package
```

## From the command line

```shell
# Run the demo
$> java -jar target/kstreams-errors-0.1-SNAPSHOT-jar-with-dependencies.jar <subcommand>

# TODO enable compiling using native-image 
$> ksdemos

```

## Helpers for the demos

* Patterns for [testing kafka streams](https://www.confluent.io/blog/testing-kafka-streams/)

* A Library for spinning up [kafka broker container](https://www.testcontainers.org/modules/kafka/) from within junit test cases: 

* Example for spinning up a full [kafka cluster](https://github.com/testcontainers/testcontainers-java/tree/master/examples/kafka-cluster/src/test/java/com/example/kafkacluster) from test code.



