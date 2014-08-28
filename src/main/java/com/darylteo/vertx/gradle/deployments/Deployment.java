package com.darylteo.vertx.gradle.deployments;

import com.darylteo.vertx.gradle.tasks.VertxRun;
import groovy.json.JsonBuilder;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

public class Deployment {
  private final String name;
  private final Map config;
  private final PlatformConfiguration platform;
  private boolean isDebug = false;

  private DeploymentItem deploy;
  private VertxRun runTask;

  public Deployment(String name) {
    this.name = name;
    this.config = new HashMap<>();
    this.platform = new PlatformConfiguration();
  }

  public String getName() {
    return name;
  }

  public Map getConfig() {
    return config;
  }

  public PlatformConfiguration getPlatform() {
    return platform;
  }

  public boolean getIsDebug() {
    return isDebug;
  }

  public void setIsDebug(boolean debug) {
    this.isDebug = debug;
  }

  public DeploymentItem getDeploy() {
    return deploy;
  }

  public void setDeploy(DeploymentItem deploy) {
    this.deploy = deploy;
  }

  public VertxRun getRunTask() {
    return this.runTask;
  }

  public void setRunTask(VertxRun runTask) {
    this.runTask = runTask;
  }

  public void config(Closure data) {
    JsonBuilder builder = new JsonBuilder();
    config(DefaultGroovyMethods.asType(builder.call(data), Map.class));
  }

  public void config(Map data) {
    DefaultGroovyMethods.leftShift(this.config, data);
  }

  public void debug(boolean debug) {
    this.isDebug = debug;
  }

  public void deploy(Project project, Closure closure) {
    this.deploy(project, 1, closure);
  }

  public void deploy(Project project) {
    deploy(project, null);
  }

  public void deploy(String notation, Closure closure) {
    this.deploy(notation, 1, closure);
  }

  public void deploy(String notation) {
    deploy(notation, null);
  }

  public void deploy(Project project, int instances, Closure closure) {
    this.deploy = new DeploymentItem(this, project, closure);
  }

  public void deploy(Project project, int instances) {
    deploy(project, instances, null);
  }

  public void deploy(String notation, int instances, Closure closure) {
    this.deploy = new DeploymentItem(this, notation, closure);
  }

  public void deploy(String notation, int instances) {
    deploy(notation, instances, null);
  }

  public void platform(Closure closure) {
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.setDelegate(this.platform);
    closure.call(this.platform);
  }

}
