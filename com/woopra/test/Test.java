package com.woopra.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.woopra.java.sdk.WoopraEvent;
import com.woopra.java.sdk.WoopraTracker;

public class Test extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
	    response.setCharacterEncoding( "UTF-8" );
	    PrintWriter out;
	    
	    
	    //Tracker, config, and set cookie
	    WoopraTracker woopra = new WoopraTracker(request, response);
	    woopra.config(new Object[][] {{WoopraTracker.DOMAIN, "4ltrophy.campus.ecp.fr"}});
	    woopra.setWoopraCookie();
	    
	    //Identify
	    woopra.identify(new String[][] {
	        {"name", "Antoine"},
	        {"email", "antoine@woopra.com"},
	        {"company", "My Business"}
	    });
	    
	    //Event
	    WoopraEvent event = new WoopraEvent("test event", new Object[][]{{"prop1", "value1"}, {"prop2", 0}});
	    
	    
		try {
			out = response.getWriter();
			out.println("<!DOCTYPE html>");
		    out.println("<html>");
		    out.println("<head>");
		    out.println("<meta charset=\"utf-8\" />");
		    out.println("<title>Test</title>");
		    
		    
		    //WOOPRA CODE GOES HERE
		    
		    woopra.track(event);
		    woopra.track();
		    woopra.woopraCode();
		    
		    
		    out.println("</head>");
		    out.println("<body>");
		    out.println("<p>Ceci est une page générée depuis une servlet.</p>");
		    out.println("</body>");
		    out.println("</html>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	
	}

}
