package com.woopra.java.sdk;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Random;

import com.woopra.json.JSONException;
import com.woopra.json.JSONObject;
import com.woopra.sss.test.AsyncClient;

/**
 * Woopra Java SDK
 * This class enables back-end tracking in Java for Woopra.
 * Even though the getInstance is implemented for this class, note that the design of
 * this object is such that a new instance should be created for each new request.
 * @author Antoine Chkaiban
 * @version 2013-09-30
 */
public class WoopraTracker {
	
	protected JSONObject user;
	protected boolean userUpToDate;
	protected JSONObject customConfig;
	protected static JSONObject defaultConfig;
	protected JSONObject currentConfig;
	private static WoopraTracker instance = null;
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
	
	/**
	 * If an instance of WoopraTracker already exists, this method returns it.
	 * It creates a new instance otherwise.
	 * @return WoopraTracker
	 */
	public synchronized final static WoopraTracker getInstance() {
		if (WoopraTracker.instance == null) {
    		WoopraTracker.instance = new WoopraTracker();
    	}
    	return WoopraTracker.instance;
	}

	/**
	 * Public Constructor
	 */
	public WoopraTracker() {
		this.currentConfig = WoopraTracker.defaultConfig;
		this.user = new JSONObject();
		this.userUpToDate = true;
	}
	
	/**
	 * Configures the WoopraTracker Object.
	 * @param key	configuration key. All keys are accessible statically (ex: WoopraTracker.IDLE_TIMEOUT)
	 * @param value	configuration value
	 * @return Configured WoopraTracker
	 */
	public final WoopraTracker config(String key, Object value) {
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
	
	/**
	 * Configures the WoopraTracker Object.
	 * @param data	2D Array containing Arrays of size 2 where:<br>
	 * 				key (String) - configuration key. All keys are accessible statically (ex: WoopraTracker.IDLE_TIMEOUT)<br>
	 * 				value (String, int, boolean) - configuration value<br>
	 * @return Configured WoopraTracker
	 */
	public final WoopraTracker config(Object[][] data) {
		for(Object[] keyValue : data) {
			String key = (String) keyValue[0];
			Object value = keyValue[1];
			this.config(key, value);
		}
		return this;
	}
	
	/**
	 * Identifies the user to the tracker, so that when tracking (or pushing), the identified user is passed along to Woopra.
	 * @param key	identification key
	 * @param value	identification value
	 * @return WoopraTracker with identified user
	 */
	public final WoopraTracker identify(String key, String value) {
		this.user.put(key, value);
		this.userUpToDate = false;
		return this;
	}

	/**
	 * Identifies the user to the tracker, so that when tracking (or pushing), the identified user is passed along to Woopra.
	 * @param data	(Object[][]) 2D Array containing Arrays of size 2 where:<br>
	 * 				key (String) - identification key<br>
	 * 				value (String, int, boolean) - identification value<br>
	 * @return WoopraTracker with identified user
	 */
	public final WoopraTracker identify(Object[][] data) {
		for(Object[] keyValue : data) {
			String key = (String) keyValue[0];
			Object value = keyValue[1];
			this.user.put(key, value);
		}
		this.userUpToDate = false;
		return this;
	}
	
	/**
	 * Tracks a page view through the back-end.<br>
	 * To be able to track, at least the domain name (WoopraTracker.DOMAIN) will have to be configured using the public config method.
	 * Configure the following keys (optional) for the tracking to be accurate:<br>
	 * - WoopraTracker.IP_ADDRESS		the IP address of the user currently being tracked
	 * - WoopraTracker.CURRENT_URL		the URL of the page being currently viewed
	 * - WoopraTracker.COOKIE_VALUE		the value of the cookie used to identify the user
	 * - WoopraTracker.USER_AGENT		the User-Agent of the user currently being tracked
	 * @return WoopraTracker 
	 */
	public WoopraTracker track() {
		return this.track(null);
	}
	
	/**
	 * Tracks a custom event through the back-end.
	 * @param event	(WoopraEvent) the event to track
	 * @return WoopraTracker 
	 */
	public WoopraTracker track(WoopraEvent event) {
		String userAgent = this.currentConfig.getString(WoopraTracker.USER_AGENT);
		this.woopraHttpRequest(true, event, userAgent == "" ? null : new String[] {"User-Agent: ".concat(userAgent)});
		return this;
	}
	
	/**
	 * Tracks a custom event through the back-end.
	 * @param name (String) the name of the custom event
	 * @param properties 	(Object[][]) 2D Array containing Arrays of size 2 where:<br>
	 * 				key (String) - property name<br>
	 * 				value (String, int, boolean) - property value<br>
	 * @return
	 */
	public WoopraTracker track(String name, Object[][] properties) {
		return this.track(new WoopraEvent(name, properties));
	}
	
	/**
	 * Pushes the identified user to Woopra through the back-end.
	 * Please note that the identified user is automatically pushed with any tracking event.
	 * Therefore, this method is useful if you need to identify a user without tracking any event.
	 */
	public void push() {
		if (! this.userUpToDate) {
			String userAgent = this.currentConfig.getString(WoopraTracker.USER_AGENT);
			this.woopraHttpRequest(false, null, userAgent == "" ? null : new String[] {"User-Agent: ".concat(userAgent)});
		}
	}
	
	protected final void woopraHttpRequest(boolean isTracking, WoopraEvent event, String[] headers) {
		if (this.currentConfig.get(WoopraTracker.DOMAIN).equals("")) {
			return;
		}
		try {
			String baseUrl = "http://www.woopra.com/track/";
			String configParams = "?host=".concat(URLEncoder.encode((String) this.currentConfig.get(WoopraTracker.DOMAIN), "UTF-8"));
			if (! this.currentConfig.getString(WoopraTracker.COOKIE_VALUE).equals("")) {
				configParams = configParams.concat("&cookie=").concat(URLEncoder.encode( (String) this.currentConfig.get(WoopraTracker.COOKIE_VALUE), "UTF-8"));
			}
			if (! this.currentConfig.getString(WoopraTracker.IP_ADDRESS).equals("")) {
				configParams = configParams.concat("&ip=").concat(URLEncoder.encode( (String) this.currentConfig.get(WoopraTracker.IP_ADDRESS), "UTF-8"));
			}
			configParams = configParams.concat("&timeout=").concat(URLEncoder.encode(this.currentConfig.get(WoopraTracker.IDLE_TIMEOUT).toString(), "UTF-8"));
			String userParams = "";
			@SuppressWarnings("unchecked")
			Iterator<String> userKeys = this.user.keys();
			while (userKeys.hasNext()) {
				String key = userKeys.next();
				String value = this.user.get(key).toString();
				userParams = userParams.concat("&cv_").concat(URLEncoder.encode(key, "UTF-8")).concat("=").concat(URLEncoder.encode(value, "UTF-8"));
			}
			String url;
			if ( ! isTracking ) {
				url = baseUrl.concat("identify/").concat(configParams).concat(userParams);
			} else {
				String eventParams = "";
				if ( event != null ) {
					eventParams = eventParams.concat("&ce_name=").concat(URLEncoder.encode(event.name, "UTF-8"));
					@SuppressWarnings("unchecked")
					Iterator<String> eventKeys = event.properties.keys();
					while (eventKeys.hasNext()) {
						String key = eventKeys.next();
						String value = event.properties.get(key).toString();
						eventParams = eventParams.concat("&ce_").concat(URLEncoder.encode(key, "UTF-8")).concat("=").concat(URLEncoder.encode(value, "UTF-8"));
					}
				} else {
					eventParams = eventParams.concat("&ce_name=pv");
					if (! this.currentConfig.getString(WoopraTracker.CURRENT_URL).equals("")) {
						eventParams = eventParams.concat("&ce_url=").concat(this.currentConfig.getString(WoopraTracker.CURRENT_URL));
					}
				}
				url = baseUrl.concat("ce/").concat(configParams).concat(userParams).concat(eventParams);
			}
			AsyncClient.getInstance().send(new URL(url), headers);
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
	}
	
	protected final static String randomCookie() {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder( 12 );
		for( int i = 0; i < 12; i++ ) {
			sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		}
		return sb.toString();
	}
	
	protected void actualizeCookie() {
		return;
	}
}
