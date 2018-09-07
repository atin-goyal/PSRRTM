package com.oracle.psr.jsonClasses;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.oracle.psr.TestServlet;
import com.oracle.psr.workerClasses.GetLogger;

class SubjectT {
	String id;
}

class SeriesT {
	String id;
	SubjectT subject = new SubjectT();
}


class TargetT {
	
	@SerializedName("group")
	@Expose
	public GroupT group = new GroupT();
	
	@SerializedName("metricType")
	@Expose
	public MetricTypeMR metricType = new MetricTypeMR();
	
	@SerializedName("isGroupExpanded")
	@Expose
	public Boolean isGroupExpanded;
}

class Condition {
	String stats;
	String operation;
	String value;
	String periodDuration;
	String periodCountToSatisfy;
	String periodCountToUnsatisfy;
}

class AlertAction {
	String type;
	String url = "a";
}

class GroupT {

@SerializedName("id")
@Expose
public String id;

}

public class Threshold {

	public String id;
	public String name;
	private TargetT target = new TargetT();
	private Condition condition = new Condition();
	private AlertAction alertAction = new AlertAction();
	private String JSON;
	
	public static String urlRead;
	public static final Logger logger = GetLogger.getLogger();
	
	static{
		try{
		InputStream fIn = TestServlet.class.getResourceAsStream("Config.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fIn));
		String strLine = null,temp1 = null,temp2 = null;
		while ((strLine = br.readLine()) != null)   {
			  if(strLine.contains("IP of current machine:"))
			  {
				  int index = strLine.indexOf(':');
				  temp1=strLine.substring(index+1);
			  }
			  else if (strLine.contains("Any free port on the current machine:"))
			  {
				  int index = strLine.indexOf(':');
				  temp2=strLine.substring(index+1);
			  }
			}
		urlRead="http://" + temp1 + ":" + temp2;
		logger.finest("PSRRTM: " + "alertActionURL:" + urlRead);

		br.close();
		fIn.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
    }
	
	private Threshold()
	{
		// TODO PSRRTM revisit for prod
		condition.stats="avg";
		condition.operation=">=";
		condition.periodCountToSatisfy="1";
		condition.periodCountToUnsatisfy="1";
		alertAction.type="postToEndPoint";
		alertAction.url=urlRead;
		target.isGroupExpanded=true;
	}
	
	public Threshold(String tName, String GroupID, String metricName, String value, String period)
	{
		this();
		name=tName;
		target.group.id=GroupID;
		target.metricType.id=metricName;
		condition.value=value;
		condition.periodDuration=period;
		this.generateJSON();
	}
	
	private void generateJSON()
	{
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		 JSON = gson.toJson(this);
	}
	
	public String getThresholdJSON() {
		return JSON;
	}

}	

