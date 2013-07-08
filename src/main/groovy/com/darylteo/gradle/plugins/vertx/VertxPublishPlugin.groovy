/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.darylteo.gradle.plugins.vertx

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.file.*
import org.gradle.api.logging.*
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip

import com.darylteo.gradle.plugins.MavenPlugin

class VertxPublishPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.with {
      apply plugin: MavenPlugin;

      // Zipping up the module
      task('modZip', type: Zip, dependsOn: copyMod) {
        group = 'vert.x publishing'
        description: 'Assemble the module into a zip file'

        destinationDir = file("$buildDir/libs")
        classifier = 'mod'

        from copyMod
      }

      if(project.vertx?.config?.main) {
        configurations.archives.artifacts.clear()
      } else {
        task('sourcesJar', type: Jar, dependsOn: classes) {
          classifier = 'sources'
          sourceSets.all {  from allSource }
        }

        artifacts { archives sourcesJar }

        if(tasks.findByName('javadoc')) {
          task('javadocJar', type: Jar, dependsOn: javadoc) {
            classifier = 'javadoc'
            from javadoc.destinationDir
          }

          artifacts { archives javadocJar }
        }

        if(tasks.findByName('groovydoc')) {
          task('groovydocJar', type: Jar, dependsOn: groovydoc) {
            classifier = 'groovydoc'
            from groovydoc.destinationDir
          }

          artifacts { archives groovydocJar }
        }
      }

      artifacts { archives modZip }

    }
  }
}