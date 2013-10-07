package com.darylteo.vertx.gradle.configuration


class ModuleConfiguration {
  // module information
  def group
  void group(def group) {
    this.group = group
  }
  
  def name
  void name(def name) {
    this.name = name
  }
  
  def version
  void version(def version) {
    this.version = version
  }
  
  def getVertxName() {
    return "$group~$name~$version" 
  }
  
  def getMavenName() {
    return "$group:$name:$version"
  }
}
