package com.darylteo.vertx.gradle.deployments;

import groovy.json.JsonBuilder;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.ClosureBackedAction;

import java.util.HashMap;
import java.util.Map;

public class Deployment {
  private String name;
  private Map<String, Object> config;
  private PlatformConfiguration platform;

  private DeploymentItem deploymentItem;
  private String runTaskName;
  private String generateConfigTaskName;

  public Deployment() {
    this(null);
  }

  public Deployment(String name) {
    this.name = name;
    this.config = new HashMap<>();
    this.platform = new PlatformConfiguration();
  }

  public String getName() {
    return name;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public PlatformConfiguration getPlatform() {
    return platform;
  }

  public DeploymentItem getDeploymentItem() {
    return deploymentItem;
  }

  public String getRunTaskName() {
    return this.runTaskName;
  }

  public void setRunTaskName(String runTaskName) {
    this.runTaskName = runTaskName;
  }

  public String getGenerateConfigTaskName() {
    return generateConfigTaskName;
  }

  public void setGenerateConfigTaskName(String generateConfigTaskName) {
    this.generateConfigTaskName = generateConfigTaskName;
  }

  public void config(Closure closure) {
    JsonBuilder json = new JsonBuilder();
    this.config.putAll((Map) json.call(closure));
  }

  public void config(Map<String, Object> config) {
    this.config.putAll(config);
  }

  public void deploy(Project project) {
    this.deploy(project, 1, null);
  }

  public void deploy(Project project, Closure closure) {
    this.deploy(project, 1, closure);
  }

  public void deploy(Project project, int instances) {
    this.deploy(project, instances, null);
  }

  public void deploy(Project project, int instances, Closure closure) {
    this.deploymentItem = new DeploymentItem(this, project, closureToMap(closure));
  }

  public void deploy(String notation) {
    this.deploy(notation, 1, null);
  }

  public void deploy(String notation, Closure closure) {
    this.deploy(notation, 1, closure);
  }

  public void deploy(String notation, int instances) {
    this.deploy(notation, instances, null);
  }

  public void deploy(String notation, int instances, Closure closure) {
    this.deploymentItem = new DeploymentItem(this, notation, closureToMap(closure));
  }

  public void platform(Closure<PlatformConfiguration> closure) {
    closure.setDelegate(this.platform);
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    platform(new ClosureBackedAction<PlatformConfiguration>(closure));
  }

  public void platform(Action<PlatformConfiguration> action) {
    action.execute(this.platform);
  }

  private Map closureToMap(Closure closure) {
    if (closure == null) {
      return new HashMap();
    }

    JsonBuilder builder = new JsonBuilder();
    return ((Map) (builder.call(closure)));
  }
}
