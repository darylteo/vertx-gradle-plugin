package com.darylteo.vertx.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec

import com.darylteo.vertx.gradle.deployments.Deployment

class RunVertx extends JavaExec {
  Deployment deployment

  def deployment(Deployment deployment) {
    this.deployment = deployment
  }

  @Override
  public void exec() {
    def items = this.deployment.modules

    def config = getVertxPlatformDependencies(project, this.deployment.platform.version)

    classpath += config
    main  = 'org.vertx.java.platform.impl.cli.Starter'
    args 'version'

    if(this.deployment.debug) {
      jvmArgs '-Xdebug', '-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044'
    }

    super.exec()
  }

  private Configuration getVertxPlatformDependencies(Project project, String version) {
    project.with {
      def configName = '__platform'
      def config = configurations.create configName

      dependencies.add(configName, "io.vertx:vertx-platform:$version") {
        exclude group: 'log4j', module: 'log4j'
      }

      return config
    }
  }
}
