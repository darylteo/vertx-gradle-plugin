package com.darylteo.gradle.plugin.vertx.resolvers

import groovyx.net.http.HTTPBuilder

import java.util.concurrent.atomic.AtomicBoolean

class VertxMavenResolver {
  public VertxMavenResolver(String repo, String group, String artifact, String version) {
  }

  private boolean checkURL(String url, String path) {
    AtomicBoolean result = new AtomicBoolean(false);

    Thread.start {
      try {
        new HTTPBuilder(url).get(path) {
          response ->
          synchronized(result) {
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

    synchronized(result) {
      result.wait()
    }

    return result.value;
  }
}
