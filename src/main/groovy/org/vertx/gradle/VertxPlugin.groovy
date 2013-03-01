package org.vertx.gradle

import org.gradle.api.*

class VertxPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.ext.vertx = true

    project.subprojects { def child ->
      setupDependencies(child)
    }
  }

  private void setupDependencies(Project project) {
    project.with {
      apply plugin: 'java'
      apply plugin: 'groovy'
      apply plugin: 'scala'
      apply plugin: 'eclipse'
      apply plugin: 'idea'

      defaultTasks = ['assemble']

      sourceCompatibility = '1.7'
      targetCompatibility = '1.7'

      configurations {
        provided
        testCompile.extendsFrom provided
      }

      repositories {
        mavenLocal()
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
        mavenCentral()
      }

      dependencies {
        provided "io.vertx:vertx-core:${vertxVersion}"
        provided "io.vertx:vertx-platform:${vertxVersion}"
        testCompile "junit:junit:${junitVersion}"
        testCompile "io.vertx:testtools:${toolsVersion}"
      }

      sourceSets {
        main {
          compileClasspath = compileClasspath + configurations.provided
        }
      }
    }

    project.apply plugin: VertxModulePlugin
  }
}