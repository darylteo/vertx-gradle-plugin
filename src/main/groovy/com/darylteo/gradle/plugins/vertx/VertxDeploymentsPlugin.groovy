package com.darylteo.gradle.plugins.vertx;

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment
import com.darylteo.gradle.plugins.vertx.deployments.VertxDeploymentItem
import com.darylteo.gradle.plugins.vertx.deployments.VertxDeploymentsContainer
import com.darylteo.gradle.plugins.vertx.deployments.VertxProjectDeploymentItem
import com.darylteo.gradle.plugins.vertx.tasks.VertxRun

public class VertxDeploymentsPlugin implements Plugin<Project> {
  public void apply(Project project) {
    project.with {
      convention.plugins.VertxDeploymentPlugin = new VertxDeploymentPluginConvention(it)

      // Adding deployment tasks
      afterEvaluate {
        project.deployments?.each { VertxDeployment dep ->
          task("run-${dep.name}", type: VertxRun) {
            group = 'Deployment'

            deployment = dep
            
            // adding copymod dependencies on other projects
            // since we are running inside a afterEvaluate,
            // there is a chance that a deployed project may have 
            // already been evaluated. This is why the presence of 
            // copyMod task is checked first, indicated an evaluated
            // project. 
            dep
              .findAll { VertxDeploymentItem module -> module instanceof VertxProjectDeploymentItem }
              .collect { VertxProjectDeploymentItem module -> module.project }
              .each { Project subproject ->
                if(subproject.copyMod) {
                  dependsOn subproject.copyMod
                } else {
                  subproject.afterEvaluate { dependsOn subproject.copyMod }
                }
              }

          }
        }
      }
    }
  }

  private class VertxDeploymentPluginConvention {
    private Project project
    private VertxDeploymentsContainer deployments

    public VertxDeploymentPluginConvention(Project project) {
      this.project = project
      this.deployments = new VertxDeploymentsContainer(project)
    }

    public void deployments(Closure closure) {
      closure.delegate = this.deployments
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure.call this.deployments
    }

    public VertxDeploymentsContainer getDeployments(){
      return this.deployments
    }
  }
}
