package com.woopra.java.sdk;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WoopraTracker {
	
	//Request, response and writer
	private HttpServletRequest request;
	private HttpServletResponse response;
	private PrintWriter out;
	
	//Event stack
	private LinkedList<WoopraEvent> events;
	
	//User variables
	private WoopraUser user;
	private boolean userUpToDate;
	
	//Is the JS tracker ready?
	private boolean trackerReady;
	
	//Configuration Arrays
	private static Hashtable<String, Object> defaultConfig = new Hashtable<String, Object>(15);
	private Hashtable<String, Object> currentConfig;
	private Hashtable<String, Object> customConfig;

	
	//Public Constructor
	public WoopraTracker(HttpServletRequest request, HttpServletResponse response) {
		
		//Request, response, and writer
		this.request = request;
		this.response = response;
		try {
			this.out = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Tracker is not ready
		this.trackerReady = false;
		
		//Set the default Configuration
		WoopraTracker.defaultConfig.put("domain", "");
		WoopraTracker.defaultConfig.put("cookieName", "wooTracker");
		WoopraTracker.defaultConfig.put("cookieDomain", "");
		WoopraTracker.defaultConfig.put("cookiePath", "/");
		WoopraTracker.defaultConfig.put("ping", true);
		WoopraTracker.defaultConfig.put("pingInterval", 12000);
		WoopraTracker.defaultConfig.put("idleTimeout", 300000);
		WoopraTracker.defaultConfig.put("downloadTracking", true);
		WoopraTracker.defaultConfig.put("outgoingTracking", true);
		WoopraTracker.defaultConfig.put("downloadPause", 200);
		WoopraTracker.defaultConfig.put("outgoingPause", 400);
		WoopraTracker.defaultConfig.put("ignoreQueryUrl", true);
		WoopraTracker.defaultConfig.put("hideCampaign", false);
		WoopraTracker.defaultConfig.put("ipAddress", "");
		WoopraTracker.defaultConfig.put("cookieValue", "");
		
		//The current configuration is the default
		this.currentConfig = WoopraTracker.defaultConfig;
		
		//Set domain, the cookieDomain, and the IP of the client
		this.currentConfig.put("domain", request.getServerName());
		this.currentConfig.put("cookieDomain", request.getServerName());
		this.currentConfig.put("ip_address", request.getRemoteAddr());
		
		Cookie[] cookies = request.getCookies();
		for(int i = 0; i < cookies.length; i++) {
			if(cookies[i].getName() == this.currentConfig.get("cookieName")) {
				this.currentConfig.put("cookieValue", cookies[i].getValue());
			}
		}
		if (this.currentConfig.get("cookieValue") == "") {
			this.currentConfig.put("cookieValue", WoopraTracker.randomCookie());
		}
		
		this.userUpToDate = true;
	}
	
	//In java, define setters instead!
	public WoopraTracker config() {
		
		return this;
	}
	
	public WoopraTracker setDomain(String domain) {
		this.currentConfig.put("domain", domain);
		if(this.customConfig == null) {
			this.customConfig = new Hashtable<String, Object>(15);
		}
		this.customConfig.put("domain", domain);
		return this;
	}
	
	
	public WoopraTracker identify(WoopraUser user) {
		this.user = user;
		this.userUpToDate = false;
		return this;
	}
	
	public WoopraTracker track() {
		if(this.trackerReady && this.out != null) {
			this.out.println("   <script>");
			this.printJavaScriptConfiguration();
			this.printJavaScriptIndentification();
			this.out.println("      woopra.track();");
			this.out.println("   </script>");
		} else {
			this.events.add(new WoopraEvent());
		}
		return this;
	}
	
	public WoopraTracker track(boolean backEndProcessing) {
		if(backEndProcessing) {
			this.woopraHttpRequest(true, new WoopraEvent());
			
			return this;
		} else {
			return this.track();
		}
	}
	
	public WoopraTracker track(WoopraEvent event) {
		if(this.events == null) {
			this.events = new LinkedList<WoopraEvent>();
		}
		this.events.add(event);
		if(this.trackerReady && this.out != null) {
			this.printJavaScriptConfiguration();
			this.printJavaScriptIndentification();
			this.printJavaScriptEvents();
		}
		return this;
	}
	
	public WoopraTracker track(WoopraEvent event, boolean backEndProcessing) {
		if(backEndProcessing) {
			this.woopraHttpRequest(true, event);
			return this;
		} else {
			return this.track(event);
		}
	}
	
	
	public void push() {
		if(this.trackerReady && this.out != null) {
			this.out.println("   <script>");
			this.printJavaScriptConfiguration();
			this.printJavaScriptIndentification();
			this.out.println("      woopra.push();");
			this.out.println("   </script>");
		}
	}
	
	public void push(boolean backEndProcessing) {
		if(backEndProcessing) {
			
			this.woopraHttpRequest(false, new WoopraEvent());
			
		} else {
			this.push();
		}
	}
	
	private void printJavaScriptConfiguration() {
		if(this.customConfig != null) {
			String JSarray = "{";
			for (Enumeration<String> e = this.customConfig.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				Object value = this.customConfig.get(key);
				String stringValue = new String();
				if(value instanceof String) {
					stringValue = "'".concat((String) value).concat("'");
				} else {
					stringValue = value.toString();
				}
				JSarray = JSarray.concat("'").concat(key).concat("' : ").concat(stringValue).concat(", ");
			}
			JSarray = JSarray.concat("}");
			this.out.println("      woopra.config(".concat(JSarray).concat(");"));
			this.customConfig = null;
		}
		
	}
	
	private void printJavaScriptIndentification() {
		if(!this.userUpToDate) {
			out.println("      woopra.identify(".concat(this.user.toJavaScriptArray()).concat(");"));
			this.userUpToDate = true;
		}
		
	}
	
	private void printJavaScriptEvents() {
		if(this.events != null) {
			for(WoopraEvent event : this.events){
				   if(event.name == null) {
					   this.out.println("      woopra.track();");
				   } else {
					   this.out.println("      woopra.track('".concat(event.name).concat("', ").concat(event.toJavaScriptArray()).concat(");"));
				   }
				}
		}
		
	}
	
	private void woopraHttpRequest(boolean isTracking, WoopraEvent event) {
		try {
			String baseUrl = "http://www.woopra.com/track/";
	
			//Config params
			String configParams = "?host=".concat(URLEncoder.encode((String) this.currentConfig.get("domain"), "UTF-8"));
			configParams = configParams.concat("&cookie=").concat(URLEncoder.encode( (String) this.currentConfig.get("cookie_value"), "UTF-8"));
			configParams = configParams.concat("&ip=").concat(URLEncoder.encode( (String) this.currentConfig.get("ip_address"), "UTF-8"));
			configParams = configParams.concat("&timeout=").concat(URLEncoder.encode( (String) this.currentConfig.get("idle_timeout"), "UTF-8"));
	
			//User params
			String userParams = "";
			if ( this.user != null ) {
				for (Enumeration<?> e = this.user.propertyNames(); e.hasMoreElements();) {
					String key = (String) e.nextElement();
					String value = (String) this.user.getProperty(key);
					userParams = userParams.concat("&cv_").concat(URLEncoder.encode(key, "UTF-8")).concat("=").concat(URLEncoder.encode(value, "UTF-8"));
				}
			}

			String url;
			
			//Just identifying
			if ( ! isTracking ) {
				url = baseUrl.concat("identify/").concat(configParams).concat(userParams);
	
			//Tracking
			} else {
	
				//Event params
				String eventParams = "";
				if ( event.name != null ) {
					eventParams = eventParams.concat("&ce_name=").concat(URLEncoder.encode(event.name, "UTF-8"));
					for (Enumeration<?> e = event.propertyNames(); e.hasMoreElements();) {
						String key = (String) e.nextElement();
						String value = (String) event.getProperty(key);
						eventParams = eventParams.concat("&ce_").concat(URLEncoder.encode(key, "UTF-8")).concat("=").concat(URLEncoder.encode(value, "UTF-8"));
					}
	
				} else {
					eventParams = eventParams.concat("&ce_name=pv&ce_url=").concat(request.getRequestURL().toString());
				}
				url = baseUrl.concat("ce/").concat(configParams).concat(userParams).concat(eventParams);
			}
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");
	 
			//add request header
			con.setRequestProperty("User-Agent", request.getHeader("User-Agent"));
			con.getResponseCode();

		} catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public WoopraTracker woopraCode() {
		if (this.out != null) {
			this.out.println("<!-- Woopra code starts here -->");
			this.out.println("   <script>");
			this.printJavaScriptConfiguration();
			this.printJavaScriptIndentification();
			this.printJavaScriptEvents();
			this.out.println("   </script>");
			this.out.println("<!-- Woopra code starts here -->");
		}
		return this;
	}
	
	public void setWoopraCookie() {
		response.addCookie(new Cookie( (String) this.currentConfig.get("cookieName"), (String) this.currentConfig.get("cookieValue")));
	}
	
	private static String randomCookie() {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder( 12 );
		for( int i = 0; i < 12; i++ ) {
			sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		}
		return sb.toString();
	}
	

}
