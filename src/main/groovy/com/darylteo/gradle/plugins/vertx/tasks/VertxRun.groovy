package com.darylteo.gradle.plugins.vertx.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.darylteo.gradle.plugins.vertx.deployments.Platform
import com.darylteo.gradle.plugins.vertx.deployments.PlatformFactory
import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment

class VertxRun extends DefaultTask {
  public VertxDeployment deployment = null
  public String version = null

  @TaskAction
  def run(){
    if(!deployment) {
      return
    }

    def platformVersion = this.deployment.version ?: this.version ?: 'unspecified'

    Platform runner = (new PlatformFactory(project, platformVersion)).runner
    runner.run(this.deployment)
  }
}
