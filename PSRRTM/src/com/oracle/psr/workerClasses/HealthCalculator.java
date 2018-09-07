package com.oracle.psr.workerClasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Logger;

public class HealthCalculator extends Thread  {

	private static File file, fileH;
	private static FileInputStream fin;
	private static FileOutputStream fout;
	private static final Logger logger = GetLogger.getLogger();
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy HH:mm");
	private static	HashMap <String, String> customer_comments=new HashMap<>();

	public HealthCalculator() {
		super("PSRRTM Health Calculator thread");
	}

	public void run() {
		logger.info("PSRRTM: Health Calculator thread started");

		do{

			try {
				Thread.sleep(400000);
				//Parse Alerts.csv file
				file = new File(System.getProperty("user.dir") + "/Alerts.csv");
				fin = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fin));
				String strLine = null,comments=null;
				String HealthTime=sdf.format(Calendar.getInstance().getTime());
				while ((strLine = br.readLine()) != null)   {
					if(strLine.contains("VM ID,"))
					{
						continue;
					}
					else 
					{
						String Alert [] = strLine.split(",");
						ComputeVM VM = PSRRTMMain.allVMs.get(Alert[0]);
						if(customer_comments.get(VM.getCustomerName()) == null)
						{
							if(Alert[1].equals("crossed threshold")){
							comments= "Avg " + Alert[3] + " for " + VM.getVmShortName() + " crossed threshold and is " +  Alert[4];
							customer_comments.put(VM.getCustomerName(), comments );
							}
						}
						else
						{
							if(Alert[1].equals("crossed threshold")){
							comments=customer_comments.get(VM.getCustomerName());
							comments+= "," + "Avg " + Alert[3] + " for " + VM.getVmShortName() + " crossed threshold and is " + Alert[4] + " now";
							customer_comments.put(VM.getCustomerName(), comments);
							}
							else if(Alert[1].equals("uncrossed threshold")){
								comments=customer_comments.get(VM.getCustomerName());
								String tempArray[] = comments.split(",");
								String temp="Avg " + Alert[3] + " for " + VM.getVmShortName();
								if(comments.contains(temp)){
									comments = "";
									for(String i: tempArray)
									{
										if(i.contains(temp))
											continue;
										else
											comments+=i + ",";
									}
								}
								customer_comments.put(VM.getCustomerName(), comments);
								if(comments.isEmpty())
								{
									customer_comments.remove(VM.getCustomerName());
								}
							}
						}
					}
				}

				logger.info(customer_comments.toString());

				//All alerts populated in customer_comments. Write to a file
				fileH = new File(System.getProperty("user.dir") + "/Health.csv");
				fileH.createNewFile();
				fout = new FileOutputStream(fileH);

				//Write header to the file
				String header = "Customer_Name, Time, Status_code, Status_color, comments\n" ;
				byte[] contentInBytes = header.getBytes();
				fout.write(contentInBytes);

				//Write the actual customer health 
				String content = null;
				for ( String Customer: PSRRTMMain.customers_domains.keySet() ) {
					if(customer_comments.containsKey(Customer))
						content = Customer + "," + HealthTime + ",2" + ",Red Alert" + "," + customer_comments.get(Customer) + "\n";
					else
						content= Customer + "," + HealthTime + ",0" + ",No Alerts" + " \n";
					contentInBytes = content.getBytes();
					fout.write(contentInBytes);
				}
				fout.flush();
				fout.close();

				//Upload Health.csv to ObjectStore
				OSUploader.pushFileToObjectStore(fileH);

				//Cleanup and delete existing Alerts.csv as alerts have been parsed
				br.close();
				fin.close();
				file.delete();

			} catch (FileNotFoundException e) {
				logger.info("PSRRTM: Alerts.csv file not found. HealthCalculator aborting");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}while(true);

	}
}
