package com.darylteo.gradle.plugins.vertx;

public class VertxPropertiesHandler {
  /* public properties */
  String version = '+';
  String language = 'java';

  /* Hidden */
  private final Map config = [:]
  void config(Closure closure) {
    closure.setDelegate(config);
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.call(config);
  }
}