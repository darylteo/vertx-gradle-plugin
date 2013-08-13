package com.darylteo.gradle.plugins.vertx.tasks;

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment
import com.darylteo.gradle.plugins.vertx.deployments.impl.DeploymentRunner
import com.darylteo.gradle.plugins.vertx.deployments.impl.DeploymentRunnerFactory

public class VertxInstallModsTask extends DefaultTask {
  public VertxDeployment deployment = null
  public String version = '2.0.0-final'

  @TaskAction
  def run(){
    if(!deployment) {
      return
    }

    DeploymentRunner runner = (new DeploymentRunnerFactory(this.version)).runner
    runner.run(this.deployment)
  }
}
