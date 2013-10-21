package com.woopra.java.sdk;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.woopra.json.JSONException;
import com.woopra.json.JSONObject;
import com.woopra.sss.test.AsyncClient;

/**
 * Woopra Java SDK
 * This class enables back-end and front-end tracking in J2EE for Woopra.
 * Even though the getInstance is implemented for this class, note that the design of
 * this object is such that a new instance should be created for each new request.
 * @author Antoine Chkaiban
 * @version 2013-09-30
 */
public class WoopraTracker {
	
	//TODO: pass param SDK_ID to front-end tracking too
	private static String SDK_ID = "j2ee";
	protected static JSONObject defaultConfig;
	protected JSONObject currentConfig;
	protected JSONObject customConfig;
	private HttpServletRequest request;
	private HttpServletResponse response;
	protected boolean hasPushed;
	protected LinkedList<WoopraEvent> events;
	protected JSONObject user;
	protected boolean userUpToDate;
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
	 * Public constructor
	 * @param request (HttpServletRequest) the current request
	 * @param response (HttpServletResponse) the current response
	 */
	public WoopraTracker(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		this.currentConfig = WoopraTracker.defaultConfig;
		this.currentConfig.put(WoopraTracker.DOMAIN, this.getDomain());
		this.currentConfig.put(WoopraTracker.COOKIE_DOMAIN, this.getDomain());
		this.currentConfig.put(WoopraTracker.COOKIE_VALUE, this.getCookie() != null ? this.getCookie() : WoopraTracker.randomCookie());
		this.currentConfig.put(WoopraTracker.IP_ADDRESS, this.getIpAddress());
		this.currentConfig.put(WoopraTracker.USER_AGENT, this.getUserAgent());
		this.currentConfig.put(WoopraTracker.CURRENT_URL, this.getCurrentUrl());
		this.hasPushed = false;
		this.userUpToDate = true;
		this.user = new JSONObject();
		this.customConfig = new JSONObject();
		this.customConfig.put("app", WoopraTracker.SDK_ID);
	}
	
	/**
	 * Configures the WoopraTracker Object.
	 * @param key - configuration key. All keys are accessible statically (ex: WoopraTracker.IDLE_TIMEOUT)
	 * @param value	(String, int, boolean) - configuration value
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
					if (key.equals(WoopraTracker.COOKIE_NAME)) {
						this.actualizeCookie();
					}
				}
			}
		}
		return this;
	}
	
	/**
	 * Configures the WoopraTracker Object.
	 * @param data - 2D Array containing Arrays of size 2 where:<br>
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
	 * @param key - identification key
	 * @param value	(String, int, boolean) - identification value
	 * @return WoopraTracker with identified user
	 */
	public final WoopraTracker identify(String key, Object value) {
		this.user.put(key, value);
		this.userUpToDate = false;
		return this;
	}

	/**
	 * Identifies the user to the tracker, so that when tracking (or pushing), the identified user is passed along to Woopra.
	 * @param data - 2D Array containing Arrays of size 2 where:<br>
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
	 * Tracks a page view through the front-end.
	 */
	public WoopraTracker track() {
		if(this.events == null) {
			this.events = new LinkedList<WoopraEvent>();
		}
		this.events.add(null);
		return this;
	}
	
	/**
	 * Tracks a page view.
	 * @param backEndProcessing - Should the page view event be tracked through the back-end?
	 * @return WoopraTracker
	 */
	public WoopraTracker track(boolean backEndProcessing) {
		if(backEndProcessing) {
			String userAgent = this.currentConfig.getString(WoopraTracker.USER_AGENT);
			this.woopraHttpRequest(true, null, userAgent == "" ? null : new String[] {"User-Agent: ".concat(userAgent)});
			return this;
		} else {
			return this.track();
		}
	}
	
	/**
	 * Tracks a custom event through the front-end.
	 * @param event	- the event to track
	 * @return WoopraTracker
	 */
	public WoopraTracker track(WoopraEvent event) {
		if(this.events == null) {
			this.events = new LinkedList<WoopraEvent>();
		}
		this.events.add(event);
		return this;
	}
	
	/**
	 * Tracks a custom event.
	 * @param event	(WoopraEvent) the event to track
	 * @param backEndProcessing (boolean) Should the event be tracked through the back-end?
	 * @return WoopraTracker
	 */
	public WoopraTracker track(WoopraEvent event, boolean backEndProcessing) {
		if(backEndProcessing) {
			String userAgent = this.currentConfig.getString(WoopraTracker.USER_AGENT);
			this.woopraHttpRequest(true, event, userAgent == "" ? null : new String[] {"User-Agent: ".concat(userAgent)});
			return this;
		} else {
			return this.track(event);
		}
	}
	
	/**
	 * Tracks a custom event through the front-end.
	 * @param name - the name of the custom event
	 * @param properties - 2D Array containing Arrays of size 2 where:<br>
	 * 				key (String) - property name<br>
	 * 				value (String, int, boolean) - property value<br>
	 * @return WoopraTracker
	 */
	public WoopraTracker track(String name, Object[][] properties) {
		return this.track(new WoopraEvent(name, properties));
	}
	
	/**
	 * Tracks a custom event.
	 * @param name - the name of the custom event
	 * @param properties - 2D Array containing Arrays of size 2 where:<br>
	 * 				key (String) - property name<br>
	 * 				value (String, int, boolean) - property value<br>
	 * @param backEndProcessing - Should the event be tracked through the back-end?
	 * @return WoopraTracker
	 */
	public WoopraTracker track(String name, Object[][] properties, boolean backEndProcessing) {
		return this.track(new WoopraEvent(name, properties), backEndProcessing);
	}
	
	/**
	 * Pushes the identified user to Woopra through the front-end.
	 * Please note that the identified user is automatically pushed with any tracking event.
	 * Therefore, this method is useful if you need to identify a user without tracking any event.
	 */
	public void push() {
		if(!this.userUpToDate) {
			this.hasPushed = true;
		}
	}
	
	/**
	 * Pushes the identified user to Woopra.
	 * Please note that the identified user is automatically pushed with any tracking event.
	 * Therefore, this method is useful if you need to identify a user without tracking any event.
	 * @param backEndProcessing - Should the identification be pushed through the back-end?
	 */
	public void push(boolean backEndProcessing) {
		if(backEndProcessing) {
			if (!this.userUpToDate) {
				String userAgent = this.currentConfig.getString(WoopraTracker.USER_AGENT);
				this.woopraHttpRequest(false, null, userAgent == "" ? null : new String[] {"User-Agent: ".concat(userAgent)});
			}
		} else {
			this.push();
		}
	}
	
	/**
	 * Returns the JavaScript code for configuring, identifying, tracking, and pushing.
	 * Call this function after all front-end actions, and place its return value in your page's header.
	 * @return the JavaScript code for all front-end tracking.
	 */
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
	
	/**
	 * Sets the current cookie configuration in the user's browser (COOKIE_NAME, COOKIE_DOMAIN, COOKIE_PATH, COOKIE_VALUE)
	 * If a cookie was already in the browser, this method will override it with a new one that expires in 2 years.
	 * If there was no cookie in the browser, a random one will be generated and sent to Woopra.
	 * Therefore, in order for the user to be recognized in future visits, the cookie should be set in the user's browser.
	 */
	public void setWoopraCookie() {
		Cookie cookie = new Cookie( (String) this.currentConfig.get(WoopraTracker.COOKIE_NAME), (String) this.currentConfig.get(WoopraTracker.COOKIE_VALUE));
		cookie.setMaxAge(60*60*24*365*2);
		this.response.addCookie(cookie);
	}
	
	private void woopraHttpRequest(boolean isTracking, WoopraEvent event, String[] headers) {
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
				url = baseUrl.concat("identify/").concat(configParams).concat(userParams).concat("&app=").concat(WoopraTracker.SDK_ID);
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
				url = baseUrl.concat("ce/").concat(configParams).concat(userParams).concat(eventParams).concat("&app=").concat(WoopraTracker.SDK_ID);
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
			   if(event == null) {
				   jsEvents = jsEvents.concat("   woopra.track();\n");
			   } else {
				   jsEvents = jsEvents.concat("   woopra.track('".concat(event.name).concat("', ").concat(event.toString()).concat(");\n"));
			   }
			}
			this.events = null;
		}
		return jsEvents;
	}
	
	private String getIpAddress() {
		if(this.request.getHeader("HTTP_X_FORWARDED_FOR") != null) {
			return this.request.getHeader("HTTP_X_FORWARDED_FOR").split(",")[0].trim();
		} else {
			return this.request.getRemoteAddr();
		}
	}
	
	private String getCookie() {
		Cookie[] cookies = this.request.getCookies();
		for(int i = 0; cookies != null && i < cookies.length; i++) {
			if(cookies[i].getName().equals(this.currentConfig.get(WoopraTracker.COOKIE_NAME))) {
				return cookies[i].getValue();
			}
		}
		return null;
	}
	
	private String getUserAgent() {
		return this.request.getHeader("User-Agent");
	}
	
	private String getCurrentUrl() {
		return request.getRequestURL().toString();
	}
	
	private String getDomain() {
		return request.getServerName();
	}
	
	private void actualizeCookie() {
		String cookie = this.getCookie();
		if (cookie != null) {
			this.currentConfig.put(WoopraTracker.COOKIE_VALUE, cookie);
		}
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