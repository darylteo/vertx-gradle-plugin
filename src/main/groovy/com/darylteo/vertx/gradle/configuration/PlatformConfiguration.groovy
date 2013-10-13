package com.darylteo.vertx.gradle.configuration

import org.gradle.api.Project

class PlatformConfiguration {
  def Project project

  PlatformConfiguration(Project project) {
    this.project = project
  }

  def tools(String version) {
    project.dependencies.vertxtest("io.vertx:testtools:${version}")
  }

  def lang(String language, String version) {
    if(language == 'java') {
      project.dependencies.vertxcore("io.vertx:vertx-platform:${version}") {
        exclude group:'log4j', module:'log4j'
      }
    } else {
      if (language in ['groovy', 'scala']){
        project.apply plugin: platform.language
      }

      project.dependencies.vertxcore("io.vertx:lang-${language}:${version}"){
        exclude group:'log4j', module:'log4j'
      }
    }
  }

  def methodMissing(String name, args) {
    if(args.size() == 1) {
      lang(name, args[0])
    } else {
      throw new MissingMethodException(name, this.class, args)
    }
  }
}
