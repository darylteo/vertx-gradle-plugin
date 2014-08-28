package com.darylteo.vertx.gradle.tasks;

import com.darylteo.vertx.gradle.configuration.ProjectConfiguration;
import com.darylteo.vertx.gradle.deployments.Deployment;
import com.darylteo.vertx.gradle.deployments.DeploymentItem;
import com.darylteo.vertx.gradle.deployments.PlatformConfiguration;
import com.darylteo.vertx.gradle.plugins.VertxPlugin;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.JavaExec;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Configuration Files - put in the 'conf' directory of this task's project.
 * </p>
 */
public class VertxRun extends JavaExec {
  private Deployment deployment;
  private File configFile;

  public VertxRun() {
  }

  public Deployment getDeployment() {
    return deployment;
  }

  public void setDeployment(Deployment deployment) {
    this.deployment = deployment;
  }

  public void deployment(Action<Deployment> action) {
    if (this.deployment == null) {
      this.deployment = new Deployment();
    }

    action.execute(this.deployment);
  }

  @Override
  public void exec() {
    ConfigurableFileCollection confDirs = this.getProject().files("conf");

    // validate configuration of task
    if (this.deployment == null) {
      this.getLogger().error(this + " does not have a deployment set");
      throw new GradleException();
    }

    // validate vert.x version
    PlatformConfiguration platform = this.deployment.getPlatform();
    String version = platform.getVersion();

    if (version == null || version.isEmpty()) {
      this.getLogger().error("Vertx Platform Version not defined for this deployment");
      throw new GradleException(this + " has not been properly configured with a vert.x platform configuration");
    }

    // validate module deployment
    DeploymentItem deploymentItem = this.deployment.getDeploymentItem();

    if (deploymentItem == null) {
      throw new GradleException(this + " has not been properly configured with a deployment item");
    }

    // test if project deployment or external deployment
    Object module = deploymentItem.getModule();
    String moduleName = module.toString();

    if (module instanceof Project) {
      moduleName = this.getModuleName((Project) module);
    }

    // configure JavaExec
    Configuration deploymentClasspath = getPlatformDependencies(version);

    this.setClasspath(
      this.getClasspath()
        .plus(deploymentClasspath)
        .plus(confDirs)
    );

    this.setMain("org.vertx.java.platform.impl.cli.Starter");

    // running a module
    List<String> args = this.getArgs();
    args.addAll(Arrays.asList("runMod", moduleName));

    // with these platform arguments
    args.addAll(platform.getArgs());

    // and config file configuration
    if (platform.getConf() != null) {
      args.addAll(Arrays.asList("-conf", platform.getConf().toString()));
    } else if (this.configFile != null) {
      args.addAll(Arrays.asList("-conf", configFile.toString()));
    }

    this.setArgs(args);

    // set stdio
    this.setStandardInput(System.in);
    this.setStandardOutput(System.out);

    // environment variables
    this.setWorkingDir(getProject().getRootDir());
    this.systemProperty("vertx.mods", getProject().getRootProject().getBuildDir() + "/mods");

    if (this.deployment.getIsDebug()) {
      this.setIgnoreExitValue(true);
      this.setJvmArgs(Arrays.asList("-agentlib:jdwp=transport=dt_socket,address=localhost,server=y,suspend=y"));
    }

    super.exec();
  }

  private String getModuleName(Project project) {
    if (!project.getPlugins().hasPlugin(VertxPlugin.class)) {
      throw new GradleException("Project " + project + " does not have Vert.x Plugin applied and cannot be deployed by Vert.x");
    }

    return project.getExtensions().getByType(ProjectConfiguration.class).getVertxName();
  }

  private Configuration getPlatformDependencies(String version, String... paths) {
    DependencyHandler dependencyHandler = getProject().getDependencies();
    List<Dependency> deps = new LinkedList<>();

    for (String path : paths) {
      deps.add(dependencyHandler.create(path));
    }

    deps.add(dependencyHandler.create("io.vertx:vertx-platform:" + version));
    // TODO: exclude log4j

    return this.getProject().getConfigurations().detachedConfiguration(deps.toArray(new Dependency[deps.size()]));
  }
}
