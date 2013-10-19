package com.darylteo.vertx.gradle.tasks

import groovy.json.JsonOutput
import groovy.xml.QName

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateModJson extends DefaultTask {
  def destinationDir = { "${project.buildDir}/conf" }

  public GenerateModJson() {
    project.afterEvaluate {
      inputs.property 'config', project.vertx.config
      inputs.property 'info', project.vertx.info

      def dir = project.file(this.destinationDir)
      outputs.file "$dir/mod.json"
    }
  }

  public File getDestinationDir() {
    return project.file(destinationDir)
  }
  
  @TaskAction
  def run() {
    def destDir = project.file(destinationDir)
    def modjson = project.file("$destDir/mod.json")
    destDir.mkdirs()
    modjson.delete()

    // http://vertx.io/mods_manual.html
    def data = [:]

    // module info
    // description, licenses, author, keywords, developers, homepage
    this.insert(data, 'description', project.vertx.info.description[0]?.value())
    this.insert(data, 'licenses', project.vertx.info.licenses[0]?.license.collect { it.name[0].value() })

    // need to use get() for properties, to bypass getProperties() method
    def props = project.vertx.info.getAt(QName.valueOf('properties'))[0]
    if(props) {
      def keywords = props.keywords[0]?.value().split('\\s*,\\s*')
      this.insert(data, 'keywords', keywords)
    }

    def developers = project.vertx.info.developers[0]?.developer
    if(developers) {
      if(developers.size() > 0) {
        insert data, 'author', developers[0]?.name[0]?.value()
      }
      if(developers.size() > 1) {
        def others = (developers.collect { it.name[0].value() })
        others.remove(0)
        insert data, 'developers', others
      }
    }

    insert data, 'homepage', project.vertx.info.url[0]?.value()

    // override with module config
    // main, worker, multi-threaded, includes, preserve-cwd, auto-redeploy, resident, system, deploys
    def config = project.vertx.config
    // hack until vertx does supports arrays for includes
    if(!config.includes instanceof String) {
      config.includes = config.includes.join(',')
    }

    project.vertx.config.each { prop ->
      this.insert(data, prop.key, prop.value)
    } 


    modjson << JsonOutput.toJson(data)
  }

  void insert(def map, String key, def value) {
    if(value) {
      if(key) {
        key = key.replaceAll(~/\p{Lu}\p{Ll}*/){ String word ->
          return "-${word.toLowerCase()}"
        }
      }
      map."$key" = value
    }
  }
}
