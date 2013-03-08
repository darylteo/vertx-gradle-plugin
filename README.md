# Vert.x Gradle Template

Template project for creating a Vert.x module with a Gradle build.

Clone this and adapt it to easily develop Vert.x modules using Gradle as your build tool.

See the [build script](build.gradle) for the list of useful tasks

## Installation

Just place this line in your root project's build.gradle

```groovy
apply from: 'https://raw.github.com/darylteo/vertx-gradle-template/magic-apply/apply/all.gradle'
```

Done!

## Usage

````
Usage:

./gradlew task_name

(or gradlew.bat task_name if you have the misfortune to have to use Windows)

If no task name is specified then the default task 'assemble' is run

Task names are:

idea - generate a skeleton IntelliJ IDEA project

eclipse - generate a skeleton Eclipse IDE project

assemble - builds the outputs, by default this is the module zip file. It can
      also include a jar file if produceJar in gradle.properties is set to
      true. Outputs are created in build/libs. If pullInDeps in
      gradle.properties is set to 'true' then the modules dependencies will be
      automatically pulled into a nested mods directory inside the module
      during the build

copyMod - builds and copies the module to the local 'mods' directory so you
      can execute vertx runmod (etc) directly from the command line

modZip - creates the module zip into build/libs

clean - cleans everything up

test - runs the tests. An nice html test report is created in
      build/reports/tests (index.html)

run-<modulename> - runs the specified module. This is similar to executing
      vertx runmod from the command line except that it does not use the
      version of Vert.x installed and on the PATH to run it. Instead it uses
      the version of Vert.x that the module was compiled and tested against.

pullInDeps - pulls in all dependencies of the module into a nested module
      directory

uploadArchives - upload the module zip file (and jar if one has been created)
      to Nexus. You will need to configure sonatypeUsername and
      sonatypePassword in ~/.gradle/gradle.properties.

install - install any jars produced to the local Maven repository (.m2)
````