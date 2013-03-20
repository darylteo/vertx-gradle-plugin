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
package com.darylteo.gradle

import org.gradle.api.*
import org.gradle.api.logging.*;
import org.gradle.api.artifacts.maven.*;

public class MavenSettings implements Plugin<Project> {
  void apply(Project project) {
    def configurePom = { def pom ->
      if(project.hasProperty('artifact')){
        pom.artifactId = project.artifact
      }

      if (project.hasProperty('configurePom')){
        project.configurePom(pom)
      } else {
        println("$project does not provide a configurePom(). Maven validation may fail when attempting to close a staged artifact.")
      }
    }

    project.with {
      apply plugin: 'maven'
      apply plugin: 'signing'

      loadDefaults(it)

      configurations {
        archives
      }

      install {
        repositories.mavenInstaller {
          configurePom(pom)
        }
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

            configurePom(pom)
          }
        }
      }
    }
  }

  def loadDefaults(Project project){
    (
      [
        sonatypeUsername: '',
        sonatypePassword: ''
      ]
    ).each { def k,v ->
      if (!project.hasProperty(k) && !project.ext.hasProperty(k)){
        project.ext[k] = v
      }
    }
  }
}