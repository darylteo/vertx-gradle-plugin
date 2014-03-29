package com.darylteo.vertx.gradle.tasks

import com.darylteo.vertx.gradle.deployments.Deployment
import com.darylteo.vertx.gradle.exceptions.DeploymentVersionNotSetException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec

class RunVertx extends JavaExec {
  Deployment deployment
  def configFile

  def deployment(Deployment deployment) {
    this.deployment = deployment
  }

  def configFile(def configFile) {
    this.configFile = configFile
  }

  public RunVertx() {
    this.group = 'Vertx Run'
  }

  @Override
  public void exec() {
    def version = this.deployment.platform.version

    if (!version) {
      logger.error 'Vertx Platform Version not defined for this deployment'
      throw new DeploymentVersionNotSetException()
    }

    def confDirs = project.rootProject.files('conf')
    def module = this.deployment.deploy.module
    def platform = this.deployment.platform
    def moduleName = module instanceof Project ? module.vertx.vertxName : (module as String)

    def deploymentClasspath = getPlatformDependencies(project, version, platform.classpath)

    // set classpath to run
    classpath += deploymentClasspath + confDirs

    main = 'org.vertx.java.platform.impl.cli.Starter'

    // running a module
    args 'runMod', moduleName

    // with these platform arguments
    args platform.args

    // and also this configuration, if it was generated
    // this will appears AFTER the platform arguments, so if the platform
    // args also has a -conf parameter, the behavior of this is undefined.
    args '-conf', project.file(configFile).toString()

    // set stdio
    this.standardInput = System.in
    this.standardOutput = System.out

    // environment variables
    project.rootProject.with {
      workingDir projectDir
      systemProperties 'vertx.mods': "$buildDir/mods"
    }

    if (this.deployment.debug) {
      this.ignoreExitValue = true
      jvmArgs "-agentlib:jdwp=transport=dt_socket,address=localhost,server=y,suspend=y"
    }

    println "Running $this"

    super.exec()
  }

  private Configuration getPlatformDependencies(Project project, String version, def paths = []) {
    project.with {
      def deps = (paths + "io.vertx:vertx-platform:$version").collect { path ->
        project.dependencies.create(path) {
          exclude group: 'log4j', module: 'log4j'
        }
      }

      return configurations.detachedConfiguration(*deps)
    }
  }
}
