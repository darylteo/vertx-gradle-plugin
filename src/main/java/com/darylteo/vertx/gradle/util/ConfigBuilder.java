package com.darylteo.vertx.gradle.util;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.util.Expando;

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
  public void call(Closure closure) {
    closure.setDelegate(this);
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.call(this);
  }

  public void call(Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      methodMissing(entry.getKey(), new Object[]{entry.getValue()});
    }
  }

  public void methodMissing(String name, Object argArray) {
    final Object[] args = (Object[]) argArray;

    if (args.length == 1) {

      // simulates mutator
      Object value = args[0];

      // explicit 'null' argument was provided,
      // so we remove the property
      if (value == null) {
        setProperty(name, null);
        return;
      }

      // a closure was provided, so we are nesting builders using closures
      if (value instanceof Closure) {
        // check the existing property first for an existing value
        // add a new ConfigBuilder if it doesn't already exist
        // if the existing value is not a ConfigBuilder, then overwrite
        Object existing = getProperty(name);

        if (existing == null || !(existing instanceof ConfigBuilder)) {
          existing = new ConfigBuilder();
          setProperty(name, existing);
        }

        ((ConfigBuilder) existing).call((Closure) value);
        return;
      }

      // a map was provided, so we are nesting builders using map notation
      if (value instanceof Map) {
        // check the existing property first for an existing value
        // add a new ConfigBuilder if it doesn't already exist
        // if the existing value is not a ConfigBuilder, then overwrite
        Object existing = getProperty(name);

        if (existing == null || !(existing instanceof ConfigBuilder)) {
          existing = new ConfigBuilder();
          setProperty(name, existing);
        }

        ((ConfigBuilder) existing).call((Map) value);
        return;
      }

      // simple value was provided. Update the value
      setProperty(name, value);
    } else {
      throw new MissingMethodException(name, ConfigBuilder.class, args);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (Expando.class.isAssignableFrom(obj.getClass())) {
      return (super.getProperties().equals(((Expando) obj).getProperties()));
    }

    return (super.getProperties().equals(obj));
  }
}
