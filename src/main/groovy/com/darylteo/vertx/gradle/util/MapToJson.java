package com.darylteo.vertx.gradle.util;

import groovy.json.JsonBuilder;

import java.util.Map;

/**
 * Created by dteo on 10/05/2014.
 */
public class MapToJson {
  public static String convert(Map map) {
    JsonBuilder json = new JsonBuilder();
    json.call(map);
    return json.toPrettyString();
  }

}
