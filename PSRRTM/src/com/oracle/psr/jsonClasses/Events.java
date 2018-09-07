package com.oracle.psr.jsonClasses;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Events {

@SerializedName("events")
@Expose
public List<Event> events = null;

private String JSON;

public String getJSON() {
	
	Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	JSON = gson.toJson(this);
	return JSON;
}

}
