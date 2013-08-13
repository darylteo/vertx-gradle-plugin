package com.darylteo.gradle.plugins.vertx.deployments;

import groovy.json.JsonBuilder

public abstract class VertxDeploymentItem {
  private JsonBuilder _config = new JsonBuilder()
  public int instances = 1

  public VertxDeploymentItem(int instances = 1) {
    this.instances = instances
  }

  public void config(Closure closure) {
    this._config.call(closure)
  }

  public def getConfig() {
    return this._config
  }

  public abstract String getNotation()
}
