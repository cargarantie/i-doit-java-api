package com.cargarantie.idoit.api.config;

import com.cargarantie.idoit.api.ClientConfig;

public class TestConfig {

  public static ClientConfig demoIdoitCom() {
    return new ClientConfig("https://demo.i-doit.com/", "c1ia5q",
        "admin", "admin");
  }
}
