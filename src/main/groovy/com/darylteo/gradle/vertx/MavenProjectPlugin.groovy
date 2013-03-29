/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package com.darylteo.gradle.vertx

import org.gradle.api.*
import org.gradle.api.logging.*
import org.gradle.api.artifacts.maven.*

public class MavenProjectPlugin implements org.gradle.api.Plugin<Project> {
  void apply(Project project) {
    project.with {
      // apply required plugins
      apply plugin: 'maven'
      apply plugin: 'signing'

      loadDefaults(it)

      configurations { archives }

      install {
        repositories.mavenInstaller { configurePom(project, pom) }
      }

      signing {
        required { release && gradle.taskGraph.hasTask("uploadArchives") }
        sign configurations.archives
      }

      uploadArchives {
        group 'build'
        description = "Does a maven deploy of archives artifacts"

        repositories {
          mavenDeployer {
            if (release) {
              beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }
            }

            setUniqueVersion(false)

            configuration = configurations.archives

            repository(url: "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
              authentication(userName: sonatypeUsername, password: sonatypePassword)
            }

            snapshotRepository(url: "https://oss.sonatype.org/content/repositories/snapshots/") {
              authentication(userName: sonatypeUsername, password: sonatypePassword)
            }

            configurePom(project, pom)
          }
        }
      }

    } // end .with
  }

  def configurePom(Project project, def pom) {
    if(project.hasProperty('artifact')){
      pom.artifactId = project.artifact
    }

    if (project.hasProperty('configurePom')){
      project.configurePom(pom)
    } else {
      println("$project does not provide a configurePom(). Maven validation may fail when attempting to close a staged artifact.")
    }
  }

  def loadDefaults(Project project){
    project.defaults (
      sonatypeUsername: '',
      sonatypePassword: ''
    )
  }
}