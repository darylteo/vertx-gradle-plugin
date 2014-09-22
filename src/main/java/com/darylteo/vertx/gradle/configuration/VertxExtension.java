package com.darylteo.vertx.gradle.configuration;

import com.darylteo.vertx.gradle.deployments.Deployment;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import java.io.File;

public class VertxExtension {
  private final Project project;

  private final ModuleInformation info;
  private final VertxPlatformConfiguration platform;
  private final ModuleConfiguration config;
  private final ClusterConfiguration cluster;

  private final NamedDomainObjectContainer<Deployment> deployments;

  public VertxExtension(Project project) {
    this.project = project;

    this.info = new ModuleInformation();

    this.platform = new VertxPlatformConfiguration(project);
    this.config = new ModuleConfiguration();
    this.cluster = new ClusterConfiguration();

    this.deployments = this.project.container(Deployment.class);
  }

  public Project getProject() {
    return this.project;
  }

  public ModuleInformation getInfo() {
    return this.info;
  }

  public ModuleConfiguration getConfig() {
    return this.config;
  }

  public ClusterConfiguration getCluster() {
    return this.cluster;
  }

  public NamedDomainObjectContainer<Deployment> getDeployments() {
    return this.deployments;
  }

  public VertxPlatformConfiguration getPlatform() {
    return this.platform;
  }

  public void config(Action<ModuleConfiguration> action) {
    action.execute(this.config);
  }

  public void cluster(Action<ClusterConfiguration> action) {
    action.execute(this.cluster);
  }

  public void deployments(Action<DomainObjectCollection<Deployment>> action) {
    action.execute(this.deployments);
  }

  public void platform(Action<VertxPlatformConfiguration> action) {
    action.execute(this.platform);
  }

  public void info(Closure closure) {
    this.info.call(closure);
  }

  public String getVertxName() {
    String group = project.getGroup() == null ? "group" : project.getGroup().toString();
    String name = project.getName();
    String version = project.getVersion().toString();

    return group + "~" + name + "~" + version;
  }

  public String getMavenName() {
    String group = project.getGroup().toString();
    String name = project.getName();
    String version = project.getVersion().toString();

    return group + ":" + name + ":" + version;
  }

  public File getModuleDir() {
    return project.file(project.getRootProject().getBuildDir() + "/mods/" + getVertxName());
  }
}
