package com.oracle.psr.workerClasses;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.oracle.psr.TestServlet;

public class CommonCMSClass {

	private static String CMSEndpoint;
	private static String CMSUsername;
	private static String CMSPassword;
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final Logger logger = GetLogger.getLogger();
	
	static{
		try{
		InputStream fIn = TestServlet.class.getResourceAsStream("Config.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fIn));
		String strLine = null;
		while ((strLine = br.readLine()) != null)   {
			  if(strLine.contains("CMS Endpoint:"))
			  {
				  int index = strLine.indexOf(':');
				  CMSEndpoint=strLine.substring(index+1);
				  logger.finest("PSRRTM: " + "CMSEndpoint:" + CMSEndpoint);
			  }
			  if(strLine.contains("CMS Username:"))
			  {
				  int index = strLine.indexOf(':');
				  CMSUsername=strLine.substring(index+1);
				  logger.finest("PSRRTM: " + "CMSUsername:" + CMSUsername);
			  }
			  if(strLine.contains("CMS Password:"))
			  {
				  int index = strLine.indexOf(':');
				  CMSPassword=strLine.substring(index+1);
				  logger.finest("PSRRTM: " + "CMSPassword:" + CMSPassword);
			  }
			}
		br.close();
		fIn.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
    }

	public static String sendGETRequestToCMS(String reqParams) {
		String reqUrl = CMSEndpoint + reqParams;
		StringBuilder responseBody = new StringBuilder();
		GenericUrl url = new GenericUrl(reqUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuthentication(CMSUsername, CMSPassword);
		headers.setContentType("application/json");
		HttpResponse response = null;
		try{
			HttpRequest request = HTTP_TRANSPORT.createRequestFactory().buildGetRequest(url);
			request.setHeaders(headers);
			response = request.execute();
			if (response.isSuccessStatusCode())
			{
				InputStream is = response.getContent();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;
				while((line = rd.readLine()) != null) {
					responseBody.append(line);
					responseBody.append('\r');
				}
				rd.close();
			}
			else
			{
				logger.severe("PSRRTM: " + "Error while sending request" + request.toString());
				logger.severe("PSRRTM: " + "StatusCode: " + response.getStatusCode() + " Message: " + response.getStatusMessage());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return responseBody.toString();
	}
	
	public static String sendDELETERequestToCMS(String reqParams) {
		String reqUrl = CMSEndpoint + reqParams;
		StringBuilder responseBody = new StringBuilder();
		GenericUrl url = new GenericUrl(reqUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuthentication(CMSUsername, CMSPassword);
		headers.setContentType("application/json");
		HttpResponse response = null;
		try{
			HttpRequest request = HTTP_TRANSPORT.createRequestFactory().buildDeleteRequest(url);
			request.setHeaders(headers);
			response = request.execute();
			if (response.isSuccessStatusCode())
			{
				InputStream is = response.getContent();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;
				while((line = rd.readLine()) != null) {
					responseBody.append(line);
					responseBody.append('\r');
				}
				rd.close();
			}
			else
			{
				logger.severe("PSRRTM: " + "Error while sending request" + request.toString());
				logger.severe("PSRRTM: " + "StatusCode: " + response.getStatusCode() + " Message: " + response.getStatusMessage());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return responseBody.toString();
	}
	
	public static String sendPOSTRequestToCMS(String reqParams , String requestBody) {
		String reqUrl = CMSEndpoint + reqParams;
		StringBuilder responseBody = new StringBuilder();
		GenericUrl url = new GenericUrl(reqUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuthentication(CMSUsername, CMSPassword);
		headers.setContentType("application/json");
		HttpResponse response = null;
		try{
			HttpRequest request = HTTP_TRANSPORT.createRequestFactory().buildPostRequest(url, ByteArrayContent.fromString("application/json", requestBody));
			logger.finest("PSRRTM: " + "Sending request: " + request.getUrl() + " " + requestBody);
			request.setHeaders(headers);
			response = request.execute();
			if (response.isSuccessStatusCode())
			{
				InputStream is = response.getContent();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;
				while((line = rd.readLine()) != null) {
					responseBody.append(line);
					responseBody.append('\r');
				}
				rd.close();
			}
			else
			{
				logger.severe("PSRRTM: " + "Error while sending request" + request.toString());
				logger.severe("PSRRTM: " + "StatusCode: " + response.getStatusCode() + " Message: " + response.getStatusMessage());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return responseBody.toString();
	}

}
