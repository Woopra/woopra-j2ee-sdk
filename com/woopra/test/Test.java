package com.woopra.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.woopra.java.sdk.WoopraEvent;
import com.woopra.java.sdk.WoopraTracker;
import com.woopra.java.sdk.WoopraUser;

public class Test extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
	    response.setCharacterEncoding( "UTF-8" );
	    PrintWriter out;
	    
	    
	    
	    WoopraTracker woopra = new WoopraTracker(request, response);
	    WoopraUser user = new WoopraUser();
	    WoopraEvent event = new WoopraEvent();
	    
	    user.setProperty("email", "user@test.com");
	    user.setProperty("name", "Test User");
	    event.setName("test event");
	    event.setProperty("prop1", "value1");
	    woopra.setWoopraCookie();
	    
		try {
			out = response.getWriter();
			out.println("<!DOCTYPE html>");
		    out.println("<html>");
		    out.println("<head>");
		    out.println("<meta charset=\"utf-8\" />");
		    out.println("<title>Test</title>");
		    
		    
		    //WOOPRA CODE GOES HERE
		    woopra.config(WoopraTracker.DOMAIN, "4ltrophy.campus.ecp.fr");
		    woopra.identify(user);
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
