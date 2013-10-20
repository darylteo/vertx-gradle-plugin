package com.darylteo.vertx.gradle.configuration

import org.gradle.api.Project

class PlatformConfiguration {
  def Project project
  def String version

  PlatformConfiguration(Project project) {
    this.project = project
  }

  def version(String version) {
    this.setVersion(version)
  }

  def setVersion(String version) {
    this.version = version
    this.lang('java', version)
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
        project.apply plugin: language
      }

      // verticles needing lang-modules need them in mod.json
      project.vertx.config.include "io.vertx~lang-${language}~${version}"
      
      // FIXME: Temporary hack until lang-groovy correctly pulls appropriate groovy jars
      if(language == 'groovy') {
        project.dependencies.vertxcore('org.codehaus.groovy:groovy-all:2.1.5')
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
