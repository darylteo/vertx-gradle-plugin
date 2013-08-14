package com.darylteo.gradle.plugins.vertx.deployments.impl


import org.gradle.api.Project

import com.darylteo.gradle.plugins.vertx.deployments.DeploymentRunner;
import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment

abstract class AbstractDeploymentRunner implements DeploymentRunner {

  private final Object mutex = new Object()

  protected final String version
  protected final Project project

  public AbstractDeploymentRunner(Project project, String version) {
    this.version = version
    this.project = project
  }

  public void run(VertxDeployment deployment) {
    println "Deploying ${deployment.project.name} deployment '${deployment.name}' using vert.x-${version}."
    beforeRun()

    def incomplete = [] as Set

    deployment.each { dep ->
      incomplete.add(dep)
      this.deploy(dep.notation, dep.instances, dep.config.toString()) { result ->
        if(result.success) {
          println "${dep.instances} of ${dep.notation} deployed"
          incomplete.remove(dep)

          if(incomplete.empty) {
            println 'Deployment Complete. Press Ctrl/Command + C to stop.'
          }
        } else {
          println "${dep.notation} failed to deploy"
          result.exception.printStackTrace()

          this.unlock()
        }
      }
    }

    this.lock()
  }

  public void run(String module) {
    beforeRun()

    this.deploy(module, 1, '{}') { result ->
      if(result.success) {
        println "${module} deployed"
      } else {
        println "${module} failed to deploy"
        result.exception.printStackTrace()
      }

      this.unlock()
    }

    this.lock()
  }

  protected void beforeRun() {
  }
  protected abstract void deploy(String module, int instances, String config, Closure callback)

  void lock() {
    synchronized(this.mutex){
      this.mutex.wait()
    }
  }

  void unlock() {
    synchronized(this.mutex){
      this.mutex.notifyAll()
    }
  }
}
