package com.woopra.java.sdk;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import com.woopra.json.JSONException;
import com.woopra.json.JSONObject;
import com.woopra.sss.test.AsyncClient;

/**
 * Woopra Java SDK
 * This class represents the Java Equivalent of the JavaScript Woopra Object
 * @author Antoine Chkaiban
 * @version 2013-09-30
 */
public class WoopraTracker {
	
	private boolean hasPushed;
	private LinkedList<WoopraEvent> events;
	private JSONObject user;
	private boolean userUpToDate;
	private static JSONObject defaultConfig;
	protected JSONObject currentConfig;
	private JSONObject customConfig;
	public static final String DOMAIN = "domain",
            COOKIE_NAME = "cookie_name",
            COOKIE_DOMAIN = "cookie_domain",
            COOKIE_PATH = "cookie_path",
            PING = "ping",
            PING_INTERVAL = "ping_interval",
            IDLE_TIMEOUT = "idle_timeout",
            DOWNLOAD_TRACKING = "download_tracking",
            OUTGOING_TRACKING = "outgoing_tracking",
            DOWNLOAD_PAUSE = "download_pause",
            OUTGOING_PAUSE = "outgoing_pause",
            IGNORE_QUERY_URL = "ignore_query_url",
            HIDE_CAMPAIGN = "hide_campaign",
            IP_ADDRESS = "ip_address",
            COOKIE_VALUE = "cookie_value",
            USER_AGENT = "user_agent",
            CURRENT_URL = "current_url";
	
	static {
		WoopraTracker.defaultConfig = new JSONObject();
		WoopraTracker.defaultConfig.put(WoopraTracker.DOMAIN, "");
	    WoopraTracker.defaultConfig.put(WoopraTracker.COOKIE_NAME, "wooTracker");
	    WoopraTracker.defaultConfig.put(WoopraTracker.COOKIE_DOMAIN, "");
	    WoopraTracker.defaultConfig.put(WoopraTracker.COOKIE_PATH, "/");
	    WoopraTracker.defaultConfig.put(WoopraTracker.PING, true);
	    WoopraTracker.defaultConfig.put(WoopraTracker.PING_INTERVAL, 12000);
	    WoopraTracker.defaultConfig.put(WoopraTracker.IDLE_TIMEOUT, 300000);
	    WoopraTracker.defaultConfig.put(WoopraTracker.DOWNLOAD_TRACKING, true);
	    WoopraTracker.defaultConfig.put(WoopraTracker.OUTGOING_TRACKING, true);
	    WoopraTracker.defaultConfig.put(WoopraTracker.DOWNLOAD_PAUSE, 200);
	    WoopraTracker.defaultConfig.put(WoopraTracker.OUTGOING_PAUSE, 400);
	    WoopraTracker.defaultConfig.put(WoopraTracker.IGNORE_QUERY_URL, true);
	    WoopraTracker.defaultConfig.put(WoopraTracker.HIDE_CAMPAIGN, false);
	    WoopraTracker.defaultConfig.put(WoopraTracker.IP_ADDRESS, "");
	    WoopraTracker.defaultConfig.put(WoopraTracker.COOKIE_VALUE, "");
	    WoopraTracker.defaultConfig.put(WoopraTracker.USER_AGENT, "");
	    WoopraTracker.defaultConfig.put(WoopraTracker.CURRENT_URL, "");
    }

	public WoopraTracker() {
		this.currentConfig = WoopraTracker.defaultConfig;
		this.userUpToDate = true;
		this.user = new JSONObject();
		this.hasPushed = false;
	}
	
	public WoopraTracker config(String key, Object value) {
		if(WoopraTracker.defaultConfig.has(key)) {
			if (WoopraTracker.defaultConfig.get(key).getClass() == value.getClass()) {
				this.currentConfig.put(key, value);
				if(! key.equals(WoopraTracker.IP_ADDRESS) && ! key.equals(WoopraTracker.COOKIE_VALUE) && ! key.equals(WoopraTracker.USER_AGENT) && ! key.equals(WoopraTracker.CURRENT_URL)) {
					if(this.customConfig == null) {
						this.customConfig = new JSONObject();
					}
					this.customConfig.put(key, value);
					if (key.equals(WoopraTrackerEE.COOKIE_NAME)) {
						this.actualizeCookie();
					}
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
		this.user.put(key, value);
		this.userUpToDate = false;
		return this;
	}
	
	public WoopraTracker identify(String[][] data) {
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
			try {
				this.woopraHttpRequest(true, new WoopraEvent(), new String[] {"User-Agent: ".concat(this.currentConfig.getString(WoopraTracker.USER_AGENT))});
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			try {
				this.woopraHttpRequest(true, event, new String[] {"User-Agent: ".concat(this.currentConfig.getString(WoopraTracker.USER_AGENT))});
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	
	public void push(boolean backEndProcessing) throws JSONException, IOException {
		if(backEndProcessing) {
			this.woopraHttpRequest(false, new WoopraEvent(), new String[] {"User-Agent: ".concat(this.currentConfig.getString(WoopraTracker.USER_AGENT))});
		} else {
			this.push();
		}
	}
	
	private String printJavaScriptConfiguration() {
		String jsConfig = "";
		if(this.customConfig != null) {
			jsConfig = jsConfig.concat("   woopra.config(".concat(this.customConfig.toString()).concat(");\n"));
			this.customConfig = null;
		}
		return jsConfig;
	}
	
	private String printJavaScriptIndentification() {
		String jsUser = "";
		if(!this.userUpToDate) {
			jsUser = jsUser.concat("   woopra.identify(".concat(this.user.toString()).concat(");\n"));
			this.userUpToDate = true;
		}
		return jsUser;
	}
	
	private String printJavaScriptEvents() {
		String jsEvents = "";
		if(this.events != null) {
			for(WoopraEvent event : this.events){
			   if(event.name == null) {
				   jsEvents = jsEvents.concat("   woopra.track();\n");
			   } else {
				   jsEvents = jsEvents.concat("   woopra.track('".concat(event.name).concat("', ").concat(event.toString()).concat(");\n"));
			   }
			}
			this.events = null;
		}
		return jsEvents;
	}
	
	private void woopraHttpRequest(boolean isTracking, WoopraEvent event, String[] headers) throws JSONException, MalformedURLException, IOException {
		String baseUrl = "http://www.woopra.com/track/";

		//Config params
		String configParams = "?host=".concat(URLEncoder.encode((String) this.currentConfig.get(WoopraTracker.DOMAIN), "UTF-8"));
		configParams = configParams.concat("&cookie=").concat(URLEncoder.encode( (String) this.currentConfig.get(WoopraTracker.COOKIE_VALUE), "UTF-8"));
		configParams = configParams.concat("&ip=").concat(URLEncoder.encode( (String) this.currentConfig.get(WoopraTracker.IP_ADDRESS), "UTF-8"));
		configParams = configParams.concat("&timeout=").concat(URLEncoder.encode(this.currentConfig.get(WoopraTracker.IDLE_TIMEOUT).toString(), "UTF-8"));

		//User params
		String userParams = "";
		@SuppressWarnings("unchecked")
		Iterator<String> userKeys = this.user.keys();
		while (userKeys.hasNext()) {
			String key = userKeys.next();
			String value = this.user.get(key).toString();
			userParams = userParams.concat("&cv_").concat(URLEncoder.encode(key, "UTF-8")).concat("=").concat(URLEncoder.encode(value, "UTF-8"));
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
				Iterator<String> eventKeys = event.properties.keys();
				while (eventKeys.hasNext()) {
					String key = eventKeys.next();
					String value = event.properties.get(key).toString();
					eventParams = eventParams.concat("&ce_").concat(URLEncoder.encode(key, "UTF-8")).concat("=").concat(URLEncoder.encode(value, "UTF-8"));
				}

			} else {
				eventParams = eventParams.concat("&ce_name=pv&ce_url=").concat(this.currentConfig.getString(WoopraTracker.CURRENT_URL));
			}
			url = baseUrl.concat("ce/").concat(configParams).concat(userParams).concat(eventParams);
		}
		
		AsyncClient.getInstance().send(new URL(url), headers);
	}
	
	public String jsCode() {
		String jsCode = "\n";
		jsCode = jsCode.concat("<!-- Woopra code starts here -->\n");
		jsCode = jsCode.concat("<script>\n");
		jsCode = jsCode.concat("   (function(){\n   var t,i,e,n=window,o=document,a=arguments,s=\"script\",r=[\"config\",\"track\",\"identify\",\"visit\",\"push\",\"call\"],c=function(){var t,i=this;for(i._e=[],t=0;r.length>t;t++)(function(t){i[t]=function(){return i._e.push([t].concat(Array.prototype.slice.call(arguments,0))),i}})(r[t])};for(n._w=n._w||{},t=0;a.length>t;t++)n._w[a[t]]=n[a[t]]=n[a[t]]||new c;i=o.createElement(s),i.async=1,i.src=\"//static.woopra.com/js/w.js\",e=o.getElementsByTagName(s)[0],e.parentNode.insertBefore(i,e)\n   })(\"woopra\");\n");
		jsCode = jsCode.concat(this.printJavaScriptConfiguration());
		jsCode = jsCode.concat(this.printJavaScriptIndentification());
		jsCode = jsCode.concat(this.printJavaScriptEvents());
		if(this.hasPushed) {
			jsCode = jsCode.concat("   woopra.push();\n");
		}
		jsCode = jsCode.concat("</script>\n");
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
	
	protected void actualizeCookie() {
		
	}
}
