package com.darylteo.vertx.gradle.configuration;

import org.gradle.api.Project;

import java.util.*;

public class ModuleConfiguration {
  private Map<String, Object> map;
  private Project project;

  public ModuleConfiguration(Project project) {
    this.project = project;
    this.map = new HashMap<>();
  }

  public void main(String value) {
    map.put("main", value);
  }

  public void worker(boolean value) {
    map.put("worker", value);
  }

  public void multiThreaded(boolean value) {
    map.put("multi-threaded", value);
  }

  public void preserveCwd(boolean value) {
    map.put("preserve-cwd", value);
  }

  public void autoRedeploy(boolean value) {
    map.put("auto-redeploy", value);
  }

  public void resident(boolean value) {
    map.put("resident", value);
  }

  public void system(boolean value) {
    map.put("system", value);
  }

  public void includes(String... includes) {
    if (map.containsKey("includes") || map.get("includes") == null) {
      map.put("includes", new LinkedList<String>());
    }

    ((List<String>) map.get("includes")).addAll(Arrays.asList(includes));
  }

  public void deploys(String... deploys) {
    if (map.containsKey("deploys") || map.get("deploys") == null) {
      map.put("deploys", new LinkedList<String>());
    }

    ((List<String>) map.get("deploys")).addAll(Arrays.asList(deploys));
  }

  public List<String> getIncludes() {
    return map.containsKey("includes") ? (List<String>) map.get("includes)") : new ArrayList<String>();
  }
}
