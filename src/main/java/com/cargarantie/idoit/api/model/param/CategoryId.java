package com.cargarantie.idoit.api.model.param;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
public class CategoryId {
  int id;

  @JsonCreator
  public CategoryId(String id) {
    this.id = Integer.parseInt(id);
  }

  @JsonValue
  public int getId() {
    return id;
  }
}
