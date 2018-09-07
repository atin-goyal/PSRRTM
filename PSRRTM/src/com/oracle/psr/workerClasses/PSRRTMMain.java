package com.oracle.psr.workerClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.oracle.psr.TestServlet;
import com.oracle.psr.jsonClasses.MetricSubjects;
import com.oracle.psr.jsonClasses.Threshold;

public class PSRRTMMain {

	public static HashMap <String, ComputeVM> allVMs =  new  HashMap <>();
	public static HashMap <String, List <String>> domainVMs = new  HashMap<>();
	public static HashMap <String, List <String>> customers_domains = new  HashMap<>();
	public static final Logger logger = GetLogger.getLogger();
	static{
		try{
			InputStream fIn = TestServlet.class.getResourceAsStream("domain_names.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fIn));
			String strLine = null;
			int lineCount=0;
			List<String> temp;
			String customer_name = null, domain_name = null;
			while ((strLine = br.readLine()) != null)   {
				lineCount++;
				if(strLine.contains("Customer Name:"))
				{
					continue;
				}
				else
				{
					int index = strLine.indexOf(':');
					customer_name=strLine.substring(0,index);
					domain_name=strLine.substring(index+1);
					if (customer_name == null || customer_name.isEmpty() || domain_name == null || domain_name.isEmpty())
					{
						logger.severe("PSRRTM: invalid entry in domain_names.txt at line " + lineCount + ". Please check");
						continue;
					}
					else
					{
						temp= Arrays.asList(domain_name.split(","));
						customers_domains.put(customer_name, temp);
						logger.finest("PSRRTM: Customer Added:" + customers_domains.toString());
					}
				}
			}
			br.close();
			fIn.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static List<String> getAllVMsforaDomain(String customerName, String domainName)
	{
		List<String> temp = new ArrayList<>();
		int count=0;
		ComputeVM tempVM = null;
		String requestParams = "/monitoring/" + domainName +"/.oracle/api/v1/metricSubjects?subjectTypeIds=compute.lvm&filterOutDeleted=true";
		Gson gson = new Gson();
		MetricSubjects ms = new MetricSubjects();
		do{
			ms = gson.fromJson(CommonCMSClass.sendGETRequestToCMS(requestParams),MetricSubjects.class);
			logger.finest("PSRRTM: "  + ms.getJSON());
			requestParams=ms.getNextLink();
			temp.addAll(ms.getAllVMIDs());
			for(String vmID: ms.getAllVMIDs()){
				tempVM = ms.getComputeVM(vmID);
				tempVM.setCustomerName(customerName);
				tempVM.setDomainName(domainName);
				String tempShortName = domainName.toUpperCase() + "_" + tempVM.getVMLabel().toUpperCase()+ "_" + count;
				tempVM.setVmShortName(tempShortName.replace(" ", "_"));
				count++;
				allVMs.put(vmID, tempVM);
			}
		}
		while(requestParams!=null);
		return temp;
	}

	public static void start() {
		logger.finest("PSRRTM: " + "PSRRTMMain started");
		List<String> temp = null;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+00"));
		for ( String Customer: customers_domains.keySet() ) {
			for ( String domain: customers_domains.get(Customer))
			{
				temp=PSRRTMMain.getAllVMsforaDomain(Customer,domain);
				domainVMs.put(domain, temp);
			}
		}
		
		logger.finest("PSRRTM: All Customers and Domains" + customers_domains.toString());
		logger.finest("PSRRTM: All Domains and VMIds" + domainVMs.toString());
		logger.finest("PSRRTM: All VMs" + allVMs.toString());
		
		//spawn new thread to start creating threshold
		new ThresholdCreator().start();

		//spawn new thread to listen for alerts from CMS
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				ServerSocket serverSocket = null;
				try{
					int portNumber=Integer.parseInt(Threshold.urlRead.substring(Threshold.urlRead.lastIndexOf(':') + 1));
					serverSocket = new ServerSocket(portNumber);
					logger.info("PSRRTM: Listener Running . . .");
					while (true) {
			            new AlertParser(serverSocket.accept()).start();
			        }
				}
				catch (Exception e){
					e.printStackTrace();
				}
				finally{
					if(serverSocket!=null){
						try {
							serverSocket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}

			}
		}).start();

		//spawn new thread to get VM Metrics for daily monitoring
		new DailyMetricsGenerator().start();
		
		//start HealthCalculator thread to get customer health every 5 mins
		new HealthCalculator().start();

	}

}
