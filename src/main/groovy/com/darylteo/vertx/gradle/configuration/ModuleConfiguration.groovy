package com.darylteo.vertx.gradle.configuration

import org.gradle.api.Project

class ModuleConfiguration {
  private Project project

  public Map map

  public ModuleConfiguration(Project project) {
    this.project = project
    this.map = [:]
  }
  
  public def main(String value) {
    map.main = value
  }

  public def worker(boolean value) {
    map.worker = value
  }

  public def multiThreaded(boolean value) {
    map['multi-threaded'] = value
  }

  public def preserveCwd(boolean value) {
    map['preserve-cwd'] = value
  }

  public def autoRedeploy(boolean value) {
    map['auto-redeploy'] = value
  }

  public def resident(boolean value) {
    map.resident = value
  }

  public def system(boolean value) {
    map.system = value
  }

  public def includes(String ... includes) {
    if(!map.includes) {
      map.includes = []
    }

    map.includes.addAll(includes)
  }

  public def deploys(String ... deploys) {
    if(!map.deploys) {
      map.deploys = []
    }

    map.deploys << deploys
  }
}
