package com.oracle.psr.workerClasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.oracle.psr.jsonClasses.Event;
import com.oracle.psr.jsonClasses.Events;
import com.oracle.psr.jsonClasses.MetricReport;

public class AlertParser extends Thread {
	private Socket socket = null;
	private static final Logger logger = GetLogger.getLogger();
	private static File file;
	private static FileOutputStream fOut;
	private static Events ev = new Events();
	private static Gson gson = new Gson();
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
	private static MetricReport mr = new MetricReport();

	static{
		try{
			file = new File(System.getProperty("user.dir") + "/Alerts.csv");
			fOut = new FileOutputStream(file);
			file.createNewFile();
			String header = "VM ID, Alert_Time, Metric type, Metric Value\n" ;
			byte[] contentInBytes = header.getBytes();
			fOut.write(contentInBytes);
			fOut.flush();
			fOut.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public AlertParser(Socket socket) {
		super("PSRRTM Alert Parser thread");
		this.socket = socket;
	}

	private static double getCurrentValue(ComputeVM tempVM, String metricID)
	{
		String requestParams = "/monitoring/" + tempVM.getDomainName() + "/.oracle/api/v1/metricReports?subjectIds=";
		requestParams+= tempVM.getVmID() + "&sinceDuration=PT5M&precision=PT5M&metricTypeIds=";
		requestParams+= metricID;

		logger.finest("PSRRTM: Sending metricReport request: " + requestParams);
		String metric=CommonCMSClass.sendGETRequestToCMS(requestParams);
		logger.finest("PSRRTM: metricReport response: " + metric);

		mr = gson.fromJson(metric,MetricReport.class);
		return mr.getLastAvgValue(metricID);
	}

	private static void writeAlertToFile(String content) throws Exception
	{
		if (!file.exists()) {
			file.createNewFile();
			fOut = new FileOutputStream(file);
			String header = "VM ID, Alert type, Alert_Time, Metric type, Metric Value\n";
			byte[] contentInBytes = header.getBytes();
			fOut.write(contentInBytes);
		}
		else
			fOut = new FileOutputStream(file,true);

		byte[] contentInBytes = content.getBytes();
		fOut.write(contentInBytes);
		fOut.flush();
		fOut.close();

	}

	public void run() {
		try {
			String line = "", json="";
			String AlertTime=sdf.format(Calendar.getInstance().getTime());
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while ((line = br.readLine()) != null){
				logger.finest(line);
				json = line;
			}
			socket.close();
			ev = gson.fromJson(json,Events.class);

			for(Event evt: ev.events)
			{
				String alertVMID=evt.properties.subjectId;
				if(evt.type.equals("changedToSatisfied")){
					if(PSRRTMMain.allVMs.containsKey(alertVMID)){
						ComputeVM tempVM= PSRRTMMain.allVMs.get(alertVMID);
						//Get the avg vlaue of the metric when threshold was crossed
						double avgValue = getCurrentValue(tempVM, evt.properties.metricTypeId);

						//Write the alert into a file
						String content = tempVM.getVmID() + "," + "crossed threshold" + "," + AlertTime + "," + evt.properties.metricTypeId + "," + String.format( "%.2f",avgValue) + "\n" ;
						logger.info("PSRRTM: Incoming Alert: " + content);
						writeAlertToFile(content);
					}
				}
				else if(evt.type.equals("changedToUnsatisfied")){
					if(PSRRTMMain.allVMs.containsKey(alertVMID)){
						ComputeVM tempVM= PSRRTMMain.allVMs.get(alertVMID);
						//Get the avg vlaue of the metric when threshold was crossed
						double avgValue = getCurrentValue(tempVM, evt.properties.metricTypeId);

						//Write the alert into a file
						String content = tempVM.getVmID() + "," + "uncrossed threshold" + "," + AlertTime + "," + evt.properties.metricTypeId + "," + avgValue + "\n" ;
						logger.info("PSRRTM: Incoming Alert: " + content);
						writeAlertToFile(content);
					}						
				}
				else{
					logger.info("PSRRTM: Incoming Alert: with event type as " +  evt.type);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
