package io.firkin.containers;

import io.firkin.containers.SchemaRegistryContainer;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;

public class KafkaCluster implements Startable {

  private final int brokersNum;
  private final Network network;
  private final Collection<KafkaContainer> brokers;
  private final GenericContainer<?> schemaRegistry;
  private final GenericContainer<?> zookeeper;

  public KafkaCluster(String confluentPlatformVersion, int brokersNum, int internalTopicsRf) {
    if (brokersNum < 0) {
      throw new IllegalArgumentException("brokersNum '" + brokersNum + "' must be greater than 0");
    }
    if (internalTopicsRf < 0 || internalTopicsRf > brokersNum) {
      throw new IllegalArgumentException("internalTopicsRf '" + internalTopicsRf + "' must be less than brokersNum and greater than 0");
    }

    this.brokersNum = brokersNum;
    this.network = Network.newNetwork();

    this.zookeeper = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-zookeeper").withTag(confluentPlatformVersion))
        .withNetwork(network)
        .withNetworkAliases("zookeeper")
        .withEnv("ZOOKEEPER_CLIENT_PORT", String.valueOf(KafkaContainer.ZOOKEEPER_PORT));

//    this.schemaRegistry = new SchemaRegistryContainer(DockerImageName.parse("confluentinc/cp-schema-registry").withTag(confluentPlatformVersion))

    this.schemaRegistry = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-schema-registry").withTag(confluentPlatformVersion))
        .withNetwork(network)
        .withNetworkAliases("schemaregistry")
        .withEnv("SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL", zookeeper.getHost()+":2181")
        .withEnv("SCHEMA_REGISTRY_HOST_NAME", "localhost");

    this.brokers = IntStream
        .range(0, this.brokersNum)
        .mapToObj(brokerNum -> {
          return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka").withTag(confluentPlatformVersion))
              .withNetwork(this.network)
              .withNetworkAliases("broker-" + brokerNum)
              .dependsOn(this.zookeeper)
              .withExternalZookeeper("zookeeper:" + KafkaContainer.ZOOKEEPER_PORT)
              .withEnv("KAFKA_BROKER_ID", brokerNum + "")
              .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", internalTopicsRf + "")
              .withEnv("KAFKA_OFFSETS_TOPIC_NUM_PARTITIONS", internalTopicsRf + "")
              .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", internalTopicsRf + "")
              .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", internalTopicsRf + "");
        })
        .collect(Collectors.toList());

  }

  public Collection<KafkaContainer> getBrokers() {
    return this.brokers;
  }

  public String getBootstrapServers() {
    return brokers.stream()
        .map(KafkaContainer::getBootstrapServers)
        .collect(Collectors.joining(","));
  }

  private Stream<GenericContainer<?>> allContainers() {
    return Stream.concat(
        this.brokers.stream(),
        Stream.of(this.zookeeper, this.schemaRegistry)
    );
  }

  @Override
  public void start() {
    Stream<Startable> startables = this.brokers.stream().map(Startable.class::cast);
    try {
      Startables.deepStart(startables).get(60, SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      e.printStackTrace();
    }

    Unreliables.retryUntilTrue(30, SECONDS, () -> {
      Container.ExecResult result = this.zookeeper.execInContainer(
          "sh", "-c",
          "zookeeper-shell zookeeper:" + KafkaContainer.ZOOKEEPER_PORT + " ls /brokers/ids | tail -n 1"
      );
      String brokers = result.getStdout();

      return brokers != null && brokers.split(",").length == this.brokersNum;
    });
  }

  @Override
  public void stop() {
    allContainers().parallel().forEach(GenericContainer::stop);
  }
}