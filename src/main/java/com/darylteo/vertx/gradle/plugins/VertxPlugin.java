package com.darylteo.vertx.gradle.plugins;

import com.darylteo.vertx.gradle.configuration.ModuleConfiguration;
import com.darylteo.vertx.gradle.configuration.VertxExtension;
import com.darylteo.vertx.gradle.configuration.VertxPlatformConfiguration;
import com.darylteo.vertx.gradle.deployments.Deployment;
import com.darylteo.vertx.gradle.tasks.GenerateDeploymentConfig;
import com.darylteo.vertx.gradle.tasks.GenerateModJson;
import com.darylteo.vertx.gradle.tasks.VertxRun;
import groovy.lang.Closure;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.file.CopySpec;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.tasks.testing.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

public class VertxPlugin implements Plugin<Project> {
  private static String COMMON_TASK_GROUP = "Vert.x Common";
  private static String RUN_TASK_GROUP = "Vert.x Run";

  private static String VERTX_MAVEN_GROUP = "io.vertx";
  private static String VERTX_MAVEN_NAME = "vertx-platform";
  private static String VERTX_MAVEN_LANG_NAME_PREFIX = "vertx-lang-";

  private VertxPlugin that = this;

  public void apply(Project project) {
    applyPlugins(project);
    applyExtensions(project);
    addDependencies(project);
    addTasks(project);
  }

  private void applyPlugins(Project project) {
    project.getPlugins().apply("java");
//    project.getPlugins().apply("watcher");
  }

  private VertxExtension applyExtensions(final Project project) {
    final ExtensionContainer extensions = project.getExtensions();
    final TaskContainer tasks = project.getTasks();

    // apply vertx convention
    final VertxExtension vertx = extensions.create("vertx", VertxExtension.class, project);

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

        GenerateDeploymentConfig generateConfigTask = tasks.create("generate" + name + "Config", GenerateDeploymentConfig.class);
        generateConfigTask.setGroup(RUN_TASK_GROUP);
        generateConfigTask.setDeployment(deployment);

        VertxRun runTask = tasks.create("run" + name, VertxRun.class);
        runTask.setGroup(RUN_TASK_GROUP);
        runTask.setDeployment(deployment);
        runTask.dependsOn(generateConfigTask);

        deployment.setRunTaskName(runTask.getName());
        deployment.setGenerateConfigTaskName(generateConfigTask.getName());
      }
    });

    deployments.whenObjectRemoved(new Action<Deployment>() {
      @Override
      public void execute(Deployment deployment) {
        tasks.remove(project.getTasks().findByName(deployment.getRunTaskName()));
        tasks.remove(project.getTasks().findByName(deployment.getGenerateConfigTaskName()));
      }
    });

    // add default "mod" deployment for this project
    deployments.create("mod").deploy(project);

    return vertx;
  }

  private void addDependencies(Project project) {
    RepositoryHandler repositories = project.getRepositories();
    repositories.mavenCentral();

    ConfigurationContainer configurations = project.getConfigurations();

    final Configuration vertxAll = configurations.create("vertxAll");
    final Configuration vertxCore = configurations.create("vertxCore");
    final Configuration vertxLang = configurations.create("vertxLang");
    final Configuration vertxTest = configurations.create("vertxTest");
    final Configuration vertxIncludes = configurations.create("vertxIncludes");

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

    project.afterEvaluate(new Action<Project>() {
      @Override
      public void execute(Project project) {
        // validate Vert.x configuration
        VertxExtension vertx = project.getExtensions().getByType(VertxExtension.class);
        DependencyHandler dependencies = project.getDependencies();

        VertxPlatformConfiguration platform = vertx.getPlatform();
        ModuleConfiguration config = vertx.getConfig();

        String version = platform.getVersion();

        if (version == null || version.trim().isEmpty()) {
          version = "+";
        }

        // load vert.x platform
        String artifactId = String.format("%s:%s:%s", VERTX_MAVEN_GROUP, VERTX_MAVEN_NAME, version);
        dependencies.add(vertxCore.getName(), artifactId);

        // load vert.x language dependencies
        String language = platform.getLang();
        if (language == null || language.trim().isEmpty()) {
          language = "java";
        }

        if (language != "java") {
          try {
            String languageArtifactId = getModuleForLang(project, language);
            dependencies.add(vertxLang.getName(), languageArtifactId);
          } catch (Exception e) {
            throw new GradleException("Error loading appropriate language module for vert.x", e);
          }
        }

        // load vert.x include dependencies
        // TODO: only load dependencies for languages that require it - i.e. compiled modx
        for (String include : config.getIncludes()) {
          include = include.replace("~", ":");
          dependencies.add(vertxIncludes.getName(), include);
        }
      }
    });
  }

  private String getModuleForLang(Project project, String lang) throws Exception {
    ConfigurationContainer configurations = project.getConfigurations();

    // setup classpath to search for langs.properties and get the correct version
    List<URL> classpath = new LinkedList<URL>();

    for (File file : configurations.getByName("vertxCore").getFiles()) {
      // File.toURL() is bugged. Use toURI().toURL(). See Javadoc
      classpath.add(file.toURI().toURL());
    }

    classpath.add(project.file("conf").toURI().toURL());

    // load lang properties files into properties
    Properties props = new Properties();

    try (
      URLClassLoader loader = new URLClassLoader(classpath.toArray(new URL[classpath.size()]))
    ) {
      InputStream in;

      in = loader.getResourceAsStream("default-langs.properties");
      if (in != null) {
        props.load(in);
      }

      in = loader.getResourceAsStream("langs.properties");
      if (in != null) {
        props.load(in);
      }
    }

    // vertx modules are defined in a different format.
    String langString = props.getProperty(lang);
    if (langString != null) {
      return langString.split(":", -1)[0].replace("~", ":");
    }

    throw new Exception("Language \"" + lang + "\" is not a known vert.x language. Please ensure it is registered in langs.properties");
  }

  private void addTasks(Project project) {
    addArchiveTasks(project);
  }

  private void addArchiveTasks(final Project project) {
    final VertxExtension vertx = project.getExtensions().findByType(VertxExtension.class);
    final JavaPlugin java = project.getPlugins().findPlugin(JavaPlugin.class);
    final TaskContainer tasks = project.getTasks();
    final ConfigurationContainer configurations = project.getConfigurations();

    // archive tasks
    final GenerateModJson generateModJsonTask = tasks.create("generateModJson", GenerateModJson.class);
    generateModJsonTask.setGroup(COMMON_TASK_GROUP);

    // assemble vertx task
    final Sync assembleVertxTask = tasks.create("assembleVertxTask", Sync.class);
    assembleVertxTask.setGroup(COMMON_TASK_GROUP);

    assembleVertxTask.into(project.file(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return Paths.get(project.getBuildDir().toString(), "mods").toString();
      }
    }));

    assembleVertxTask.into("lib", new Closure(this) {
      @Override
      public Object call() {
        // this should be a CopySpec
        CopySpec spec = (CopySpec) this.getDelegate();

        spec.from(configurations.getByName("compile").minus(configurations.getByName("provided")));
        return null;
      }
    });

    assembleVertxTask.from(new Callable<List<SourceSetOutput>>() {
      @Override
      public List<SourceSetOutput> call() throws Exception {
        SourceSetContainer sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets");
        List<SourceSetOutput> files = new LinkedList<>();

        for (SourceSet sourceSet : sourceSets) {
          if (sourceSet.getName().equals(SourceSet.TEST_SOURCE_SET_NAME)) {
            continue;
          }

          files.add(sourceSet.getOutput());
        }

        return files;
      }
    });

    assembleVertxTask.from(generateModJsonTask);

    final Sync copyModTask = tasks.create("copyMod", Sync.class);
    copyModTask
      .from(assembleVertxTask)
      .into(vertx.getModuleDir());
    copyModTask.setGroup(COMMON_TASK_GROUP);

    final Zip modZipTask = tasks.create("modZip", Zip.class);
    modZipTask.from(assembleVertxTask);
    modZipTask.setGroup(COMMON_TASK_GROUP);
    modZipTask.setClassifier("mod");

    final Test testTask = (Test) tasks.getByName("test");

    project.afterEvaluate(new Action<Project>() {
      @Override
      public void execute(final Project project) {
        // set up system properties required for the test task templates.
        testTask.systemProperty("vertx.modulename", vertx.getVertxName());
        testTask.systemProperty("vertx.mods", Paths.get(project.getRootProject().getBuildDir().toString(), "mods")).toString();
        testTask.dependsOn(copyModTask);
      }
    });
  }

}
