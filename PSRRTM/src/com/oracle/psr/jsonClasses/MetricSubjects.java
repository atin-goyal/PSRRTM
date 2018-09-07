package com.oracle.psr.jsonClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.oracle.psr.workerClasses.ComputeVM;
import com.oracle.psr.workerClasses.GetLogger;

public class MetricSubjects {

	@SerializedName("hasMore")
	@Expose
	private Boolean hasMore;
	@SerializedName("canonicalLink")
	@Expose
	private String canonicalLink;
	@SerializedName("nextLink")
	@Expose
	private String nextLink;
	@SerializedName("items")
	@Expose
	private List<Item> items = null;
	@SerializedName("limit")
	@Expose
	private Integer limit;
	@SerializedName("offset")
	@Expose
	private Integer offset;

	public String getNextLink() {
		if(this.hasMore){
			return nextLink;
		}
		else
			return null;
	}

	public List<String> getAllVMDisplayNames() {
		if(items!=null){
			List<String> temp = new ArrayList<String>();
			for(Item a:this.items){
				temp.add(a.displayName);
			}

			return temp;
		}
		else{
			return null;
		}
	}

	public List<String> getAllVMIDs() {
		if(items!=null){
			List<String> temp = new ArrayList<String>();
			for(Item a:this.items){
				temp.add(a.id);
			}

			return temp;
		}
		else{
			return null;
		}
	}

	private String JSON;
	public static final Logger logger = GetLogger.getLogger();

	public String getJSON() {

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		JSON = gson.toJson(this);
		return JSON;
	}

	public ComputeVM getComputeVM(String subjectID){
		ComputeVM temp= new ComputeVM();
		temp.setVmID(subjectID);
		if(items!=null){
			outer:
				for(Item a:this.items){
					if(a.id == subjectID){
						for(Property p:a.properties){
							switch(p.name){
							case "vmType" : temp.setVmType(p.value);
							break;
							case "vmZone" : temp.setVmZone(p.value);
							break;								
							case "vmLogicalUid" : temp.setVmLogicalUid(p.value);
							break;						
							case "vmNamePrefix" : temp.setVmNamePrefix(p.value);
							break;				
							case "vmOrchestration" : temp.setVmOrchestration(p.value);
							break;					
							case "vmLabel" : temp.setVMLabel(p.value);
							break;
							default: logger.severe("PSRRTM: Unidentified property in CMS Json response. Please check. Property name: " + p.name + "Property value: " + p.value );
							}
						}
						break outer;
					}
				}
		}
		return temp;
	}

}

class Item {

	@SerializedName("subjectType")
	@Expose
	SubjectType subjectType;
	@SerializedName("externalVersion")
	@Expose
	String externalVersion;
	@SerializedName("externalId")
	@Expose
	String externalId;
	@SerializedName("canonicalLink")
	@Expose
	String canonicalLink;
	@SerializedName("isDeleted")
	@Expose
	Boolean isDeleted;
	@SerializedName("properties")
	@Expose
	List<Property> properties = null;
	@SerializedName("id")
	@Expose
	String id;
	@SerializedName("displayName")
	@Expose
	String displayName;

}

class Property {

	@SerializedName("name")
	@Expose
	String name;
	@SerializedName("value")
	@Expose
	String value;

}
