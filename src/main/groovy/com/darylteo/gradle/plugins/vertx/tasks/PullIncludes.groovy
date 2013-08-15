package com.darylteo.gradle.plugins.vertx.tasks;

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.darylteo.gradle.plugins.vertx.deployments.Platform
import com.darylteo.gradle.plugins.vertx.deployments.PlatformFactory

public class PullIncludes extends DefaultTask {
  @TaskAction
  public void run(){
    Platform platform = (new PlatformFactory(project)).runner
    platform.install(project.vertx.config?.includes)
  }

  String[] parse(String modules) {
    if(!modules) {
      return []
    }

    return modules.split("\\s*,\\s*")
  }

  String[] parse(String[] modules) {
    return modules
  }
}
