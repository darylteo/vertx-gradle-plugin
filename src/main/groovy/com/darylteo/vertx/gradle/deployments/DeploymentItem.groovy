package com.darylteo.vertx.gradle.deployments

import groovy.json.JsonBuilder

import org.gradle.api.Project

class DeploymentItem {
  final Deployment deployment
  final Map config
  final def module

  DeploymentItem(Deployment deployment, def module, Closure closure = null) {
    this.deployment = deployment
    this.module = module
    this.config = [:] as Map
    this.config << closureToMap(closure)
  }

  def config(Map data) {
    this.config << (data as Map)
  }

  def getEffectiveConfig() {
    def map = [:]

    map << this.deployment.config
    map << this.config
  }
  
  private Map closureToMap(Closure closure) {
    if(!closure) {
      return [:]
    }

    JsonBuilder builder = new JsonBuilder()
    return builder.call(closure)
  }
}
