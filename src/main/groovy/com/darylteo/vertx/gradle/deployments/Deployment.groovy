package com.darylteo.vertx.gradle.deployments

import groovy.json.JsonBuilder

import org.gradle.api.Project

class Deployment {
  final String name

  final Map config
  boolean debug = false

  final List<DeploymentItem> modules = []

  public Deployment(String name){
    this.name = name
    this.config = [:] as Map
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
    def item = new DeploymentItem(this, project, closure)
    modules << item
  }

  def deploy(String notation, int instances, Closure closure = null) {
    def item = new DeploymentItem(this, notation, closure)
    modules << item
  }
}
