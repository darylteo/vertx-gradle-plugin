package com.darylteo.vertx.gradle.tasks;

import com.darylteo.vertx.gradle.configuration.ProjectConfiguration;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Map;

public class GenerateModJson extends DefaultTask {
  private File destinationDir;

  public GenerateModJson() {
    destinationDir = this.getProject().file(this.getProject().getBuildDir() + "/jsons");
  }

  @OutputDirectory
  public File getDestinationDir() {
    return getProject().file(destinationDir);
  }

  @TaskAction
  public void run() {
    Project project = this.getProject();
    ProjectConfiguration vertx = project.getExtensions().getByType(ProjectConfiguration.class);

    File destDir = this.getDestinationDir();
    File modjson = project.file(destDir + "/mod.json");

    destDir.mkdirs();

    // http://vertx.io/mods_manual.html
    Map<String, Object> data = vertx.getInfo().getProperties();

    // module info
    // description, licenses, author, keywords, developers, homepage
//    this.insert(data, 'description', info.description[0]?.value())
//    this.insert(data, 'licenses', info.licenses[0]?.license.collect { it.name[0].value() })
//
//    // need to use get() for properties, to bypass getProperties() method
//    def props = info.getAt(QName.valueOf('properties'))[0]
//    if (props) {
//      def keywords = props.keywords[0]?.value().split('\\s*,\\s*')
//      this.insert(data, 'keywords', keywords)
//    }
//
//    def developers = info.developers[0]?.developer
//    if (developers) {
//      if (developers.size() > 0) {
//        insert data, 'author', developers[0]?.name[0]?.value()
//      }
//      if (developers.size() > 1) {
//        def others = (developers.collect { it.name[0].value() })
//        others.remove(0)
//        insert data, 'developers', others
//      }
//    }
//
//    insert data, 'homepage', info.url[0]?.value()
//
//    // override with module config
//    // main, worker, multi-threaded, includes, preserve-cwd, auto-redeploy, resident, system, deploys
//    data << project.vertx.config.map
//
//    // hack until vertx supports array for includes property
//    if (data.includes && !(data.includes instanceof String)) {
//      data.includes = data.includes.join(',')
//    }
//
//    try (
//      PrintWriter writer = new PrintWriter(new FileWriter(modjson))
//    ) {
//      writer.write(JsonObject.toJson(data));
//    }
  }
}
