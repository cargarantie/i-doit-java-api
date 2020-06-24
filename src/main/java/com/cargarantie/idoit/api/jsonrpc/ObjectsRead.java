package com.cargarantie.idoit.api.jsonrpc;

import com.cargarantie.idoit.api.model.IdoitObject;
import com.cargarantie.idoit.api.util.Util;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.Value;

/*
filter	Array	No	Filter list of objects; see below for a full list of options
limit	Mixed	No

Maximum amount of objects (as integer), for example, fetch the first thousand of objects: 1000

Combine this limit with an offset (as string), for example, fetch the next thousand of objects: "1000,1000"
order_by	String	No

Order result set by (see filter for more details what each value means):

    "isys_obj_type__id",
    "isys_obj__isys_obj_type__id",
    "type",
    "isys_obj__title",
    "title",
    "isys_obj_type__title",
    "type_title",
    "isys_obj__sysid",
    "sysid",
    "isys_cats_person_list__first_name",
    "first_name",
    "isys_cats_person_list__last_name",
    "last_name",
    "isys_cats_person_list__mail_address",
    "email",
    "isys_obj__id", or
    "id"

sort	String	No	Only useful in combination with key order_by; allowed values are either "ASC" (ascending) or "DESC" (descending)


FILTER
ids	Array	No	List of object identifiers (as integers), for example: [1, 2, 3]
type	Integer|String	No

Object type identifier (as integer), for example: 5

Alternatively, object type constant (as string), for example: "C__OBJTYPE__SERVER"
title	String	No	Object title (see attribute Title in category Global), for example: "My little server"
type_title	String	No

Translated name of object type, for example: "Server"

Note: Set a proper language in your request.
sysid	String	No	SYSID (see category Global), for example: "SRV_101010"
first_name	String	No	First name of an object of type Persons (see attribute First name in category Persons → Master Data), for example: "John"
last_name	String	No	Last name of an object of type Persons (see attribute Last name in category Persons → Master Data), for example: "Doe"
email	String	No

Primary e-mail address of an object of type Persons, Person groups or Organization (see attribute E-mail address in categories Persons/Person groups/Organization → Master Data), for example: "john.doe@example.com"
 */

@Value
public class ObjectsRead<T extends IdoitObject> implements IdoitRequest<ObjectsReadResponse> {

  private static final String METHOD = "cmdb.objects.read";
  Filter filter;
  Ordering orderBy;

  @JsonIgnore
  Class<T> filterType;

  @Builder
  public ObjectsRead(String filterFirstName, String filterLastName,
      @Singular List<Integer> filterIds,
      String filterTitle, Class<T> filterType, String filterTypeName, String filterEmail,
      String filterSysid, Ordering orderBy) {

    if (filterType != null) {
      filterTypeName = Util.getObjectTypeName(filterType);
    }

    if (filterIds.size() == 0) {
      filterIds = null;
    }

    this.filter = new Filter(filterFirstName, filterLastName, filterIds, filterTitle,
        filterTypeName,
        filterEmail, filterSysid, null);
    this.orderBy = orderBy;
    this.filterType = filterType;
  }

  @Override
  public Class<ObjectsReadResponse> getResponseClass() {
    return ObjectsReadResponse.class;
  }

  @Override
  public String getMethod() {
    return METHOD;
  }

  public enum Ordering {
    email,
    first_name,
    id,
    last_name,
    sysid,
    title,
    type,
    type_title
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Filter {

    private String firstName;
    private String lastName;
    private List<Integer> ids;
    private String title;

    /**
     * Object type identifier (as integer), for example: 5
     *
     * Alternatively, object type constant (as string), for example: "C__OBJTYPE__SERVER"
     */
    private String type;
    private String email;
    private String sysid;

    /**
     * Translated name of object type, for example: "Server"
     *
     * Note: Set a proper language in your request.
     */
    private String typeTitle;
  }
}
