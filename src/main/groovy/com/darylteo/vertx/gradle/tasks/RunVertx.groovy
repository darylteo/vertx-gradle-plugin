package com.darylteo.vertx.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec

import com.darylteo.vertx.gradle.deployments.Deployment
import com.darylteo.vertx.gradle.exceptions.DeploymentVersionNotSetException

class RunVertx extends JavaExec {
  Deployment deployment
  def deployment(Deployment deployment) {
    this.deployment = deployment
  }

  public RunVertx() {
    this.group = 'Vertx Run'
  }
  
  @Override
  public void exec() {
    def version = this.deployment.platform.version

    if(!version) {
      logger.error 'Vertx Platform Version not defined for this deployment'
      throw new DeploymentVersionNotSetException()
    }

    def config = getVertxPlatformDependencies(project, version)
    def module = this.deployment.deploy.module
    def platform = this.deployment.platform
    def moduleName = module instanceof Project ? module.vertx.vertxName : (module as String)

    // set classpath to run
    classpath += project.rootProject.files('conf')
    classpath += config
    main  = 'org.vertx.java.platform.impl.cli.Starter'
    args 'runMod', moduleName
    args(platform.args)

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

    println "Running $this"
    
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
