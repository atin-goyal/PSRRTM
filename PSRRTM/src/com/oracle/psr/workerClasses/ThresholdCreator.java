package com.oracle.psr.workerClasses;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.oracle.psr.TestServlet;
import com.oracle.psr.jsonClasses.Group;
import com.oracle.psr.jsonClasses.Groups;
import com.oracle.psr.jsonClasses.Threshold;
import com.oracle.psr.jsonClasses.Thresholds;

public class ThresholdCreator extends Thread{

	public static final Logger logger = GetLogger.getLogger();
	public static HashMap <String, List <String>> domain_groups =  new HashMap<>();
	private static Gson gson = new Gson();
	public static List <ThresholdDefinition> inputThresholds = new ArrayList <>();


	static{
		try{
			InputStream fIn = TestServlet.class.getResourceAsStream("Config.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fIn));
			String strLine = null,temp1 = null;
			String temp2 [] = new String [3];
			int lineCount =0;
			while ((strLine = br.readLine()) != null)   {
				ThresholdDefinition temp3 = new ThresholdDefinition();
				lineCount++;
				if(strLine.contains("Threshold:") && !strLine.contains("#"))
				{
					int index = strLine.indexOf(':');
					temp1=strLine.substring(index+1);
					if (temp1 == null || temp1.isEmpty())
					{
						logger.severe("PSRRTM: invalid entry in Config.txt at line " + lineCount + ". Please check");
						continue;
					}
					else
					{
						temp2= temp1.split(",");
						temp3.metric=temp2[0];
						temp3.value=temp2[1];
						temp3.period=temp2[2];
						inputThresholds.add(temp3);
						logger.info("PSRRTM: Threshold definition Added:" + temp3.toString());
					}
				}
			}

			br.close();
			fIn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void deleteThresholds(String domainName)
	{
		String requestParams = "/monitoring/" + domainName + "/.oracle/api/v1/metricThresholds";
		String response = null;
		Thresholds tr = null;

		// Delete existing thresholds created by PSRRTM
		response = CommonCMSClass.sendGETRequestToCMS(requestParams);
		logger.finest(response);
		try{
			tr = gson.fromJson(response,Thresholds.class);
			for(Threshold i:tr.items)
			{
				if(i.name.contains("PSRRTM"))
				{
					logger.info("PSRRTM: Threshold " + i.name + " exists. Deleting");
					String delrequestParams = requestParams + "/" + i.id;
					response = CommonCMSClass.sendDELETERequestToCMS(delrequestParams);
				}
			}
		}
		catch(Exception e)
		{
			logger.warning("PSRRTM: json parsing failure in createThreshold");
		}
	}


	public static void createThreshold(String domainName, Threshold t)
	{
		String requestParams = "/monitoring/" + domainName + "/.oracle/api/v1/metricThresholds";
		String response = null;

		//Create the theshold
		response = CommonCMSClass.sendPOSTRequestToCMS(requestParams,t.getThresholdJSON());
		logger.info("PSRRTM: Creating threshold.");
		logger.finest("PSRRTM: URL: " + requestParams);
		logger.info("PSRRTM: JSON: " + t.getThresholdJSON());
		logger.finest("PSRRTM: response: " + response);
	}

	public static String createGroup(String domainName, Group g)
	{
		String requestParams = "/monitoring/" + domainName + "/.oracle/api/v1/metricSubjectGroups";
		String response = null;
		Group gr = null;
		String gid = null;

		//Check if the group already exists. If yes get the group ID
		String checkrequestParams = requestParams + "?name=" + g.name;
		response = CommonCMSClass.sendGETRequestToCMS(checkrequestParams);
		logger.finest("PSRRTM: response: " + response);

		try{
			gr = gson.fromJson(response,Groups.class).items.get(0);
			gid = gr.id;
		}
		catch(Exception e)
		{
			logger.warning("PSRRTM: Either " + g.name + " Group does not exist or json parsing failure.");
		}

		//if group doesn't exist, create it
		if(gid==null || gid.isEmpty()){
			response = CommonCMSClass.sendPOSTRequestToCMS(requestParams,g.getGroupJSON());
			logger.finest("PSRRTM: Creating Group.");
			logger.finest("PSRRTM: URL: " + requestParams);
			logger.finest("PSRRTM: JSON: " + g.getGroupJSON());
			logger.finest("PSRRTM: response: " + response);


			try{
				gr = gson.fromJson(response,Group.class);
				gid = gr.id;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		return gid;
	}


	public ThresholdCreator() {
		super("PSRRTM Threshold Creator thread");
	}


	public void run() {
		logger.info("PSRRTM: " + "Threshold Creator started");
		Group tempG_compute = new Group("PSRRTM_monitoring_Compute group","Compute");
		Group tempG_jaas = new Group("PSRRTM_monitoring_JAAS group","JAAS");
		Group tempG_dbaas = new Group("PSRRTM_monitoring_DBAAS_group","DBAAS");
		Threshold t = null;

		//Start creating groups
		logger.info("PSRRTM: Started Creating groups");
		for ( String Customer: PSRRTMMain.customers_domains.keySet() ) {
			for ( String domain: PSRRTMMain.customers_domains.get(Customer))
			{
				List <String> temp = new ArrayList<>();
				temp.add(createGroup(domain,tempG_compute));
				temp.add(createGroup(domain,tempG_jaas));
				temp.add(createGroup(domain,tempG_dbaas));
				domain_groups.put(domain, temp);
			}
		}

		logger.finest("PSRRTM: Groups= " + domain_groups.toString());
		
		// Create thresholds
		logger.info("PSRRTM: Started Creating thresholds");
		for ( String Customer: PSRRTMMain.customers_domains.keySet() ) {
			for ( String domain: PSRRTMMain.customers_domains.get(Customer))
			{
				//Delete existing PSRRTM thresholds before creating new ones
				deleteThresholds(domain);
				for (ThresholdDefinition td:inputThresholds)
				{
				List <String> temp = domain_groups.get(domain);
				t = new Threshold("PSRRTM_" + domain + "_compute_Threshold_for_" + td.metric,temp.get(0),td.metric,td.value,td.period);
				createThreshold(domain,t);
				t = new Threshold("PSRRTM_" + domain + "_jaas_Threshold_for_" + td.metric,temp.get(1),td.metric,td.value,td.period);
				createThreshold(domain,t);
				t = new Threshold("PSRRTM_" + domain + "_dbaas_Threshold_for_" + td.metric,temp.get(2),td.metric,td.value,td.period);
				createThreshold(domain,t);
				}
			}
		}
	}
}

class ThresholdDefinition{
	public String metric,value,period;
	
	public String toString()
	{
		return metric + "," + value + "," + period;
	}
}
