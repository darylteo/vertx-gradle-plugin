package com.darylteo.vertx.gradle.deployments;

import groovy.json.JsonBuilder;
import groovy.lang.Closure;
import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Module can be
 * <ul>
 * <li>String - a Maven artifact identifier</li>
 * <li>Project - a Gradle Project that has Vert.x plugin</li>
 * </ul>
 * </p>
 */
public class DeploymentItem {
  private final Deployment deployment;
  private final Map<String, Object> config;
  private final Object module;

  public DeploymentItem(Deployment deployment, Project project, Closure closure) {
    this.deployment = deployment;
    this.module = project;
    this.config = new HashMap<String, Object>();
    this.config.putAll(closureToMap(closure));

  }

  public DeploymentItem(Deployment deployment, String module, Closure closure) {
    this.deployment = deployment;
    this.module = module;
    this.config = new HashMap<String, Object>();
    this.config.putAll(closureToMap(closure));
  }

  public Deployment getDeployment() {
    return deployment;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public Object getModule() {
    return module;
  }

  public void config(Map data) {
    this.config.putAll(data);
  }

  public void getEffectiveConfig() {
    Map<String, Object> map = new HashMap<String, Object>();

    map.putAll(this.deployment.getConfig());
    map.putAll(this.config);
  }

  private Map closureToMap(Closure closure) {
    if (closure == null) {
      return new HashMap();
    }

    JsonBuilder builder = new JsonBuilder();
    return ((Map) (builder.call(closure)));
  }
}
