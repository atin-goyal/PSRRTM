package com.oracle.psr.jsonClasses;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Group {

	@SerializedName("subjectType")
	@Expose
	public SubjectType subjectType = new SubjectType();

	@SerializedName("name")
	@Expose
	public String name;
	@SerializedName("type")
	@Expose
	public String type;

	@SerializedName("subjectProperties")
	@Expose
	public List<SubjectProperty> subjectProperties = new ArrayList<>();;

	@SerializedName("id")
	@Expose
	public String id;

	private String JSON;

	private void generateJSON()
	{
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		JSON = gson.toJson(this);
	}

	public String getGroupJSON() {
		return JSON;
	}

	private Group()
	{
		type="dynamic";
		subjectType.id="compute.lvm";
		subjectType.canonicalLink="";
	}


	public Group(String gName, String vmType)
	{
		this();
		name=gName;
		SubjectProperty sp = new SubjectProperty();
		sp.name="vmType";
		sp.value=vmType;
		subjectProperties.add(sp);
		this.generateJSON();
	}

}

class SubjectProperty {

	@SerializedName("name")
	@Expose
	public String name;
	@SerializedName("value")
	@Expose
	public String value;

}
