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
package org.vertx.gradle

import org.gradle.api.*
import org.gradle.api.logging.*;
import org.gradle.api.artifacts.maven.*;

public class MavenSettings implements Plugin<Project> {
  void apply(Project project) {
    def sonatypeUsername, sonatypePassword

    project.with {
      apply plugin: 'maven'
      apply plugin: 'signing'

      task('getCredentials') << {
        def console = System.console()
        if (!console) {
          console = new Console()
        }

        if (!hasProperty('sonatypeUsername')) {
          sonatypeUsername = console.readLine('Enter Sonatype Username: ')
        } else {
          sonatypeUsername = project.sonatypeUsername
        }

        if (!hasProperty('sonatypePassword')) {
          sonatypePassword = console.readPassword("Enter Sonatype Password for $sonatypeUsername: ")
        } else {
          sonatypePassword = project.sonatypePassword
        }
      }

      artifacts {
        archives modZip
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

      uploadArchives {
        group 'build'
        description = "Does a maven deploy of archives artifacts"
        dependsOn 'getCredentials'

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

            if (project.hasProperty('configurePom')){
              project.configurePom(pom)
            }
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