package com.darylteo.gradle.plugins.vertx.deployments;

import org.gradle.api.Project

import com.darylteo.gradle.plugins.vertx.deployments.impl.DefaultPlatform;

public class PlatformFactory {

  private final String version
  private final Project project

  public PlatformFactory(Project project) {
    this.version = project.vertx?.version
    this.project = project
  }


  public PlatformFactory(Project project, String version) {
    this.version = version
    this.project = project
  }

  public Platform getRunner() {
    // Future Work: if platform api changes, we'll need to detect which version we're trying to deploy on
    // and return the appropriate deployment runner
    return new DefaultPlatform(this.project, this.version)
  }
}
