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

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

public abstract class DemoGeneratorUtils {

  private static String[] envList = {"SBX", "DEV", "SQA", "TST", "UAT", "PRD"};

  private static long defaultSeed = -1L;
  private static Random defaultRandom = new Random(defaultSeed);

  private long seed;
  private Random random;

  private DemoGeneratorUtils() {
    this(-1L);
  }

  protected DemoGeneratorUtils(long seed) {
    this.seed = seed;
    this.random = new Random(seed);
  }

  public long seed() {
    return this.seed;
  }

  public Random random() {
    return this.random;
  }

  public static String envId() {
    return envList[defaultRandom.nextInt(envList.length)];
  }

//  public Environment environment() {
//    return envList[defaultRandom.nextInt(envList.length)];
//  }

  private static final int DEF_ALPHA_ID_STRING_LENGTH = 6;
  private static final int MAX_ALPHA_ID_STRING_LENGTH = 24;
  private static final int MIN_ALPHA_ID_STRING_LENGTH = 4;

  private static final int DEF_NUMERIC_ID_STRING_LENGTH = 3;
  private static final int MAX_NUMERIC_ID_STRING_LENGTH = 8;
  private static final int MIN_NUMERIC_ID_STRING_LENGTH = 1;

  public static int fitToRange(int value, int min, int max) {
    return Math.min(max, Math.max(min, value));
  }

  public static String idString() {
    return randomAlphanumeric(DEF_ALPHA_ID_STRING_LENGTH).toLowerCase();
  }

  public static String idString(int length) {
    return randomAlphanumeric(fitToRange(length, MIN_ALPHA_ID_STRING_LENGTH, MAX_ALPHA_ID_STRING_LENGTH)).toLowerCase();
  }

  public static String idString(String prefix) {
    if (prefix.contains(" ")) throw new IllegalArgumentException();
    return prefix + "-" + idString();
  }

  public static String idString(String prefix, int length) {
    if (prefix.contains(" ")) throw new IllegalArgumentException();
    return prefix + "-" + randomAlphanumeric(length).toLowerCase();
  }

  public static String idString(String prefix, String suffix) {
    if (suffix.contains(" ")) throw new IllegalArgumentException();
    return idString(prefix) + "-" + suffix;
  }

  public static String instanceId() {
    return randomNumeric(DEF_NUMERIC_ID_STRING_LENGTH);
  }

  public static String instanceId(String prefix) {
    if (prefix.contains(" ")) throw new IllegalArgumentException();
    return prefix + randomNumeric(DEF_NUMERIC_ID_STRING_LENGTH);
  }

  public static String instanceId(String prefix, int length) {
    if (prefix.contains(" ")) throw new IllegalArgumentException();
    return prefix + randomNumeric(fitToRange(length, MIN_NUMERIC_ID_STRING_LENGTH, MAX_NUMERIC_ID_STRING_LENGTH));
  }

  public static String uuid() {
    return UUID.randomUUID().toString();
  }

  public static String httpResponse() {
    return "";
  }

  /*
   * These methods could be defined with Generics, but we'd have to provide 9(!) types.
   *
    public class DemoGeneratorUtils<SRC_DATA, SRC_META, SRC_RESP, RCD_DATA, RCD_KEY, RCD_META, SNK_DATA, SNK_META, SNK_RESP> {
      public SRC_DATA sourceDataObject();
      public SRC_META sourceMetadataObject();
      public SRC_RESP sourceResponseObject();
      public SNK_DATA sinkDataObject();
      public SNK_META sinkMetadataObject();
      public SNK_RESP sinkResponseObject();
      public RCD_DATA recordDataObject();
      public RCD_KEY  recordKeyObjet();
      public RCD_META recordMetadataObject();
    }
   *
   */

  /**
   *
   * @return non-null buffer containing the raw bytes
   */
  public ByteBuffer sourceDataBytes() {
    return ByteBuffer.allocate(0);
  }

  /**
   *
   * @return non-null buffer containing the raw bytes
   */
  public ByteBuffer sourceMetadataBytes() {
    return ByteBuffer.allocate(0);
  }

  /**
   *
   * @return non-null buffer containing the raw bytes
   */
  public ByteBuffer sourceResponseBytes() {
    return ByteBuffer.allocate(0);
  }

  /**
   *
   * @return non-null buffer containing the raw bytes
   */
  public ByteBuffer recordDataBytes() {
    return ByteBuffer.allocate(0);
  }

  /**
   *
   * @return non-null buffer containing the raw bytes
   */
  public ByteBuffer recordKeyBytes() {
    return ByteBuffer.allocate(0);
  }

  /**
   *
   * @return non-null buffer containing the raw bytes
   */
  public ByteBuffer recordMetadataBytes() {
    return ByteBuffer.allocate(0);
  }

  /**
   *
   * @return non-null buffer containing the raw bytes
   */
  public ByteBuffer sinkDataBytes() {
    return ByteBuffer.allocate(0);
  }

  /**
   *
   * @return non-null buffer containing the raw bytes
   */
  public ByteBuffer sinkMetadataBytes() {
    return ByteBuffer.allocate(0);
  }

  /**
   *
   * @return non-null buffer containing the raw bytes
   */
  public ByteBuffer sinkResponseBytes() {
    return ByteBuffer.allocate(0);
  }

  /**
   *
   * @return non-null string containing the json string
   */
  protected String toJsonString(Object data) {
    return "{}"; // Always return an empty JSON object.
  }

  /**
   *
   * @param data
   * @return
   */
  protected byte[] toJsonBytes(Object data) {
    return new byte[0];
  }

  /**
   *
   * @param data
   * @return
   */
  protected byte[] toAvroBytes(Object data) {
    return new byte[0];
  }

  /**
   *
   * @param data
   * @return
   */
  protected byte[] toProtobufBytes(Object data) {
    return new byte[0];
  }
}
