package com.darylteo.vertx.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.darylteo.vertx.gradle.deployments.Deployment

class RunVertx extends DefaultTask {
  def version
  Deployment deployment

  def version(String version) {
    this.version = version
  }

  def deployment(Deployment deployment) {
    this.deployment = deployment
  }

  @TaskAction
  def run() {
    def items = this.deployment.modules

    println this.deployment.config
  }
}
