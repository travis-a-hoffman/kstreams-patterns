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

package io.firkin.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.io.IOException;

import static io.firkin.containers.ContainerUtils.CP_SCHEMAREGISTRY;
import static io.firkin.kstreams.utils.TestUtils.CONFLUENT_VERSION;

public class SchemaRegistryContainer extends GenericContainer<SchemaRegistryContainer> {
  private static final int SCHEMA_REGISTRY_INTERNAL_PORT = 8081;

  private final String networkAlias = "schema-registry";

  public SchemaRegistryContainer(String zookeeperConnect) throws IOException {
    this(CONFLUENT_VERSION, zookeeperConnect);
  }

  public SchemaRegistryContainer(String confluentVersion, String zookeeperConnect) throws IOException {
    super(getSchemaRegistryContainerImage(confluentVersion));

    addEnv("SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL", zookeeperConnect);
    addEnv("SCHEMA_REGISTRY_HOST_NAME", "localhost");

    withExposedPorts(SCHEMA_REGISTRY_INTERNAL_PORT);
    withNetworkAliases(networkAlias);

    waitingFor(Wait.forHttp("/subjects"));
  }

  public String getUrl() {
    return String.format("http://%s:%d", this.getContainerIpAddress(), this.getMappedPort(SCHEMA_REGISTRY_INTERNAL_PORT));
  }


  private static String getSchemaRegistryContainerImage(String confluentPlatformVersion) {
    return (String) TestcontainersConfiguration.getInstance().getProperties()
        .getOrDefault(
            "schemaregistry.container.image",
            CP_SCHEMAREGISTRY + ":" + confluentPlatformVersion
        );
  }
}
