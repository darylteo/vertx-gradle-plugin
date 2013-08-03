package com.darylteo.gradle.plugins.vertx.handlers

import groovy.json.JsonBuilder

import com.darylteo.gradle.plugins.vertx.deployments.VertxDeploymentsContainer

public class VertxPropertiesHandler {
  /* public properties */
  String version = '+'
  String language = 'java'

  /* Hidden */
  private JsonBuilder _config = new JsonBuilder()
  public void config(Closure closure) {
    this._config.call(closure)
  }

  public def getConfig(){
    return this._config.content
  }

  private final VertxDeploymentsContainer deployments = new VertxDeploymentsContainer()
  public void deployments(Closure closure) {
    closure.setDelegate(deployments);
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.call(deployments);
  }
}