package com.darylteo.vertx.gradle.tasks;

import com.darylteo.vertx.gradle.deployments.Deployment;
import com.darylteo.vertx.gradle.util.MapToJson;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class GenerateDeploymentConfig extends DefaultTask {
  private Deployment deployment;
  private File outputFile;

  public Deployment setDeployment(Deployment deployment) {
    return this.deployment = deployment;

//    this.inputs.property('config', { MapToJson.convert(deployment.config) })

//    this.outputFile = { "${project.buildDir}/configs/${deployment.name}.conf" }
//    this.outputs.file outputFile
  }

  @Input
  String getConfig() {
    return MapToJson.convert(deployment.getConfig());
  }

  public Deployment getDeployment() {
    return deployment;
  }

  @OutputFile
  public File getOutputFile() {
    if (this.outputFile == null) {
      return getProject().file(getProject().getBuildDir() + "/configs/" + this.deployment.getName() + ".conf");
    }

    return this.outputFile;
  }

  public void setOutputFile(File outputFile) {
    this.outputFile = outputFile;
  }

  @TaskAction
  public void run() {
    File file = getProject().file(outputFile);
    File dir = file.getParentFile();

    file.delete();

    try {
      if (dir.isDirectory() || dir.mkdirs()) {
        file.createNewFile();

        try (
          PrintWriter writer = new PrintWriter(new FileWriter(file))
        ) {
          writer.write(this.getConfig());
        }
      } else {
        throw new Exception("Could not create directory: " + dir);
      }
    } catch (Throwable e) {
      throw new GradleException("Error occurred while creating deployment configuration file", e);
    }

  }

}
