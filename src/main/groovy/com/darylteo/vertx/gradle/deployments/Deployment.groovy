package com.darylteo.vertx.gradle.deployments

import groovy.json.JsonBuilder

import org.gradle.api.Project

class Deployment {
  final String name

  final Map config
  final PlatformConfiguration platform

  boolean debug = false

  DeploymentItem deploy

  public Deployment(String name){
    this.name = name
    this.config = [:] as Map
    this.platform = new PlatformConfiguration()
  }

  def config(Closure data) {
    JsonBuilder builder = new JsonBuilder()
    config(builder.call(data) as Map)
  }

  def config(Map data) {
    this.config << data
  }

  def debug(boolean debug) {
    this.debug = debug
  }

  def deploy(Project project, Closure closure = null) {
    this.deploy(project, 1, closure)
  }

  def deploy(String notation, Closure closure = null) {
    this.deploy(notation, 1, closure)
  }

  def deploy(Project project, int instances, Closure closure = null) {
    this.deploy = new DeploymentItem(this, project, closure)
  }

  def deploy(String notation, int instances, Closure closure = null) {
    this.deploy = new DeploymentItem(this, notation, closure)
  }

  def platform(Closure closure) {
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.delegate = this.platform
    closure(this.platform)
  }
}
