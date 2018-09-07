package com.oracle.psr.jsonClasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubjectType {

	@SerializedName("canonicalLink")
	@Expose
	String canonicalLink;
	@SerializedName("id")
	@Expose
	String id;

}
