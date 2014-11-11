package com.darylteo.vertx.gradle.tasks;

import com.darylteo.vertx.gradle.configuration.VertxExtension;
import groovy.json.JsonOutput;
import groovy.util.Node;
import groovy.util.NodeList;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GenerateModJson extends DefaultTask {
  private File destinationDir;

  @OutputDirectory
  public File getDestinationDir() {
    Project project = getProject();
    if (destinationDir == null) {
      return project.getBuildDir().toPath().resolve("jsons").toFile();
    } else {
      return project.file(destinationDir);
    }
  }

  @TaskAction
  public void run() throws IOException {
    Project project = this.getProject();
    VertxExtension vertx = project.getExtensions().getByType(VertxExtension.class);

    File destDir = this.getDestinationDir();
    File modjson = destDir.toPath().resolve("mod.json").toFile();

    destDir.mkdirs();

    // http://vertx.io/mods_manual.html
    Map<String, Object> config = vertx.getConfig().getMap();
    Node info = vertx.getInfo().asNode();

    // module info
    // description, licenses, author, keywords, developers, homepage
    Map<String, Object> data = new HashMap<>();
    data.putAll(config);
    data.putAll(new NodeToMapConvertor().convert(info));

    try (
      PrintWriter writer = new PrintWriter(new FileWriter(modjson))
    ) {
      JsonOutput json = new JsonOutput();
      writer.write(json.toJson(data));

      writer.flush();
      writer.close();
    }
  }

  private void insert(Map<String, Object> map, String key, Object value) {
    if (value == null) {
      return;
    }

    map.put(key, value);
  }

  private void copyProperty(Map<String, Object> src, Map<String, Object> dest, String property) {
    Object prop = src.get(property);
    if (prop != null) {
      dest.put(property, prop);
    }
  }

  public class NodeToMapConvertor {
    public Map<String, Object> convert(Node root) {
      Map<String, Object> result = new HashMap<>();

      NodeList description = (NodeList) root.get("description");
      this.put(result, "description", getValue(description));

      NodeList licenses = ((NodeList) root.get("licenses")).getAt("license").getAt("name");
      this.put(result, "licenses", getValues(licenses));

      NodeList developers = ((NodeList) root.get("developers")).getAt("developer").getAt("name");
      this.put(result, "author", getValue(developers));

      if (developers.size() > 1) {
        developers.remove(0);
        this.put(result, "developers", getValues(developers));
      }

      NodeList keywords = ((NodeList) root.get("properties")).getAt("keyword");
      this.put(result, "keywords", getValues(keywords));

      NodeList homepage = ((NodeList) root.get("url"));
      this.put(result, "homepage", getValue(homepage));

      return result;
    }

    private void put(Map<String, Object> map, String key, Object value) {
      if (value == null) {
        return;
      }

      map.put(key, value);
    }


    private Object getValue(Object obj) {
      if (!(obj instanceof NodeList)) {
        return null;
      }

      NodeList node = (NodeList) obj;

      if (node.isEmpty()) {
        return null;
      }

      return ((Node) node.get(0)).value();
    }

    private List<Object> getValues(Object obj) {
      if (!(obj instanceof NodeList)) {
        return null;
      }

      NodeList nodes = (NodeList) obj;

      if (nodes.isEmpty()) {
        return null;
      }

      List<Object> list = new LinkedList<>();

      for (Object element : nodes) {
        list.add(((Node) element).value());
      }

      return list;
    }
  }
}
