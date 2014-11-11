package com.darylteo.vertx.gradle.configuration;

import java.util.*;

public class ModuleConfiguration {
  private String main;

  private Boolean worker;
  private Boolean multiThreaded;
  private Boolean preserveCwd;
  private Boolean autoRedeploy;

  private Boolean resident;
  private Boolean system;

  private List<String> includes = new LinkedList<>();
  private List<String> deploys = new LinkedList<>();

  private boolean changed = false;
  private Map<String, Object> map;

  public void main(String value) {
    this.changed = true;
    this.main = value;
  }

  public void worker(boolean value) {
    this.changed = true;
    this.worker = value;
  }

  public void multiThreaded(boolean value) {
    this.changed = true;
    this.multiThreaded = value;
  }

  public void preserveCwd(boolean value) {
    this.changed = true;
    this.preserveCwd = value;
  }

  public void autoRedeploy(boolean value) {
    this.changed = true;
    this.autoRedeploy = value;
  }

  public void resident(boolean value) {
    this.changed = true;
    this.resident = value;
  }

  public void system(boolean value) {
    this.changed = true;
    this.system = value;
  }

  public void includes(String... includes) {
    this.changed = true;
    this.includes.addAll(Arrays.asList(includes));
  }

  public void deploys(String... deploys) {
    this.changed = true;
    this.deploys.addAll(Arrays.asList(deploys));
  }

  public List<String> getIncludes() {
    return this.includes;
  }

  public Map<String, Object> getMap() {
    if (!changed && this.map != null) {
      return this.map;
    }

    Map<String, Object> result = new HashMap<>();

    this.put(result, "main", this.main);
    this.put(result, "worker", this.worker);
    this.put(result, "multi-threaded", this.multiThreaded);
    this.put(result, "preserve-cwd", this.preserveCwd);
    this.put(result, "auto-redeploy", this.autoRedeploy);

    this.put(result, "resident", this.resident);
    this.put(result, "system", this.system);

    if (!this.includes.isEmpty()) {
      // note: vert.x is still stupid and doesn't acccept arrays for includes
      // and my PR was stale and I cbf to reupdate it all over again.
      // so this is here to solve that

      StringBuilder builder = new StringBuilder();
      List<String> clone = new ArrayList<>(this.includes);

      builder.append(clone.remove(0));
      while (!clone.isEmpty()) {
        builder.append(":");
        builder.append(clone.remove(0));
      }

      this.put(result, "includes", builder.toString());
    }

    if (!this.deploys.isEmpty()) {
      this.put(result, "deploys", deploys);
    }

    changed = false;

    return result;
  }

  private void put(Map<String, Object> map, String key, Object value) {
    if (value == null) {
      return;
    }

    map.put(key, value);
  }
}
