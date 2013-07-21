# Vert.x Gradle Plugin

The Vert.x Gradle Plugin (unofficial) is a plugin for rapidly building 
Vert.x modules.

## Sample Buildscript

Start off with this as your buildscript.

```groovy
import com.darylteo.gradle.plugins.*
import com.darylteo.gradle.plugins.vertx.*

buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.darylteo:maven-plugin:1.1.0'
  }
}

// apply the following plugins

apply plugin: VertxProjectPlugin // a normal vert.x project
apply plugin: VertxPublishPlugin // preps this module for maven deployment. Optional

// configure your project
vertx {
  version = '2.0.0-final' // the version of vert.x to use
  language = 'java' // or 'groovy', or 'scala' (not supported by vert.x yet)
  
  config {
    // these go into mod.json
    main = 'MainVerticle'
    includes = 'some~vertx-module~1.0.0'
  }
}

````
