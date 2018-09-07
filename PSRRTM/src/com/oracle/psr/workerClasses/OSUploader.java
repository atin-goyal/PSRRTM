package com.oracle.psr.workerClasses;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.net.ssl.SSLSession;

import org.apache.commons.codec.binary.Base64;

import com.oracle.psr.TestServlet;
import weblogic.net.http.HttpsURLConnection;
import weblogic.security.SSL.HostnameVerifier;


public class OSUploader {
	private static final Logger logger = GetLogger.getLogger();
	private static String ObjStoreEndpoint;
	private static String ObjStoreUsername;
	private static String ObjStorePassword;
	
	static{
		try{
		InputStream fIn = TestServlet.class.getResourceAsStream("Config.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fIn));
		String strLine = null;
		while ((strLine = br.readLine()) != null)   {
			  if(strLine.contains("ObjectStore Endpoint:"))
			  {
				  int index = strLine.indexOf(':');
				  ObjStoreEndpoint=strLine.substring(index+1);
				  logger.finest("PSRRTM: " + "ObjStoreEndpoint:" + ObjStoreEndpoint);
			  }
			  if(strLine.contains("ObjectStore Username:"))
			  {
				  int index = strLine.indexOf(':');
				  ObjStoreUsername=strLine.substring(index+1);
				  logger.finest("PSRRTM: " + "ObjStoreUsername:" + ObjStoreUsername);
			  }
			  if(strLine.contains("ObjectStore Password:"))
			  {
				  int index = strLine.indexOf(':');
				  ObjStorePassword=strLine.substring(index+1);
				  logger.finest("PSRRTM: " + "ObjStorePassword:" + ObjStorePassword);
			  }
			}
		br.close();
		fIn.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
    }
	

	public static StringBuilder pushFileToObjectStore(File fileName) {
	    
	    HostnameVerifier allHostsValid = new HostnameVerifier() {
	    	@Override
	    	public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
		
		String reqUrl = ObjStoreEndpoint + fileName.getName();
			StringBuilder responseBody = new StringBuilder();
			Base64 encoder = new Base64();
			try{
				URL tURL = new URL(reqUrl);
				HttpsURLConnection tURLConnection = (HttpsURLConnection)tURL.openConnection();
				tURLConnection.setHostnameVerifier(allHostsValid);
				tURLConnection.setRequestMethod("PUT");
				logger.info("PSRRTM: " + "Sending PUT request: " + reqUrl);
				String userpass = ObjStoreUsername + ":" + ObjStorePassword;
				String encoded = encoder.encodeToString(userpass.getBytes(StandardCharsets.UTF_8));
				tURLConnection.setRequestProperty ("Authorization", "Basic " + encoded);
				tURLConnection.setUseCaches(false);
				tURLConnection.setDoInput(true);
				tURLConnection.setDoOutput(true);
				tURLConnection.connect(); 
				
				OutputStream os = tURLConnection.getOutputStream();
				Files.copy(fileName.toPath(), os);
				os.close();
				
				int responseCode = tURLConnection.getResponseCode();
				logger.info("PSRRTM: " + "Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(
				        new InputStreamReader(tURLConnection.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					responseBody.append(inputLine);
					responseBody.append('\r');
				}
				in.close();

				//logger.info("PSRRTM: " + "Response " + responseBody.toString());
			} catch(Exception e)
			{
				e.printStackTrace();
			}
			return responseBody;
	}
	
	public static StringBuilder readFileFromObjectStore(String fileName) {
		String reqUrl = ObjStoreEndpoint + fileName;
		StringBuilder responseBody = new StringBuilder();
		Base64 encoder = new org.apache.commons.codec.binary.Base64(0, null);
		try{			
			URL tURL = new URL(reqUrl);
			HttpURLConnection tURLConnection = (HttpURLConnection)tURL.openConnection();
			tURLConnection.setRequestMethod("GET");
			logger.info("PSRRTM: " + "Sending GET request: " + reqUrl);
			String userpass = ObjStoreUsername + ":" + ObjStorePassword;
			String encoded = encoder.encodeToString(userpass.getBytes(StandardCharsets.UTF_8));  //Java 8
			tURLConnection.setRequestProperty ("Authorization", "Basic " + encoded);
			tURLConnection.setUseCaches(false);
			tURLConnection.setDoInput(true);
			tURLConnection.setDoOutput(true);
			tURLConnection.connect();

			int responseCode = tURLConnection.getResponseCode();
			logger.info("PSRRTM: " + "Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(tURLConnection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				responseBody.append(inputLine);
				responseBody.append('\r');
			}
			in.close();

			//print result
			logger.info("PSRRTM: " + "Response " + responseBody.toString());
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return responseBody;
	}

}
