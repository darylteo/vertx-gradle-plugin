package com.darylteo.gradle.plugins.vertx.tasks;

import groovy.json.JsonSlurper

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.darylteo.gradle.plugins.vertx.deployments.DeploymentRunner;
import com.darylteo.gradle.plugins.vertx.deployments.DeploymentRunnerFactory;

public class PullIncludes extends DefaultTask {
  @TaskAction
  public void run(){
    //    installModules(project.vertx?.config?.include)

    DeploymentRunner runner = (new DeploymentRunnerFactory(project)).runner
    runner.run(project.moduleName)
  }

  void installModules(String[] modules) {
    def slurper = new JsonSlurper()

    modules.each { module ->
      println "Installing $module"

      try {
        installModule(module)
        println "$module pulled in successfully"

        //        this.project.rootProject.file("mods/$module/mod.json").withReader { reader->
        //          def json = slurper.parse(reader)
        //
        //          // json.includes can either be a string or an array of strings
        //          installModules(json.includes)
        //        }
      }catch(Exception e) {
        println "$module did not install successfully"
        e.printStackTrace()
      }
    }
  }

  void installModules(String modules) {
    if(!modules) {
      return
    }

    installModules(modules.split("\\s*,\\s*"))
  }

  void installModule(def module) {
    println "INSTALLING MODULE $module"
  }
}
