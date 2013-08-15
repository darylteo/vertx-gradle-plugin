package com.darylteo.gradle.plugins.vertx.deployments.impl


import groovy.json.JsonSlurper

import org.gradle.api.Project

import com.darylteo.gradle.plugins.vertx.deployments.Platform
import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment

abstract class AbstractPlatform implements Platform {

  private final Object mutex = new Object()

  protected final String version
  protected final Project project

  public AbstractPlatform(Project project, String version) {
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

  public void install(def modules) {
    beforeRun()

    def queue = []
    if(modules instanceof String) {
      queue.addAll modules.split('\\s*,\\s*')
    } else if(modules != null) {
      queue.addAll modules
    }

    println queue.empty
    while(!queue.empty) {
      def module = queue.remove(0)

      this.installModule(module) { result ->
        if(result.success) {
          println "${module} installed: ${result.dir}"

          // TODO: slurp JSON and install other modules
          def slurp = new JsonSlurper()
          new File("${result.dir}/mod.json").withReader { reader ->
            def json = slurp.parse(reader)

            if(json.includes instanceof String) {
              queue.addAll(json.includes.split('\\s*,\\s*'))
            } else if(json.includes != null) {
              queue.addAll(json.includes)
            }
          }
        } else {
          println "${module} failed to deploy"
          result.exception.printStackTrace()
        }

        synchronized(queue) {
          queue.notify()
        }
      }

      synchronized(queue) {
        queue.wait()
      }
    }
  }

  protected void beforeRun() {
  }

  protected abstract void deploy(String module, int instances, String config, Closure callback)
  protected abstract void installModule(String module, Closure callback)

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
