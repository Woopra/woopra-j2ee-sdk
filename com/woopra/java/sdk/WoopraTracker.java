package com.woopra.java.sdk;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import com.woopra.json.JSONObject;

/**
 * Woopra Java SDK
 * This class represents the Java Equivalent of the JavaScript Woopra Object
 * @author Antoine Chkaiban
 * @version 2013-09-30
 */
public class WoopraTracker {
	
	//Has the user info been pushed?
	private boolean hasPushed;
	
	//Event stack
	private LinkedList<WoopraEvent> events;
	
	//User variables
	protected JSONObject user;
	private boolean userUpToDate;
	
	//Configuration Arrays
	protected static JSONObject defaultConfig = new JSONObject();
	public JSONObject currentConfig;
	public JSONObject customConfig;
	
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
	public WoopraTracker() {
		
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
		
		//User is up to date
		this.userUpToDate = true;
		
		//The user info hasn't been pushed yet
		this.hasPushed = false;
	}
	
	
	public WoopraTracker config(String key, Object value) {
		if(WoopraTracker.defaultConfig.has(key)) {
			if (WoopraTracker.defaultConfig.get(key).getClass() == value.getClass()) {
				this.currentConfig.put(key, value);
				if(! key.equals(WoopraTracker.IP_ADDRESS) && ! key.equals(WoopraTracker.COOKIE_VALUE)) {
					if(this.customConfig == null) {
						this.customConfig = new JSONObject();
					}
					this.customConfig.put(key, value);
				}
			}
		}
		
		return this;
	}
	
	public WoopraTracker config(Object[][] data) {
		for(Object[] keyValue : data) {
			String key = (String) keyValue[0];
			Object value = keyValue[1];
			this.config(key, value);
		}
		return this;
	}
	
	
	public WoopraTracker identify(String key, String value) {
		if (this.user == null) {
			this.user = new JSONObject();
		}
		this.user.put(key, value);
		this.userUpToDate = false;
		return this;
	}
	
	public WoopraTracker identify(String[][] data) {
		if (this.user == null) {
			this.user = new JSONObject();
		}
		for(String[] keyValue : data) {
			this.user.put(keyValue[0], keyValue[1]);
		}
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
	
	public WoopraTracker track(String name, Object[][] properties) {
		return this.track(new WoopraEvent(name, properties));
	}
	
	public WoopraTracker track(String name, Object[][] properties, boolean backEndProcessing) {
		if(backEndProcessing) {
			return this.track(new WoopraEvent(name, properties), true);
		} else {
			return this.track(new WoopraEvent(name, properties));
		}
	}
	
	
	public void push() {
		if(!this.userUpToDate) {
			this.hasPushed = true;
		}
	}
	
	public void push(boolean backEndProcessing) {
		if(backEndProcessing) {
			
			this.woopraHttpRequest(false, new WoopraEvent());
			
		} else {
			this.push();
		}
	}
	
	private String printJavaScriptConfiguration() {
		String jsConfig = "";
		if(this.customConfig != null) {
			jsConfig = jsConfig.concat("      woopra.config(".concat(this.customConfig.toString()).concat(");\n"));
			this.customConfig = null;
		}
		return jsConfig;
	}
	
	private String printJavaScriptIndentification() {
		String jsUser = "";
		if(!this.userUpToDate) {
			jsUser = jsUser.concat("      woopra.identify(".concat(this.user.toString()).concat(");\n"));
			this.userUpToDate = true;
		}
		return jsUser;
	}
	
	private String printJavaScriptEvents() {
		String jsEvents = "";
		if(this.events != null) {
			for(WoopraEvent event : this.events){
			   if(event.name == null) {
				   jsEvents = jsEvents.concat("      woopra.track();\n");
			   } else {
				   jsEvents = jsEvents.concat("      woopra.track('".concat(event.name).concat("', ").concat(event.toString()).concat(");\n"));
			   }
			}
			this.events = null;
		}
		return jsEvents;
	}
	
	protected void woopraHttpRequest(boolean isTracking, WoopraEvent event) {
		try {
			String baseUrl = "http://www.woopra.com/track/";
	
			//Config params
			String configParams = "?host=".concat(URLEncoder.encode((String) this.currentConfig.get(WoopraTracker.DOMAIN), "UTF-8"));
			configParams = configParams.concat("&cookie=").concat(URLEncoder.encode( (String) this.currentConfig.get(WoopraTracker.COOKIE_VALUE), "UTF-8"));
			configParams = configParams.concat("&ip=").concat(URLEncoder.encode( (String) this.currentConfig.get(WoopraTracker.IP_ADDRESS), "UTF-8"));
			configParams = configParams.concat("&timeout=").concat(URLEncoder.encode(this.currentConfig.get(WoopraTracker.IDLE_TIMEOUT).toString(), "UTF-8"));
	
			//User params
			String userParams = "";
			if ( this.user != null ) {
				@SuppressWarnings("unchecked")
				Iterator<String> keys = this.user.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					String value = this.user.get(key).toString();
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
					eventParams = eventParams.concat("&ce_name=pv");
				}
				url = baseUrl.concat("ce/").concat(configParams).concat(userParams).concat(eventParams);
			}
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");
			con.getResponseCode();

		} catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String woopraCode() {
		String jsCode = "\n";
		jsCode = jsCode.concat("<!-- Woopra code starts here -->\n");
		jsCode = jsCode.concat("   <script>\n");
		jsCode = jsCode.concat("      (function(){\n      var t,i,e,n=window,o=document,a=arguments,s=\"script\",r=[\"config\",\"track\",\"identify\",\"visit\",\"push\",\"call\"],c=function(){var t,i=this;for(i._e=[],t=0;r.length>t;t++)(function(t){i[t]=function(){return i._e.push([t].concat(Array.prototype.slice.call(arguments,0))),i}})(r[t])};for(n._w=n._w||{},t=0;a.length>t;t++)n._w[a[t]]=n[a[t]]=n[a[t]]||new c;i=o.createElement(s),i.async=1,i.src=\"//static.woopra.com/js/w.js\",e=o.getElementsByTagName(s)[0],e.parentNode.insertBefore(i,e)\n      })(\"woopra\");\n");
		jsCode = jsCode.concat(this.printJavaScriptConfiguration());
		jsCode = jsCode.concat(this.printJavaScriptIndentification());
		jsCode = jsCode.concat(this.printJavaScriptEvents());
		if(this.hasPushed) {
			jsCode = jsCode.concat("      woopra.push();\n");
		}
		jsCode = jsCode.concat("   </script>\n");
		jsCode = jsCode.concat("<!-- Woopra code ends here -->\n");
		return jsCode;
	}
	
	protected static String randomCookie() {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder( 12 );
		for( int i = 0; i < 12; i++ ) {
			sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		}
		return sb.toString();
	}
	

}
