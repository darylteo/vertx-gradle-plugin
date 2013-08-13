package com.darylteo.gradle.plugins.vertx;

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment
import com.darylteo.gradle.plugins.vertx.deployments.VertxDeploymentItem
import com.darylteo.gradle.plugins.vertx.deployments.VertxDeploymentsContainer
import com.darylteo.gradle.plugins.vertx.deployments.VertxModuleDeploymentItem
import com.darylteo.gradle.plugins.vertx.tasks.VertxRunTask

public class VertxDeploymentsPlugin implements Plugin<Project> {
  public void apply(Project project) {
    project.with {
      convention.plugins.VertxDeploymentPlugin = new VertxDeploymentPluginConvention(it)

      // Adding deployment tasks
      afterEvaluate {
        project.deployments?.each { VertxDeployment dep ->
          task("run-${dep.name}", type: VertxRunTask) {
            deployment = dep
            group = 'Deployment '
            dependsOn {
              dep
                .findAll { VertxDeploymentItem module ->
                  return module instanceof VertxModuleDeploymentItem
                }
                .collect { VertxDeploymentItem module ->
                  return project.project(module.notation).copyMod
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
