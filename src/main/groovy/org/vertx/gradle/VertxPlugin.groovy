package org.vertx.gradle

import org.gradle.api.*

class VertxPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.ext.vertx = true
  }

  private void setupDependencies(Project project) {
    project.allprojects {
      dependencies {
        provided "io.vertx:vertx-core:$vertxVersion"
        provided "io.vertx:vertx-platform:$vertxVersion"
        testCompile "junit:junit:$junitVersion"
        testCompile "io.vertx:testtools:$toolsVersion"
      }
    }
  }
}