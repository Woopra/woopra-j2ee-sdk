package com.woopra.java.sdk;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.woopra.json.JSONObject;

/**
 * Woopra Java SDK
 * This class represents the Java Equivalent of the JavaScript Woopra Object
 * @author Antoine Chkaiban
 * @version 2013-09-30
 */
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
	private static JSONObject defaultConfig = new JSONObject();
	private JSONObject currentConfig;
	private JSONObject customConfig;
	
	//Configuration KEYS:
	public static final String DOMAIN = "domain";
	public static final String COOKIE_NAME = "cookieName";
	public static final String COOKIE_DOMAIN = "cookieDomain";
	public static final String COOKIE_PATH = "cookiePath";
	public static final String PING = "ping";
	public static final String PING_INTERVAL = "pingInterval";
	public static final String IDLE_TIMEOUT = "idleTimeout";
	public static final String DOWNLOAD_TRACKING = "downloadTracking";
	public static final String OUTGOING_TRACKING = "outgoingTracking";
	public static final String DOWNLOAD_PAUSE = "downloadPause";
	public static final String OUTGOING_PAUSE = "outgoingPause";
	public static final String IGNORE_QUERY_URL = "ignoreQueryUrl";
	public static final String HIDE_CAMPAIGN = "hideCampaign";
	public static final String IP_ADDRESS = "ipAddress";
	public static final String COOKIE_VALUE = "cookieValue";

	
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
			if(cookies[i].getName().equals(this.currentConfig.get("cookieName"))) {
				this.currentConfig.put("cookieValue", cookies[i].getValue());
			}
		}
		if (this.currentConfig.get("cookieValue") == "") {
			this.currentConfig.put("cookieValue", WoopraTracker.randomCookie());
		}
		
		this.userUpToDate = true;
	}
	
	
	public WoopraTracker config(String key, Object value) {
		if(WoopraTracker.defaultConfig.has(key)) {
			if (WoopraTracker.defaultConfig.get(key).getClass() == value.getClass()) {
				this.currentConfig.put(key, value);
				if(this.customConfig == null) {
					this.customConfig = new JSONObject();
				}
				this.customConfig.put(key, value);
			}
		}
		
		return this;
	}
	
	
	public WoopraTracker identify(WoopraUser user) {
		this.user = user;
		this.userUpToDate = false;
		return this;
	}
	
	public WoopraTracker track() {
		return this.track(new WoopraEvent());
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
			this.out.println("   <script>");
			this.printJavaScriptConfiguration();
			this.printJavaScriptIndentification();
			this.printJavaScriptEvents();
			this.out.println("   </script>");
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
			this.out.println("      woopra.config(".concat(this.customConfig.toString()).concat(");"));
			this.customConfig = null;
		}
		
	}
	
	private void printJavaScriptIndentification() {
		if(!this.userUpToDate) {
			out.println("      woopra.identify(".concat(this.user.toString()).concat(");"));
			this.userUpToDate = true;
		}
		
	}
	
	private void printJavaScriptEvents() {
		if(this.events != null) {
			for(WoopraEvent event : this.events){
			   if(event.name == null) {
				   this.out.println("      woopra.track();");
			   } else {
				   this.out.println("      woopra.track('".concat(event.name).concat("', ").concat(event.toString()).concat(");"));
			   }
			}
			this.events = null;
		}
	}
	
	private void woopraHttpRequest(boolean isTracking, WoopraEvent event) {
		try {
			String baseUrl = "http://www.woopra.com/track/";
	
			//Config params
			String configParams = "?host=".concat(URLEncoder.encode((String) this.currentConfig.get("domain"), "UTF-8"));
			configParams = configParams.concat("&cookie=").concat(URLEncoder.encode( (String) this.currentConfig.get("cookieValue"), "UTF-8"));
			configParams = configParams.concat("&ip=").concat(URLEncoder.encode( (String) this.currentConfig.get("ipAddress"), "UTF-8"));
			configParams = configParams.concat("&timeout=").concat(URLEncoder.encode(this.currentConfig.get("idleTimeout").toString(), "UTF-8"));
	
			//User params
			String userParams = "";
			if ( this.user != null ) {
				@SuppressWarnings("unchecked")
				Iterator<String> keys = this.user.properties.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					String value = this.user.properties.get(key).toString();
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
					@SuppressWarnings("unchecked")
					Iterator<String> keys = event.properties.keys();
					while (keys.hasNext()) {
						String key = keys.next();
						String value = event.properties.get(key).toString();
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
			this.out.println("      (function(){\n      var t,i,e,n=window,o=document,a=arguments,s=\"script\",r=[\"config\",\"track\",\"identify\",\"visit\",\"push\",\"call\"],c=function(){var t,i=this;for(i._e=[],t=0;r.length>t;t++)(function(t){i[t]=function(){return i._e.push([t].concat(Array.prototype.slice.call(arguments,0))),i}})(r[t])};for(n._w=n._w||{},t=0;a.length>t;t++)n._w[a[t]]=n[a[t]]=n[a[t]]||new c;i=o.createElement(s),i.async=1,i.src=\"//static.woopra.com/js/w.js\",e=o.getElementsByTagName(s)[0],e.parentNode.insertBefore(i,e)\n      })(\"woopra\");");
			this.printJavaScriptConfiguration();
			this.printJavaScriptIndentification();
			this.printJavaScriptEvents();
			this.out.println("   </script>");
			this.out.println("<!-- Woopra code ends here -->");
		}
		this.trackerReady = true;
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
