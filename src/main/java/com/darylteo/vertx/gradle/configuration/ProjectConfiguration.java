package com.darylteo.vertx.gradle.configuration;

import com.darylteo.vertx.gradle.deployments.Deployment;
import groovy.lang.Closure;
import groovy.util.Node;
import groovy.util.NodeList;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import java.io.File;

public class ProjectConfiguration {
  private final Project project;
  private final Node _info = new Node(null, "info");

  private final PlatformConfiguration platform;
  private final ModuleConfiguration config;
  private final ClusterConfiguration cluster;

  private final NamedDomainObjectContainer<Deployment> deployments;

  public ProjectConfiguration(Project project) {
    this.project = project;

    this.platform = new PlatformConfiguration(project);
    this.config = new ModuleConfiguration(project);
    this.cluster = new ClusterConfiguration();

    this.deployments = this.project.container(Deployment.class);
  }

  public Project getProject() {
    return this.project;
  }

  public PlatformConfiguration getPlatform() {
    return this.platform;
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

  public void platform(Action<PlatformConfiguration> action) {
    action.execute(this.platform);
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

  public NodeList getInfo() {
    return (NodeList) _info.children();
  }

  // TODO: Regression
  public void info(Closure closure) {
//    // hack for appending closure to child nodes
//    Node root = new Node(null, "temp");
//    Node empty = new Node(root, "empty");
//
//    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
//
//    // all top level children must be unique
//    empty.plus(closure);// append in front
//    root.remove(empty);

    // merge top level nodes into _info
//    for (Node section : root.children()) {
//      Object name = section.name();
//      Object list = _info.get(name);
//
//      if (list[0]) {
//        section.children().each { def element ->
//          if (element instanceof Node) {
//            list[0].append element
//          } else {
//            list[0].setValue(element.toString())
//          }
//        }
//      } else {
//        _info.append section
//      }
//    }

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
