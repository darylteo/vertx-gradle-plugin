package com.darylteo.vertx.gradle.plugins;

import com.darylteo.vertx.gradle.configuration.ProjectConfiguration;
import com.darylteo.vertx.gradle.deployments.Deployment;
import com.darylteo.vertx.gradle.tasks.GenerateModJson;
import com.darylteo.vertx.gradle.tasks.VertxRun;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Zip;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class VertxPlugin implements Plugin<Project> {
  private static String COMMON_TASK_GROUP = "Vert.x Common";
  private static String RUN_TASK_GROUP = "Vert.x Run";

  public void apply(Project project) {
    applyPlugins(project);
    applyExtensions(project);
    addDependencies(project);
    addTasks(project);
  }

  private void applyPlugins(Project project) {
    project.getPlugins().apply("java");
    project.getPlugins().apply("watcher");
  }

  private void applyExtensions(Project project) {
    final ExtensionContainer extensions = project.getExtensions();
    final TaskContainer tasks = project.getTasks();

    // apply vertx convention
    final ProjectConfiguration vertx = extensions.create("vertx", ProjectConfiguration.class, project);

    // apply hooks to deployments container
    final NamedDomainObjectContainer<Deployment> deployments = vertx.getDeployments();

    deployments.whenObjectAdded(new Action<Deployment>() {
      @Override
      public void execute(Deployment deployment) {
        // building task name
        String name = deployment.getName();

        if (name.length() > 1) {
          name = name.substring(0, 1).toUpperCase() + (name.length() > 2 ? name.substring(1) : "");
        }

        VertxRun runTask = tasks.create("run" + name, VertxRun.class);
        runTask.setGroup(RUN_TASK_GROUP);

        deployment.setRunTask(runTask);

        System.out.println("Creating Task: " + runTask);
      }
    });

    deployments.whenObjectRemoved(new Action<Deployment>() {
      @Override
      public void execute(Deployment deployment) {
        tasks.remove(deployment.getRunTask());
      }
    });

    // add default "mod" deployment
    deployments.create("mod");
  }

  private void addDependencies(Project project) {
    ConfigurationContainer configurations = project.getConfigurations();

    Configuration vertxAll = configurations.create("vertxAll");
    Configuration vertxCore = configurations.create("vertxCore");
    Configuration vertxLang = configurations.create("vertxLang");
    Configuration vertxTest = configurations.create("vertxTest");
    Configuration vertxIncludes = configurations.create("vertxIncludes");

    vertxAll.extendsFrom(vertxCore);
    vertxAll.extendsFrom(vertxLang);
    vertxAll.extendsFrom(vertxTest);
    vertxAll.extendsFrom(vertxIncludes);

    vertxCore.setVisible(false);
    vertxLang.setVisible(false);
    vertxTest.setVisible(false);
    vertxIncludes.setVisible(false);

    // TODO: evaluate mangling of configurations. Can we do without?
    Configuration provided = configurations.maybeCreate("provided");
    Configuration compile = configurations.getByName("compile");

    provided.extendsFrom(vertxAll);
    compile.extendsFrom(provided);

//    afterEvaluate {
//      // validations
//      if (vertx?.platform?.version == null) {
//        println('Vert.x Platform Version not set. e.g. "vertx.platform.version = \'2.1\'".')
//      } else {
//        def vertxGroup = 'io.vertx'
//
//        dependencies {
//          // core and lang modules
//          vertxCore("${vertxGroup}:vertx-platform:${vertx.platform.version}")
//
//          if (vertx.platform.lang != null) {
//            def module = getModuleForLang(project, vertx.platform.lang)
//            if (!module) {
//              println("Unsupported Language: ${vertx.platform.lang}")
//            } else {
//              vertxLang(module)
//            }
//          }
//
//          if (vertx.platform.toolsVersion) {
//            vertxTest("${vertxGroup}:testtools:${vertx.platform.toolsVersion}")
//          }
//
//          // includes
//          vertx.config?.map?.includes?.collect { String dep ->
//            dep.replace('~', ':')
//          }.each { dep -> vertxIncludes dep }
//        }
//      }

//  }
  }

  private String getModuleForLang(Project project, String lang) {
    ConfigurationContainer configurations = project.getConfigurations();

    // setup classpath to search for langs.properties and get the correct version
    List<URL> classpath = new LinkedList<URL>();

    for (File file : configurations.getByName("vertxCore").getFiles()) {
      try {
        URL url = file.toURI().toURL();

        // File.toURL() is bugged. Use toURI().toURL(). See Javadoc
        classpath.add(url);
      } catch (MalformedURLException e) {
        //TODO: WARNING
      }
    }

    try {
      classpath.add(project.file("conf").toURI().toURL());
    } catch (MalformedURLException e) {
      //TODO: WARNING
    }

    // load lang properties files into properties
    Properties props = new Properties();

    try {
      URLClassLoader loader = new URLClassLoader(classpath.toArray(new URL[classpath.size()]));

      props.load(loader.getResourceAsStream("default-langs.properties"));
      props.load(loader.getResourceAsStream("langs.properties"));

      loader.close();
    } catch (IOException e) {
      //TODO: WARNING
    }

    // vertx modules are defined in a different format.
    String langString = props.getProperty(lang);
    if (langString != null) {
      return langString.split(":", -1)[0].replace("~", ":");
    }

    return langString;
  }

  private void addTasks(Project project) {
    addArchiveTasks(project);
    addRunTasks(project);
  }

  private void addArchiveTasks(Project project) {


    ProjectConfiguration vertx = project.getExtensions().findByType(ProjectConfiguration.class);
    TaskContainer tasks = project.getTasks();

    // archive tasks
    Sync assembleVertxTask = tasks.create("assembleVertxTask", Sync.class);
    assembleVertxTask.setGroup(COMMON_TASK_GROUP);

    Sync copyModTask = tasks.create("copyMod", Sync.class);
    copyModTask
      .from(assembleVertxTask)
      .into(vertx.getModuleDir());
    copyModTask.setGroup(COMMON_TASK_GROUP);

    // TODO: this needs to be a deferred configuration else it won't work properly

    GenerateModJson generateModJsonTask = tasks.create("generateModJson", GenerateModJson.class);
    generateModJsonTask.setGroup(COMMON_TASK_GROUP);

    Zip modZipTask = tasks.create("modZip", Zip.class);
    modZipTask.from(assembleVertxTask);
    modZipTask.setGroup(COMMON_TASK_GROUP);
    modZipTask.setClassifier("mod");

//    task("dummyMod") {
//      // only work this if the target dir does not exist
//      onlyIf { !compileJava.didWork && !project.vertx.moduleDir.exists() }
//      mustRunAfter compileJava
//
//      doLast {
//        def modDir = project.vertx.moduleDir
//        modDir.mkdirs()
//
//        project.file("$modDir/mod.json").withWriter { writer ->
//          writer << '{"main":"Main.java","auto-redeploy":true}'
//        }
//
//        project.file("$modDir/Main.java").withWriter { writer ->
//          writer << '''\
//import org.vertx.java.platform.Verticle;
//public class Main extends Verticle {}\
//            '''
//        }
//      }
//    }
//
//    // create the watcher task
//    task('__watch', type: WatcherTask) {
//      // flags
//      block = false
//      runImmediately = true
//
//      includes = ['src/**']
//      tasks = ['dummyMod', 'copyMod']
//    }

//
//    afterEvaluate {
//      // configure the test task with system variables
//      test {
//        systemProperty 'vertx.modulename', project.vertx.vertxName
//        systemProperty 'vertx.mods', rootProject.file('build/mods');
//
//        dependsOn copyMod
//      }
//
//      assembleVertx {
//        def sourceSets = sourceSets.matching({ it.name != SourceSet.TEST_SOURCE_SET_NAME })
//
//        into "$buildDir/mod"
//        from sourceSets*.output
//        from generateModJson
//
//        into('lib') {
//          from configurations.compile - configurations.provided
//        }
//
//        dependsOn generateModJson
//        dependsOn sourceSets*.classesTaskName
//      }
//
//      // runtime configuration extends from compile configuration
//      // so let's grab all vertx projects and link their copyMod tasks
//      def deployedProjects = configurations.runtime.dependencies
//        .withType(ProjectDependency.class)
//        .collect { dep -> dep.dependencyProject }
//
//      deployedProjects.each { module ->
//        evaluationDependsOn(module.path)
//
//        if (module.plugins.hasPlugin(VertxPlugin.class)) {
//          tasks.copyMod.dependsOn module.tasks.copyMod
//          tasks.__watch.dependsOn module.tasks.__watch
//        }
//      }
//
//    }
  }

  private void addRunTasks(Project project) {
//    project.with {
//      // configure the run/debug tasks
//      vertx.deployments.whenObjectAdded { Deployment dep ->
//        // add tasks for deployment
//        def name = dep.name.capitalize()
//
//        def configTask = task("generate${name}Config", type: GenerateDeploymentConfig) {
//          deployment = dep
//        }
//
//        def runTask = task("run$name", type: RunVertx) {
//          debug = false
//        }
//        def debugTask = task("debug$name", type: RunVertx) {
//          debug = true
//        }
//
//        [runTask, debugTask]*.configure {
//          deployment dep
//          configFile { configTask.outputFile }
//
//          dependsOn configTask
//        }
//
//        afterEvaluate {
//          // make this project the default module target if it was not specified
//          def module = dep.deploy?.module ?: project
//
//          if (module instanceof Project) {
//            if (module.vertx.config.map.'auto-redeploy') {
//              module.tasks.compileJava.options.failOnError = false
//
//              runTask.dependsOn module.dummyMod, module.tasks.__watch
//              debugTask.dependsOn module.dummyMod, module.tasks.__watch
//            } else {
//              runTask.dependsOn module.tasks.copyMod
//              debugTask.dependsOn module.tasks.copyMod
//            }
//          } else {
//            // since this is an external module I don't see a use case where you would want to
//            // debug the module
//            debugTask.enabled = false
//          }
//
//          if (!dep.platform.version) {
//            dep.platform.version = vertx.platform.version
//          }
//        }
//      }
//
//      vertx.deployments.whenObjectRemoved { Deployment dep ->
//        def name = dep.name.capitalize()
//        tasks.removeAll tasks."run$name", tasks."debug$name"
//      }
//
//      vertx.deployments {
//        mod {
//          deploy project
//        }
//      }
//    }
  }

}
