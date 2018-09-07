package com.oracle.psr.workerClasses;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.oracle.psr.jsonClasses.MetricReport;

public class DailyMetricsGenerator  extends Thread{

	public static final Logger logger = GetLogger.getLogger();
	public static String metricIDs="compute.lvm.CPU.PERCENT,compute.lvm.MEMORY.CURRENT_PERCENT,compute.lvm.NETWORK.RX_BYTES_PER_SEC,compute.lvm.NETWORK.TX_BYTES_PER_SEC,compute.lvm.IOSTAT.READ,compute.lvm.IOSTAT.WRITE";
	private static Gson gson = new Gson();
	private static File file, fileH,fileD;
	private static FileOutputStream foutH,fout;
	
	public DailyMetricsGenerator() {
		super("PSRRTM Daily Metrics Generator thread");
	}

	public void run() {
		logger.info("PSRRTM: Daily Metrics Generator thread started");
		Boolean doneForToday = false;
		
		do{

			try {
				Calendar Today = Calendar.getInstance();
				Calendar PreviousDay = Calendar.getInstance();
				PreviousDay.add(Calendar.DATE, -1);

				Date currentTime = Today.getTime();
				MetricReport mr = new MetricReport();
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
				
				//Create date object for cutoff time
				//TODO set correct cutoff time for prod
				Today.set(Calendar.HOUR_OF_DAY, 3); Today.set(Calendar.MINUTE, 00); Today.set(Calendar.SECOND, 00);
				Date metricsCutoffTime=Today.getTime();
				
				//Create date object for start time
				Today.set(Calendar.HOUR_OF_DAY, 1); Today.set(Calendar.MINUTE, 00); Today.set(Calendar.SECOND, 00);
				Date metricsStartTime=Today.getTime();
				
				
				logger.info("PSRRTM MetricsCutoffTime: " + metricsCutoffTime.toString() + ", MetricsStartTime: " + metricsStartTime.toString());

				//Check if current time is between start and cutoff time
				if(!doneForToday && currentTime.before(metricsCutoffTime) && currentTime.after(metricsStartTime))
				{
					logger.info("PSRRTM " + currentTime.toString() + ": Daily metrics collection starting... " );
					
					
					//Initialize master_table.csv
					fileH = new File(System.getProperty("user.dir") + "/master_table.csv");
					if(fileH.exists())
					{
						fileD = new File(System.getProperty("user.dir") + "/Archive/" + sdf.format(PreviousDay.getTime()));
						fileD.mkdirs();
						fileH.renameTo(new File(System.getProperty("user.dir") + "/Archive/" + sdf.format(PreviousDay.getTime()) + "/master_table.csv"));
					}
					fileH.createNewFile();
					foutH = new FileOutputStream(fileH);
					
					//Write header to the master_table.csv
					String header = "POD_NAME,POD_SHAPE,SERVICE_TYPE,NO_OF_TENANTS,DATACENTER,POD_FQN,POD_VERSION,POD_TYPE,CUSTOMER_NAME,POD_HEALTH_STATUS\n" ;
					byte[] contentInBytes = header.getBytes();
					foutH.write(contentInBytes);

					//Initialize daily_metrics.csv
					file = new File(System.getProperty("user.dir") + "/daily_metrics.csv");
					if(file.exists()){
						fileD = new File(System.getProperty("user.dir") + "/Archive/" + sdf.format(PreviousDay.getTime()));
						fileD.mkdirs();
						file.renameTo(new File(System.getProperty("user.dir") + "/Archive/" + sdf.format(PreviousDay.getTime()) + "/daily_metrics.csv"));
					}
					file.createNewFile();
					fout = new FileOutputStream(file);
					
					//Write header to the daily_metrics.csv
					header = "POD_NAME,DATA_DATE,AVG_CPU_UTILIZATION,MIN_CPU_UTILIZATION,MAX_CPU_UTILIZATION,PERCENTILE_90TH_CPU,AVG_MEM_UTILIZATION,"
							+ "MIN_MEM_UTILIZATION,MAX_MEM_UTILIZATION,PERCENTILE_90TH_MEM,AVG_TOTAL_NW_IO,MIN_TOTAL_NW_IO,MAX_TOTAL_NW_IO,PERCENTILE_90TH_TOTAL_NW_IO,"
							+ "MAX_NW_INTERFACE_UTILIZATION,AVG_TOTAL_DISK_IO,MIN_TOTAL_DISK_IO,MAX_TOTAL_DISK_IO,PERCENTILE_90TH_TOTAL_DISK_IO,MAX_"
							+ "SINGLE_DISK_IO,MAX_TOTAL_DISK_SPACE_UTIL,POD_FQN,CUSTOMER_NAME\n" ;
					contentInBytes = header.getBytes();
					fout.write(contentInBytes);
					
					for ( String Customer: PSRRTMMain.customers_domains.keySet() ) {
						for ( String domain: PSRRTMMain.customers_domains.get(Customer)){
							for (String vm: PSRRTMMain.domainVMs.get(domain)){
							
							ComputeVM tempVM = PSRRTMMain.allVMs.get(vm);
							String requestParams = "/monitoring/" + tempVM.getDomainName() + "/.oracle/api/v1/metricReports?subjectIds="
									+ tempVM.getVmID() + "&sinceDuration=PT1D&precision=PT1D&metricTypeIds=" +  metricIDs;
							
							logger.info("PSRRTM: Sending metricReport request: " + requestParams);
							String metric=CommonCMSClass.sendGETRequestToCMS(requestParams);
							logger.info("PSRRTM: metricReport response: " + metric);
							
							mr = gson.fromJson(metric,MetricReport.class);

							//Write the actual pod master table info
							StringBuilder content = new StringBuilder(tempVM.getVmShortName() + ",,External Compute," + tempVM.getVmZone().toUpperCase() + "," + tempVM.getVmOrchestration()) ;
							content.append(",," + tempVM.getVmType() + "," + tempVM.getCustomerName() + "\n");
							contentInBytes = content.toString().getBytes();
							foutH.write(contentInBytes);
							
							//Write the actual daily metrics info 
							content = new StringBuilder(tempVM.getVmShortName() + "," + sdf.format(currentTime) + ",");
							
							//CPU
							content.append(String.format( "%.2f",mr.getAvgValue("compute.lvm.CPU.PERCENT")) + "," + String.format( "%.2f",mr.getMinValue("compute.lvm.CPU.PERCENT")) + 
									"," + String.format( "%.2f",mr.getMaxValue("compute.lvm.CPU.PERCENT")) + ",,");
							//Mem
							content.append(String.format( "%.2f",mr.getAvgValue("compute.lvm.MEMORY.CURRENT_PERCENT")) + "," + String.format( "%.2f",mr.getMinValue("compute.lvm.MEMORY.CURRENT_PERCENT")) + 
									"," + String.format( "%.2f",mr.getMaxValue("compute.lvm.MEMORY.CURRENT_PERCENT")) + ",,");
							//NW (sum of rx and tx and convert from bytes/s to MB/s)
							content.append(String.format( "%.2f",(((mr.getAvgValue("compute.lvm.NETWORK.RX_BYTES_PER_SEC") + mr.getAvgValue("compute.lvm.NETWORK.TX_BYTES_PER_SEC"))/1024)/1024)) 
									+ "," + String.format( "%.2f",(((mr.getMinValue("compute.lvm.NETWORK.RX_BYTES_PER_SEC") + mr.getMinValue("compute.lvm.NETWORK.TX_BYTES_PER_SEC"))/1024)/1024)) + 
									"," + String.format( "%.2f",(((mr.getMaxValue("compute.lvm.NETWORK.RX_BYTES_PER_SEC") + mr.getMaxValue("compute.lvm.NETWORK.TX_BYTES_PER_SEC"))/1024)/1024)) + ",,,");
							//DiskIO (sum of read and write sector/s and convert to million sector/s)
							content.append(String.format( "%.2f",((mr.getAvgValue("compute.lvm.IOSTAT.READ") + mr.getAvgValue("compute.lvm.IOSTAT.WRITE"))/1000000)) 
									+ "," + String.format( "%.2f",((mr.getMinValue("compute.lvm.IOSTAT.READ") + mr.getMinValue("compute.lvm.IOSTAT.WRITE"))/1000000)) + 
									"," + String.format( "%.2f",((mr.getMaxValue("compute.lvm.IOSTAT.READ") + mr.getMaxValue("compute.lvm.IOSTAT.WRITE"))/1000000)) + ",,,,");
							
							content.append(tempVM.getVmOrchestration() + "," + tempVM.getCustomerName() + "\n");
							contentInBytes = content.toString().getBytes();
							fout.write(contentInBytes);
							
							//TODO revise sleep
							Thread.sleep(30000);
						}
						}
					}
					
				foutH.flush();
				foutH.close();
				
				fout.flush();
				fout.close();

				//Upload master_table.csv to ObjectStore
				OSUploader.pushFileToObjectStore(fileH);
				
				//Upload daily_metrics.csv to ObjectStore
				OSUploader.pushFileToObjectStore(file);

				doneForToday = true;
				
				Thread.sleep((2*3600*1000));

				}
				else
				{
					//Wait for an hour
					logger.info("PSRRTM: " + currentTime.toString() + ": Waiting till tomo MetricsStartTime for getting daily metrics");
					Thread.sleep((1*3600*1000));
					doneForToday = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}while(true);

	}
}


