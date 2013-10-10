package com.woopra.java.sdk;

import java.util.LinkedList;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Woopra Java SDK
 * This class enables back-end and front-end tracking in J2EE for Woopra.
 * Even though the getInstance is implemented for this class, note that the design of
 * this object is such that a new instance should be created for each new request.
 * @author Antoine Chkaiban
 * @version 2013-09-30
 */
public class WoopraTrackerEE extends WoopraTracker {
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	protected boolean hasPushed;
	protected LinkedList<WoopraEvent> events;
	
	/**
	 * Public constructor
	 * @param request (HttpServletRequest) the current request
	 * @param response (HttpServletResponse) the current response
	 */
	public WoopraTrackerEE(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		this.currentConfig.put(WoopraTrackerEE.DOMAIN, this.getDomain());
		this.currentConfig.put(WoopraTrackerEE.COOKIE_DOMAIN, this.getDomain());
		String cookie = this.getCookie();
		cookie = cookie != null ? cookie : WoopraTrackerEE.randomCookie();
		this.currentConfig.put(WoopraTrackerEE.COOKIE_VALUE, cookie);
		this.currentConfig.put(WoopraTrackerEE.IP_ADDRESS, this.getIpAddress());
		this.currentConfig.put(WoopraTrackerEE.USER_AGENT, this.getUserAgent());
		this.currentConfig.put(WoopraTrackerEE.CURRENT_URL, this.getCurrentUrl());
		this.hasPushed = false;
		this.setWoopraCookie();
	}
	
	/**
	 * Tracks a page view through the front-end.
	 */
	public WoopraTracker track() {
		return this.track(new WoopraEvent());
	}
	
	/**
	 * Tracks a page view.
	 * @param backEndProcessing (boolean) Should the page view event be tracked through the back-end?
	 * @return WoopraTracker
	 */
	public WoopraTracker track(boolean backEndProcessing) {
		if(backEndProcessing) {
			return super.track();
		} else {
			return this.track();
		}
	}
	
	/**
	 * Tracks a custom event through the front-end.
	 * @param event	(WoopraEvent) the event to track
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
	 * @return
	 */
	public WoopraTracker track(WoopraEvent event, boolean backEndProcessing) {
		if(backEndProcessing) {
			return super.track(event);
		} else {
			return this.track(event);
		}
	}
	
	/**
	 * Tracks a custom event through the front-end.
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
	 * Tracks a custom event.
	 * @param name (String) the name of the custom event
	 * @param properties 	(Object[][]) 2D Array containing Arrays of size 2 where:<br>
	 * 				key (String) - property name<br>
	 * 				value (String, int, boolean) - property value<br>
	 * @param backEndProcessing (boolean) Should the event be tracked through the back-end?
	 * @return
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
	 * @param backEndProcessing (boolean) Should the identification be pushed through the back-end?
	 */
	public void push(boolean backEndProcessing) {
		if(backEndProcessing) {
			super.push();
		} else {
			this.push();
		}
	}
	
	/**
	 * Returns the JavaScript code for configuring, identifying, tracking, and pushing.
	 * Call this function after all front-end actions, and place its return value in your page's header.
	 * @return (String) the JavaScript code for all front-end tracking.
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
			if(cookies[i].getName().equals(this.currentConfig.get(WoopraTrackerEE.COOKIE_NAME))) {
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
	
	protected void actualizeCookie() {
		String cookie = this.getCookie();
		if (cookie != null) {
			this.currentConfig.put(WoopraTrackerEE.COOKIE_VALUE, cookie);
		}
	}
	
	public void setWoopraCookie() {
		Cookie cookie = new Cookie( (String) this.currentConfig.get(WoopraTrackerEE.COOKIE_NAME), (String) this.currentConfig.get(WoopraTrackerEE.COOKIE_VALUE));
		cookie.setMaxAge(60*60*24*365*2);
		this.response.addCookie(cookie);
	}
}
