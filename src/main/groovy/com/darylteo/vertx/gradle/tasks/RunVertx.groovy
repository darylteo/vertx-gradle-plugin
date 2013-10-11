package com.darylteo.vertx.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.Task
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
    def version = this.deployment.platform.version ?: this.project.vertx.platform.version
    def config = getVertxPlatformDependencies(project, version)
    def modules = this.deployment.deploy.module
    def item = modules instanceof Project ? modules.vertx.vertxName : (modules as String)

    // set classpath to run
    classpath += config
    main  = 'org.vertx.java.platform.impl.cli.Starter'
    args 'runMod', item

    // set stdio
    this.standardInput = System.in
    this.standardOutput = System.out

    // environment variables
    project.rootProject.with {
      workingDir projectDir
      systemProperties 'vertx.mods': "$buildDir/mods"
    }

    if(this.deployment.debug) {
      this.ignoreExitValue = true
      jvmArgs "-agentlib:jdwp=transport=dt_socket,address=localhost,server=y,suspend=y"
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
