package com.darylteo.vertx.gradle.tasks;

import com.darylteo.vertx.gradle.deployments.Deployment;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.JavaExec;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class RunVertx extends JavaExec {
  private Deployment deployment;
  private File configFile;

  public RunVertx() {
    this.setGroup("Vertx Run");
  }

  public Deployment getDeployment() {
    return deployment;
  }

  public void setDeployment(Deployment deployment) {
    this.deployment = deployment;
  }

  public File getConfigFile() {
    return configFile;
  }

  public void setConfigFile(File configFile) {
    this.configFile = configFile;
  }

  public Deployment deployment(Deployment deployment) {
    return this.deployment = deployment;
  }

  public File configFile(File configFile) {
    return this.configFile = configFile;
  }

  @Override
  public void exec() {
//    String version = this.deployment.getPlatform().getVersion();
//
//    if (version == null || version.isEmpty()) {
//      logger.error 'Vertx Platform Version not defined for this deployment'
//      throw new DeploymentVersionNotSetException()
//    }
//
//    def confDirs = project.rootProject.files('conf')
//    def module = this.deployment.deploy.module
//    def platform = this.deployment.platform
//    def moduleName = module instanceof Project ? module.vertx.vertxName : (module as String)
//
//    def deploymentClasspath = getPlatformDependencies(project, version, platform.classpath)
//
//    // set classpath to run
//    classpath += deploymentClasspath + confDirs
//
//    this.main = 'org.vertx.java.platform.impl.cli.Starter'
//
//    // running a module
//    args 'runMod', moduleName
//
//    // with these platform arguments
//    args platform.args
//
//    // and config file configuration
//    if (deployment.platform.conf != null) {
//      args '-conf', "$deployment.platform.conf"
//    } else {
//      args '-conf', project.file(configFile).toString()
//    }
//
//    // set stdio
//    this.standardInput = System.in
//    this.standardOutput = System.out
//
//    // environment variables
//    project.rootProject.with {
//      workingDir projectDir
//      systemProperties 'vertx.mods': "$buildDir/mods"
//    }
//
//    if (this.deployment.debug) {
//      this.ignoreExitValue = true
//      jvmArgs "-agentlib:jdwp=transport=dt_socket,address=localhost,server=y,suspend=y"
//    }
//
//    println "Running $this"
//
//    super.exec()
  }

  private Configuration getPlatformDependencies(Project project, String version, String... paths) {
    DependencyHandler dependencyHandler = project.getDependencies();
    List<Dependency> deps = new LinkedList<>();

    for (String path : paths) {
      deps.add(dependencyHandler.create(path));
    }

    deps.add(dependencyHandler.create("io.vertx:vertx-platform:" + version));
    // TODO: exclude log4j

    return project.getConfigurations().detachedConfiguration(deps.toArray(new Dependency[deps.size()]));
  }
}
