package com.darylteo.gradle.plugins.vertx.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment
import com.darylteo.gradle.plugins.vertx.deployments.impl.DeploymentRunner
import com.darylteo.gradle.plugins.vertx.deployments.impl.DeploymentRunnerFactory


class VertxRunTask extends DefaultTask {
  public VertxDeployment deployment = null
  public String version = '2.0.0-final'

  @TaskAction
  def run(){
    if(!deployment) {
      return
    }

    def platformVersion = this.deployment.version ?: this.version

    DeploymentRunner runner = (new DeploymentRunnerFactory(platformVersion)).runner
    runner.run(this.deployment)
  }
}
