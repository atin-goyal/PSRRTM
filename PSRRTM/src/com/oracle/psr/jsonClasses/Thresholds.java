package com.oracle.psr.jsonClasses;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Thresholds {

@SerializedName("hasMore")
@Expose
public Boolean hasMore;
@SerializedName("canonicalLink")
@Expose
public String canonicalLink;
@SerializedName("items")
@Expose
public List<Threshold> items = null;
@SerializedName("limit")
@Expose
public Integer limit;
@SerializedName("offset")
@Expose
public Integer offset;

}
