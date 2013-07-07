package com.darylteo.gradle.plugins.vertx;

import groovyx.net.http.HTTPBuilder

import java.util.concurrent.atomic.AtomicBoolean

public class VertxModuleResolver {
  private String mavenCentral = 'http://repo1.maven.org/'

  public String getIdentifier(String notation) {
    def (group,name,version) = notation.split("~")

    getIdentifier(group,name,version)
  }

  public String getIdentifier(String group, String name, String version) {
    String result = checkCache(group,name,version)
    if(result) {
      return result
    }

    def group_url = group.replace('.','/');
    def nomod = "maven2/${group_url}/${name}-${version}.zip"
    def withmod = "maven2/${group_url}/${name}-${version}-mod.zip"

    //TODO: Cache result on file
    println "Check $withmod"
    if(checkURL(withmod)) {
      println "Pass"
      return "$group:$name:$version:mod@zip"
    }else{
      println "Fail"
      return "$group:$name:$version@zip"
    }
  }

  public String checkCache(String group, String name, String version) {
    null
  }

  public boolean checkURL(String url) {
    AtomicBoolean result = new AtomicBoolean(false);

    Thread.start {
      try {
        new HTTPBuilder($mavenCentral).get( path:url ) { response ->
          synchronized(result) {
            println "${response.statusLine}"
            result.set(response.statusLine.statusCode  == 200)
            result.notifyAll()
          }
        }
      }
      catch( e ) {
        synchronized(result) {
          result.notifyAll()
        }
      }
    }
    
    println "Wait"
    synchronized(result) {
      result.wait()
    }
    println "Result ${result}"

    return result.value;
  }
}
