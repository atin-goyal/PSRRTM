package com.oracle.psr;

import java.io.IOException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oracle.psr.workerClasses.GetLogger;
import com.oracle.psr.workerClasses.PSRRTMMain;

import java.util.logging.Logger;

/**
 * Servlet implementation class TestServlet
 */
@WebServlet("/TestServlet")
public class TestServlet extends HttpServlet implements ServletContextListener {
	private static final long serialVersionUID = 1L;
	public static final Logger logger = GetLogger.getLogger();
      
       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		String requestParams = "/pool0lbaascp10/.oracle/api/v1/metricSubjects?subjectTypeIds=compute.lvm&filterOutDeleted=true";
//		System.out.println(CommonCMSClass.sendGETRequestToCMS(requestParams).toString());
//		String requestParams = "/pool0lbaascp10/.oracle/api/v1/metricThresholds";
//		Threshold t = new Threshold("Test Threshold atigoyal1","4921780f-2b9c-636a-e053-caa0f90a35cf","2b0b09a6-20df-4dfa-95fe-2b8850bc775e");
//		System.out.println(t.getThresholdJSON().toString());
		
//		if(!request.getParameter("thresholdValue").isEmpty())
//			Threshold.thresholdValue= request.getParameter("thresholdValue");
//		logger.info(Threshold.thresholdValue);
		
//		Threshold t = new Threshold("Test","0169272f-ed9a-409e-b858-a03fdfec3cb8","compute.lvm.MEMORY.CURRENT_PERCENT");
//		logger.info(t.getThresholdJSON());
		
		PSRRTMMain.start();
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	@Override
	public void contextInitialized(ServletContextEvent event)
	  {
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				
			}
		}).start();
	  }

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
