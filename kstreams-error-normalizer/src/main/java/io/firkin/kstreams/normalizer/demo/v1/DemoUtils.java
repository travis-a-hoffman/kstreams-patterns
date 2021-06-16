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

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DemoUtils {

  // coordinate with value of PLAINTEXT_HOST in kstreams-patterns/kafka-cluster/docker-compose.xml
  public static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
  private static final String DEFAULT_SCHEMA_REGISTRY_URL = "http://schema-registry:8081";
  private static final String SCHEMA_REGISTRY_URL = "KAFKA_CONFLUENT_SCHEMA_REGISTRY_URL";

  // ---- Kafka Client Utilities ------------------------------------------------------------------

  public static Admin kafkaAdminClient(Properties properties) {
    return AdminClient.create(
        Map.of(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
            properties.getProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                DEFAULT_BOOTSTRAP_SERVERS)
        ));
  }

  public static SchemaRegistryClient schemaRegistryClient(Properties properties) {
    return new CachedSchemaRegistryClient(
        properties.getProperty(SCHEMA_REGISTRY_URL, DEFAULT_SCHEMA_REGISTRY_URL),
        256);
  }


  // ---- Properties File Utilities ---------------------------------------------------------------

  public static Properties loadEnvProperties(final String fileName) throws IOException {
    final Properties envProps = new Properties();
    final FileInputStream input = new FileInputStream(fileName);
    envProps.load(input);
    input.close();
    return envProps;
  }

  public static Map<String, Object> propertiesToMap(final Properties props) {
    final Map<String, Object> rv = new HashMap<>();
    final Enumeration<?> names = props.propertyNames();
    while (names.hasMoreElements()) {
      final String key = (String)names.nextElement();
      rv.put(key, props.getProperty(key));
    }
    return rv;
  }

  /**
   * Create a temporary directory. The directory and any contents will be deleted when the test
   * process terminates.
   */
  public static File tempDirectory() {
    final File file;
    try {
      file = Files.createTempDirectory("confluent").toFile();
    } catch (final IOException ex) {
      throw new RuntimeException("Failed to create a temp dir", ex);
    }
    file.deleteOnExit();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          delete(file);
        } catch (IOException e) {
          System.out.println("Error deleting " + file.getAbsolutePath());
        }
      }
    });

    return file;
  }

  /**
   * Recursively delete the given file/directory and any subfiles (if any exist)
   *
   * @param file The root file at which to begin deleting
   */
  public static void delete(final File file) throws IOException {
    if (file == null) {
      return;
    }
    Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
        // If the root path did not exist, ignore the error; otherwise throw it.
        if (exc instanceof NoSuchFileException && path.toFile().equals(file)) {
          return FileVisitResult.TERMINATE;
        }
        throw exc;
      }

      @Override
      public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        Files.delete(path);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException {
        Files.delete(path);
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
