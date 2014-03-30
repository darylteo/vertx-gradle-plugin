# Vert.x Gradle Plugin

Unofficial Gradle plugin for Vert.x projects.

## Version

Latest version is 0.1.1

```
com.darylteo.vertx:vertx-gradle-plugin:0.1.0
```

## Sample Script

```groovy
buildscript {
	repositories {
		mavenCentral()
		mavenLocal()
	}

	dependencies {
		classpath 'com.darylteo.vertx:vertx-gradle-plugin:0.1.0'
	}
}

repositories { 
	mavenCentral() 
}

groupId 'com.darylteo'
version '0.1.0-SNAPSHOT'

apply plugin: 'java'
apply plugin: 'maven'
apply plugin: 'vertx'

vertx {
	platform {
		version '2.0.1-final'
		tools '2.0.1-final'
	}

	config { main 'Main' }

	deployments {
		mod {
			platform {
				version '2.0.3-final'
				cluster '127.0.0.1', 8080
				instances 10
			}
		}
	}

	info {
		groupId 'com.darylteo'
		artifactId project.name
		version '0.1.0-SNAPSHOT'

		description 'Java sample project for the vert.x gradle plugin'

		developers {
			developer {
				id 'darylteo'
				name 'Daryl Teo'
				email 'i.am@darylteo.com'
			}   
		}   

		licenses {
			license {
				name 'The Apache Software License, Version 2.0'
				url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
				distribution 'repo'
			}
		}

		properties { keywords 'Hello,World,How,Are,You?' }
	}
}

configurations {
	archives
}

artifacts {
	archives modZip
}

uploadArchives {
  repositories {
	mavenDeployer {
	  pom.withXml {
		asNode().children().addAll vertx.info
	  }
	}
  }
}

install {
  repositories {
	mavenInstaller {
	  pom.withXml {
		asNode().children().addAll vertx.info
	  }
	}
  }
}

````

## Documentation

### Platform Configuration

#### Platform Version

Specify which version of Vert.x you're using. If running unit tests, you may also specify the version of testtools.

```groovy
vertx {
	platform {
		// version {versionString}
		version '2.0.3-final'

		// tools {toolsVersionString}
		tools '2.0.1-final'
	}
}
````


#### Language Module

If you are using one of static-typed languages (other than Java) supported by Vert.x, just specify which language you require. This is useful when compiling under IDEs as the required Jars will be added by Gradle.

```groovy
vertx {
	platform {
		// ... platform configuration

		// lang {{langName}}
		lang 'scala'
		lang 'groovy'
	}
}
````

If you are not using a officially supported language, no worries! Simply add a langs.properties in the conf/ directory. For more information, see Vertx Configuration.

### Deployments

Setup deployment profiles for your project. 

__Incubating Features__

 - change deployment target 
 - multiple targets per deployment


__Example__

```groovy
vertx {
	platform { // ... } 

	deployments {
		mod {
			platform {
				cluster '127.0.0.1', 8080
				instances 10
			}
		}

		modWithConf {
			debug true

			platform {
				conf {
					hello: 'World',
					foo: 'bar'
				}
			}
		}
	}
}
````

By default, a deployment called "mod" is automatically created and you may alter this. However, you may add as many new ones as you wish.

__Running__

Each deployment configuration you create comes with 2 Gradle tasks _run<deploymentName>_ and _debug<deploymentName>_. Calling the debug task allows you to use remote debugging.
Refer to documentation from your preferred IDE regarding remote debugging configuration.


#### Platform Configuration

```groovy
vertx {
	deployments {
		mod {
			platform {
				// use this to deploy this module on a different version of vertx other than what it was built with. 
				// useful for testing compatibility with other versions
				version '2.0.1-final'

				// equivalent to -cluster -cluster-host 127.0.0.1 -cluster-port 8080
				cluster '127.0.0.1', 8080

				// equivalent to -instances 10
				instances 10

				// this will generate a file which is then passed to the module. Equivalent to -conf conf.json
				conf {
					hello: 'World',
					foo: 'bar'
				}
			}
		}
	}
}
````

### Module Info

Provide relevant for your module using standard Maven fields. This information will be included in the generated mod.json.

#### Example

```groovy
vertx {
	platform { //... }
	deployments { //... }

	info {
		description 'Java sample project for the vert.x gradle plugin'

		developers {
			developer {
				id 'darylteo'
				name 'Daryl Teo'
				email 'i.am@darylteo.com'
			}
			developer {
				id 'codemonkey'
				name 'Code Monkey'
				email 'monkey@darylteo.com'
			}
		}   

		licenses {
			license {
				name 'The Apache Software License, Version 2.0'
				url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
				distribution 'repo'
			}
		}

		properties { keywords 'Hello,World,How,Are,You?' }
	}
}
````

#### mod.json 

```javascript
{
	"description": "Java sample project for the vert.x gradle plugin",
	"licenses": [
		"The Apache Software License, Version 2.0"
	],
	"keywords": [
		"Hello",
		"World",
		"How",
		"Are",
		"You?"
	],
	"author": "Daryl Teo",
	"developers": [
		"Code Monkey"
	],
	"main": "Main"
}
````

### Vert.x Configuration

Vert.x looks for various files for its configurations. These include:

 - langs.properties
 - logging.properties
 - cluster.xml
 - repos.txt

This plugin automatically adds conf/ to the classpath if it exists, so you can place the files there. Putting langs.properties in this folder, in particular, allows you to run unofficial language modules in your application.

### Maven

This plugin takes the approach of not selecting your preferred method of configuring Maven, due to the number of options available to people. However, this plugin makes it easy for you to publish Vert.x modules to Maven if you desire so. Other repositories may be supported, however they have not been tested (e.g. BinTray, Ivy).

For more information on publishing to Maven, refer to the Gradle [documentation](http://www.gradle.org/docs/current/userguide/userguide_single.html)

#### Artifacts

Vertx-gradle-plugin creates a _modZip_ task that you may simply add to your artifacts. 

__Maven Plugin__

```groovy
configurations {
	archives
}

artifacts {
	archives modZip
}
````

__Maven Publish__

```groovy
publishing {
	publications {
		mavenJava(MavenPublication) {
			from components.java

			artifact modZip
		}
	}
}
````

#### Maven Pom

You can use the same info you specified for your project to generate your Maven poms.

__Maven Plugin__

```groovy
uploadArchives {
  repositories {
	mavenDeployer {
	  pom.withXml {
		asNode().children().addAll vertx.info
	  }
	}
  }
}

install {
  repositories {
	mavenInstaller {
	  pom.withXml {
		asNode().children().addAll vertx.info
	  }
	}
  }
}
````

__Maven Publish__

```groovy
publishing {
	publications {
		mavenJava(MavenPublication) {
			pom.withXml {
				asNode().children().addAll vertx.info
			}
		}
	}	
}
````

__Result__

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.darylteo</groupId>
  <artifactId>vertx-gradle</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <description>Java sample project for the vert.x gradle plugin</description>
  <developers>
	<developer>
	  <id>darylteo</id>
	  <name>Daryl Teo</name>
	  <email>i.am@darylteo.com</email>
	</developer>
	<developer>
	  <id>codemonkey</id>
	  <name>Code Monkey</name>
	  <email>monkey@darylteo.com</email>
	</developer>
  </developers>
  <licenses>
	<license>
	  <name>The Apache Software License, Version 2.0</name>
	  <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
	  <distribution>repo</distribution>
	</license>
  </licenses>
  <properties>
	<keywords>Hello,World,How,Are,You?</keywords>
  </properties>
</project>
````
