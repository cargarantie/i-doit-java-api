package com.cargarantie.idoit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@CategoryName("C__CATG__CONTACT")
public class CategoryContactAssignment extends IdoitCategory {

  private int contact;
  private String role;
  private String primary;
}
