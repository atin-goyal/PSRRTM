package com.oracle.psr.jsonClasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Event {

@SerializedName("id")
@Expose
public String id;
@SerializedName("createdOn")
@Expose
public String createdOn;
@SerializedName("category")
@Expose
public String category;
@SerializedName("type")
@Expose
public String type;
@SerializedName("sourceType")
@Expose
public String sourceType;
@SerializedName("source")
@Expose
public Source source;
@SerializedName("properties")
@Expose
public Properties properties;
@SerializedName("canonicalLink")
@Expose
public String canonicalLink;

}

class Source {

@SerializedName("canonicalLink")
@Expose
public String canonicalLink;
@SerializedName("id")
@Expose
public String id;

}
