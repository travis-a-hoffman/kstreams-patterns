package io.firkin.kstreams.normalizer.utils;

import com.github.javafaker.Faker;

import org.apache.avro.data.RecordBuilder;

public class DataGenUtils {

  private static final Faker faker;
  static {
    faker = new Faker();
  }

  public static Faker faker() {
    return faker;
  }
}
