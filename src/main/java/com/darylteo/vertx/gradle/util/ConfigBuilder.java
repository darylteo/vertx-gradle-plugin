package com.darylteo.vertx.gradle.util;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.util.Expando;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>A ConfigBuilder allows for builder-syntax support for creating nested maps. Unless JsonBuilder, it also allows
 * you to modify existing values as a normal object
 * </p>
 * <pre>
 *   def config = new ConfigBuilder();
 *   config.foo = "bar";
 *   config.properties {
 *     more "properties"
 *   }
 *
 *   config.map key: "value"
 * </pre>
 */
public class ConfigBuilder extends Expando {
  public void methodMissing(String name, Object argArray) {
    final Object[] args = (Object[]) argArray;

    if (args.length == 1) {
      insertValue(name, args[0]);
    } else if (args.length > 0) {
      insertValue(name, args);
    } else {
      throw new MissingMethodException(name, ConfigBuilder.class, args);
    }
  }

  public void call(Closure closure) {
    insertClosure(closure);
  }

  public void call(Map<String, Object> map) {
    insertMap(map);
  }

  private void insertValue(String name, Object value) {
    System.out.println("Insert Value: " + name + " Value: " + value);
    if (value == null) {
      super.setProperty(name, null);
      return;
    }

    if (isClosure(value)) {
      insertClosure(name, (Closure) value);
    } else if (isMap(value)) {
      insertMap(name, (Map<String, Object>) value);
    } else if (isCollection(value)) {
      insertCollection(name, (Iterable<?>) value);
    } else if (isArray(value)) {
      insertCollection(name, (Object[]) value);
    } else {
      super.setProperty(name, value);
    }
  }

  private void insertClosure(Closure closure) {
    closure.setDelegate(this);
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.call(this);
  }

  private void insertClosure(String name, Closure closure) {
    ConfigBuilder prop = createOrOverwriteNestedProperty(name);
    prop.insertClosure(closure);
  }

  private void insertMap(Map<String, Object> values) {
    for (Map.Entry<String, Object> entry : values.entrySet()) {
      this.insertValue(entry.getKey(), entry.getValue());
    }
  }

  private void insertMap(String name, Map<String, Object> values) {
    ConfigBuilder prop = createOrOverwriteNestedProperty(name);

    for (Map.Entry<String, Object> entry : values.entrySet()) {
      prop.insertValue(entry.getKey(), entry.getValue());
    }
  }

  private void insertCollection(String name, Object[] values) {
    this.insertCollection(name, Arrays.asList(values));
  }

  private void insertCollection(String name, Iterable<?> values) {
    super.setProperty(name, recursivelyConfigureCollection(values));
  }

  private ConfigBuilder createOrOverwriteNestedProperty(String name) {
    Object value = super.getProperty(name);
    ConfigBuilder result = null;

    if (value != null && ConfigBuilder.class.isAssignableFrom(value.getClass())) {
      result = (ConfigBuilder) value;
    } else {
      result = new ConfigBuilder();
      super.setProperty(name, result);
    }

    return result;
  }

  private List<Object> recursivelyConfigureCollection(Iterable<?> values) {
    List<Object> results = new LinkedList<>();

    for (Object value : values) {
      if (isClosure(value)) {
        ConfigBuilder prop = new ConfigBuilder();
        results.add(prop);
        prop.insertClosure((Closure) value);
      } else if (isMap(value)) {
        ConfigBuilder prop = new ConfigBuilder();
        results.add(prop);
        prop.insertMap((Map) value);
      } else if (isArray(value)) {
        results.add(recursivelyConfigureCollection(Arrays.asList(value)));
      } else if (isCollection(value)) {
        results.add(recursivelyConfigureCollection((Iterable<?>) value));
      } else {
        results.add(value);
      }
    }

    return results;
  }

  private boolean isMap(Object value) {
    return Map.class.isAssignableFrom(value.getClass());
  }

  private boolean isClosure(Object value) {
    return Closure.class.isAssignableFrom(value.getClass());
  }

  private boolean isArray(Object value) {
    return Object[].class.isAssignableFrom(value.getClass());
  }

  private boolean isCollection(Object value) {
    return Iterable.class.isAssignableFrom(value.getClass());
  }

  @Override
  public boolean equals(Object obj) {
    if (Expando.class.isAssignableFrom(obj.getClass())) {
      return (super.getProperties().equals(((Expando) obj).getProperties()));
    }

    return (super.getProperties().equals(obj));
  }
}
