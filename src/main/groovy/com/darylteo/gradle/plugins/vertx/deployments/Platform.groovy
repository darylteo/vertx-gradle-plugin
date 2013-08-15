package com.darylteo.gradle.plugins.vertx.deployments;


public interface Platform {
  void run(VertxDeployment deployment);
  void install(def module);
}
