package com.darylteo.vertx.gradle.deployments

class Deployment {
  final String name
  boolean debug = false

  public Deployment(String name){
    this.name = name
  }

  def debug(boolean debug) {
    this.debug = debug
  }
}
