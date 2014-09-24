package com.darylteo.vertx.gradle.tests.utils;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by dteo on 24/09/2014.
 */
public class GradleBuildRunner extends Runner {
  private final Class<?> clazz;
  private final Path root;
  private final ExecutorFactory factory;

  public GradleBuildRunner(Class<?> clazz) throws Exception {
    this.clazz = clazz;
    this.root = getRootProject().toPath();
    this.factory = new ExecutorFactory(createClassLoader());
  }

  @Override
  public Description getDescription() {
    Description result = Description.createSuiteDescription(
      this.clazz.getName(),
      this.clazz.getAnnotations()
    );

    return result;
  }

  @Override
  /**
   * get each test method, and execute them against root project
   */
  public void run(RunNotifier notifier) {
    Method[] methods = this.clazz.getDeclaredMethods();

    for (Method method : methods) {
      if (!shouldTest(method)) {
        continue;
      }

      this.factory.newExecutor(this.clazz, method, this.root, notifier).test();
    }
  }

  private File getRootProject() {
    RootProject annotation = this.clazz.getAnnotation(RootProject.class);
    if (annotation == null) {
      return null;
    }

    return new File(annotation.value());
  }

  private boolean shouldTest(Method method) {
    if (method.getAnnotation(Test.class) != null) {
      return true;
    }
    return false;
  }

  private ClassLoader createClassLoader() throws Exception {
    Path libs = Paths.get(
      System.getenv("GRADLE_HOME"),
      "lib"
    );

    ClassLoader loader = new URLClassLoader(new URL[]{
      libs.resolve("gradle-launcher-2.0.jar").toUri().toURL()
    });

    return loader;
  }


  private class Executor {
    private Class<?> expected;

    private RunNotifier notifier;
    private Description test;
    private Method mainMethod;

    private Path projectDir;
    private String task;

    private Object launcher;
    private ExecutionListener listener;

    public Executor(Description test, RunNotifier notifier, Object launcher, Method mainMethod, ExecutionListener listener, Path projectDir, String task, Class<?> expected) {
      this.test = test;
      this.notifier = notifier;

      this.mainMethod = mainMethod;
      this.projectDir = projectDir;
      this.task = task;

      this.expected = expected;

      this.launcher = launcher;
      this.listener = listener;
    }

    public void test() {
      this.notifier.fireTestStarted(this.test);
      Throwable error = null;

      // cast to object to avoid varargs
      try {
        mainMethod.invoke(
          launcher,
          (Object) new String[]{
            "--no-daemon",
            "--rerun-tasks",
            "--configure-on-demand",
            "--project-dir=" + projectDir,
            task
          },
          this.listener
        );
      } catch (Exception e) {
        // catches configuration error
        error = e;
      }

      // retrieve runtime error from listener
      if (error == null) {
        error = this.listener.getError();
      }

      if (error != null && !expected.isAssignableFrom(error.getClass())) {
        notifier.fireTestFailure(new Failure(this.test, error));
      } else {
        notifier.fireTestFinished(this.test);
      }
    }

  }

  private class ExecutorFactory {
    private final ClassLoader loader;

    private final Class<?> mainClass;
    private final Method mainMethod;
    private final Class<?> listenerClass;

    public ExecutorFactory(ClassLoader loader) throws Exception {
      this.loader = loader;

      this.mainClass = loader.loadClass("org.gradle.launcher.Main");
      this.listenerClass = loader.loadClass("org.gradle.launcher.bootstrap.ExecutionListener");

      this.mainMethod = this.mainClass.getDeclaredMethod("doAction", String[].class, this.listenerClass);
      this.mainMethod.setAccessible(true);
    }

    public Executor newExecutor(Class<?> testClass, Method testMethod, Path projectDir, RunNotifier notifier) {
      Test annotation = testMethod.getAnnotation(Test.class);

      Description description = Description.createTestDescription(
        testClass,
        testMethod.getName(),
        testMethod.getAnnotations()
      );

      Class<?> expected = annotation != null ? annotation.expected() : null;

      // instantiate Main class, get the required method for executing
      Object launcher = null;
      try {
        launcher = this.mainClass.newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      Executor executor = new Executor(
        description,
        notifier,
        launcher,

        mainMethod,
        (ExecutionListener) Proxy.newProxyInstance(loader, new Class<?>[]{
          this.listenerClass,
          ExecutionListener.class
        }, new RecordingExecutionListener()),

        projectDir,
        ":" + testMethod.getName() + ":build",

        expected
      );

      return executor;
    }
  }

  public interface ExecutionListener {
    Throwable getError();
  }

  private class RecordingExecutionListener implements InvocationHandler {
    private Throwable error;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getName().equals("onFailure")) {
        if (args.length == 1) {
          RecordingExecutionListener.this.error = (Throwable) args[0];
        }
      } else if (method.getName().equals("getError")) {
        return RecordingExecutionListener.this.error;
      }

      return null;
    }
  }

}
