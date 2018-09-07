package com.oracle.psr.jsonClasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Properties {

@SerializedName("metricTypeId")
@Expose
public String metricTypeId;
@SerializedName("firstPeriodStart")
@Expose
public String firstPeriodStart;
@SerializedName("subjectId")
@Expose
public String subjectId;
@SerializedName("targetId")
@Expose
public String targetId;
@SerializedName("targetType")
@Expose
public String targetType;

}
