package com.woopra.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.woopra.java.sdk.WoopraEvent;
import com.woopra.java.sdk.WoopraTracker;
import com.woopra.java.sdk.WoopraTrackerEE;

public class Test extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
	    response.setCharacterEncoding( "UTF-8" );
	    PrintWriter out;
	    
	    
	    //Tracker, config, and set cookie
	    WoopraTrackerEE woopra = new WoopraTrackerEE(request, response);
	    woopra.config(new Object[][] {{WoopraTrackerEE.DOMAIN, "4ltrophy.campus.ecp.fr", WoopraTrackerEE.IP_ADDRESS, request.getRemoteAddr()}});
	    //woopra.setWoopraCookie();
	    
	    //Identify
	    
	    //Event
	    WoopraEvent event = new WoopraEvent("test event", new Object[][]{{"prop1", "value1"}, {"prop2", 0}});
	    woopra.track(event, true);
	    
	    
	    
		try {
			out = response.getWriter();
			out.println("<!DOCTYPE html>");
		    out.println("<html>");
		    out.println("<head>");
		    out.println("<meta charset=\"utf-8\" />");
		    out.println("<title>Test</title>");
		    
		    
		    //WOOPRA CODE GOES HERE
		    
		    //out.println(woopra.woopraCode());
		    
		    
		    
		    out.println("</head>");
		    out.println("<body>");
		    out.println("<p>Ceci est une page générée depuis une servlet.</p>");
		    out.println(woopra.customConfig);
		    out.println(woopra.currentConfig);
		    out.println("</body>");
		    out.println("</html>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	
	}

}
