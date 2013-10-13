package com.darylteo.vertx.gradle.deployments

class PlatformConfiguration {
  String version
  
  def version(String version) {
    this.version = version
  }
}
