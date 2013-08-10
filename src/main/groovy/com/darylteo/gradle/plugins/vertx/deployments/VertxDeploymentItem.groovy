package com.darylteo.gradle.plugins.vertx.deployments;

import groovy.json.JsonBuilder

public class VertxDeploymentItem {
  private JsonBuilder _config = new JsonBuilder()

  public String notation = ''
  public int instances = 1

  public VertxDeploymentItem(String notation) {
    this(notation, 1)
  }
  public VertxDeploymentItem(String notation, int instances) {
    this.instances = instances
    this.notation = notation
  }

  def config(Closure closure) {
    this._config.call(closure)
  }

  def getConfig() {
    return this._config
  }
}
