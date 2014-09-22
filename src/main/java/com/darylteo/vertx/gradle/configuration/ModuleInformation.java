package com.darylteo.vertx.gradle.configuration;

import groovy.lang.Closure;
import groovy.util.Node;

/**
 * Created by dteo on 22/09/2014.
 */
public class ModuleInformation {
  private Node info;

  public ModuleInformation() {
    this.info = new Node(null, "info");
  }

  public void call(Closure closure) {
    closure.setDelegate(this.info);
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);

    Node config = new Node(info, "config");

    // lets the closure configure the node
    config.replaceNode(closure);
  }

  public Node asNode() {
    return this.info;
  }
}
