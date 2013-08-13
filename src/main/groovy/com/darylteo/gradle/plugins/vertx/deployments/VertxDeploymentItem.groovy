package com.darylteo.gradle.plugins.vertx.deployments;

import groovy.json.JsonBuilder

public class VertxDeploymentItem {
  private JsonBuilder _config = new JsonBuilder()
  public int instances = 1

  public VertxDeploymentItem(int instances = 1) {
    this.instances = instances
  }

  def config(Closure closure) {
    this._config.call(closure)
  }

  def getConfig() {
    return this._config
  }
}
