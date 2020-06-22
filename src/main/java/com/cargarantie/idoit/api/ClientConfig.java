package com.cargarantie.idoit.api;

import lombok.Value;

@Value
public class ClientConfig {

  String url;
  String apiKey;
  String username;
  String password;
}
