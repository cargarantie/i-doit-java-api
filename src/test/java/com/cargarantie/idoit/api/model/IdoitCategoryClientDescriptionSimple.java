package com.cargarantie.idoit.api.model;

import com.cargarantie.idoit.api.model.param.Dialog;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class IdoitCategoryClientDescriptionSimple extends IdoitCategory {

  @JsonProperty("f_popup_c_1581595935188")
  private String form;

  @JsonProperty("f_popup_c_1581421490617")
  private String os;

  @JsonProperty("f_text_c_1581421491453")
  private String osVersion;

  @JsonProperty("f_popup_c_1581421518089")
  private Dialog manufacturer;

  private String modelName;

  @JsonProperty("f_text_c_1581421862822")
  private String serialNumber;

  private String cpu;

  @JsonProperty("f_popup_c_1581421880679")
  private Dialog ram;

  @JsonProperty("f_popup_c_1581595990260")
  private Dialog hdType;

  @JsonProperty("f_text_c_1581595997348")
  private String hdSpace;
}
