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
      }
    }

    project.with {
      apply plugin: 'maven'
      apply plugin: 'signing'

      // default values to satisfy compiler
      if (!hasProperty('sonatypeUsername')){
        ext.sonatypeUsername = ''
      }

      if (!hasProperty('sonatypePassword')){
        ext.sonatypePassword = ''
      }

      configurations {
        mavenArchives
      }

      // This is to clear any jars output by the project
      task('setupArchives') {
        configurations.archives.artifacts.clear()
        artifacts {
          archives modZip
        }
      }

      test {
        dependsOn 'copyMod'

        // Make sure tests are always run!
        outputs.upToDateWhen { false }

        // Show output
        testLogging.showStandardStreams = true

        testLogging { exceptionFormat "full" }
      }

      // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
      // maven task configuration

      ext.isReleaseVersion = !version.endsWith("SNAPSHOT")

      signing {
        required { isReleaseVersion && gradle.taskGraph.hasTask("uploadArchives") }
        sign configurations.archives
      }

      install {
        repositories.mavenInstaller {
          configurePom(pom)
        }
      }

      uploadArchives {
        group 'build'
        description = "Does a maven deploy of archives artifacts"
        dependsOn 'setupArchives'

        repositories {
          mavenDeployer {
            setUniqueVersion(false)

            configuration = configurations.archives

            repository(url: "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
              authentication(userName: sonatypeUsername, password: sonatypePassword)
            }

            snapshotRepository(url: "https://oss.sonatype.org/content/repositories/snapshots/") {
              authentication(userName: sonatypeUsername, password: sonatypePassword)
            }

            if (isReleaseVersion) {
              beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }
            }

            configurePom(pom)
          }
        }
      }


    }
  }
}

class Console {
  String readLine(String message){
    print (message)
    print "test\n"
    return "test"
  }

  String readPassword(String message){
    print (message)
    print "pass\n"
    return "pass"
  }
}