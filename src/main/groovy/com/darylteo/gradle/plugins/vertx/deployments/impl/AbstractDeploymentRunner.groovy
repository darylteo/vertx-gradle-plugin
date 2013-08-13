package com.darylteo.gradle.plugins.vertx.deployments.impl


import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment

abstract class AbstractDeploymentRunner implements DeploymentRunner {

  private final Object mutex = new Object()
  protected final String version

  public AbstractDeploymentRunner(String version) {
    this.version = version
  }

  public void run(VertxDeployment deployment) {
    beforeRun(deployment)

    println "Deploying ${deployment.project.name} deployment '${deployment.name}'."
    doRun(deployment)
    afterRun(deployment)

    synchronized(this.mutex) {
      this.mutex.wait();
    }
  }

  public void dryRun(VertxDeployment deployment) {
    beforeRun(deployment)
    doRun(deployment)
    afterRun(deployment)
  }

  public void beforeRun(VertxDeployment deployment) {
  }

  public void afterRun(VertxDeployment deployment) {
  }

  
  public void complete() {
    println 'Deployment Complete. Press Ctrl/Command + C to stop.'  
  }
  
  public void abort() {
    synchronized(this.mutex) {
      this.mutex.notifyAll();
    }
  }

  protected abstract void doRun(VertxDeployment deployment)
}
