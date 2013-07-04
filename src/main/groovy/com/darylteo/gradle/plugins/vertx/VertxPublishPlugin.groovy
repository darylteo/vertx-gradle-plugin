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
import org.vertx.java.core.Handler
import org.vertx.java.platform.PlatformLocator
import org.vertx.java.platform.impl.ModuleClassLoader

class VertxPublishPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.afterEvaluate{
      // Zipping up the module
      task('modZip', type: Zip, dependsOn: copyMod) {
        group = 'vert.x'
        description: 'Assemble the module into a zip file'

        destinationDir = file("$buildDir/libs")
        archiveName = "${name}-${version}.zip"

        from copyMod
      }
    }
  }
}