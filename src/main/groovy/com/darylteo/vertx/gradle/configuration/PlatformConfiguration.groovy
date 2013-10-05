package com.darylteo.vertx.gradle.configuration

class PlatformConfiguration {
  def language
  def version
  def toolsVersion

  def tools(String version) {
    this.toolsVersion = version
  }

  def methodMissing(String name, args) {
    if(args.size() == 1) {
      this.language = name
      this.version = args[0]
    } else {
      throw new MissingMethodException(name, this.class, args)
    }
  }
}
