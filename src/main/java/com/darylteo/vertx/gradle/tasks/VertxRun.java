package com.darylteo.vertx.gradle.tasks;

import com.darylteo.vertx.gradle.configuration.VertxExtension;
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

    // validate module deployment
    DeploymentItem deploymentItem = this.deployment.getDeploymentItem();

    if (deploymentItem == null) {
      throw new GradleException(this + " has not been properly configured with a deployment item");
    }

    // test if project deployment or external deployment
    Object module = deploymentItem.getModule();
    String moduleName = module.toString();

    if (module instanceof Project) {
      if (!((Project) module).getPlugins().hasPlugin(VertxPlugin.class)) {
        throw new GradleException("Project " + module + " does not have Vert.x Plugin applied and cannot be deployed by Vert.x");
      }

      moduleName = getModuleName((Project) module);
    }

    // validate vert.x version
    PlatformConfiguration platform = this.deployment.getPlatform();
    String version = platform.getVersion();

    // no deployment version specified, use project version
    if ((version == null || version.trim().isEmpty()) && module instanceof Project) {
      version = getVersion((Project) module);
    }

    if (version == null || version.trim().isEmpty()) {
      this.getLogger().error("Vertx Platform Version not defined for this deployment");
      throw new GradleException(this + " has not been properly configured with a vert.x platform configuration");
    }

    // configure JavaExec
    Configuration deploymentClasspath = getPlatformDependencies(version);

    this.classpath(deploymentClasspath, confDirs);

    this.setMain("org.vertx.java.platform.impl.cli.Starter");

    // running a module
    this.args("runMod", moduleName);

    // with these platform arguments
    this.args(platform.getArgs());

    // and config file configuration
    if (platform.getConf() != null) {
      this.args("-conf", platform.getConf().toString());
    } else if (this.configFile != null) {
      this.args("-conf", configFile.toString());
    }

    // set stdio
    this.setStandardInput(System.in);
    this.setStandardOutput(System.out);

    // environment variables
    this.setWorkingDir(getProject().getRootDir());
    this.systemProperty("vertx.mods", getProject().getRootProject().getBuildDir() + "/mods");

    super.exec();
  }

  private String getModuleName(Project project) {
    return project.getExtensions().getByType(VertxExtension.class).getVertxName();
  }

  private String getVersion(Project project) {
    return project.getExtensions().getByType(VertxExtension.class).getPlatform().getVersion();
  }

  private Configuration getPlatformDependencies(String version, String... paths) {
    DependencyHandler dependencyHandler = getProject().getDependencies();
    List<Dependency> deps = new LinkedList<>();

    deps.add(dependencyHandler.create("io.vertx:vertx-platform:" + version));
    // TODO: exclude log4j

    for (String path : paths) {
      deps.add(dependencyHandler.create(path));
    }

    return this.getProject().getConfigurations().detachedConfiguration(deps.toArray(new Dependency[deps.size()]));
  }
}
