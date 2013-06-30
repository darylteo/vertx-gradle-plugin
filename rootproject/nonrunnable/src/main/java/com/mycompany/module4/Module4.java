package com.mycompany.module4;

import com.darylteo.rx.promises.*;

public class Module4  {
  public Module4(){
    System.out.println("My class has been created!");

    makePromise();
  }

  // Testing vertx module support
  private Promise<String> makePromise() {
    Promise<String> promise = Promise.defer();

    return promise;
  }
}
