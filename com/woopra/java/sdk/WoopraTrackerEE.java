package com.woopra.java.sdk;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WoopraTrackerEE extends WoopraTracker {
	
	private HttpServletRequest request;
	
	public WoopraTrackerEE(HttpServletRequest request) {
		this.request = request;
		this.currentConfig.put(WoopraTrackerEE.DOMAIN, this.getDomain());
		this.currentConfig.put(WoopraTrackerEE.COOKIE_DOMAIN, this.getDomain());
		String cookie = this.getCookie();
		cookie = cookie != null ? cookie : WoopraTracker.randomCookie();
		this.currentConfig.put(WoopraTrackerEE.COOKIE_VALUE, cookie);
		this.currentConfig.put(WoopraTrackerEE.IP_ADDRESS, this.getIpAddress());
		this.currentConfig.put(WoopraTrackerEE.USER_AGENT, this.getUserAgent());
		this.currentConfig.put(WoopraTrackerEE.CURRENT_URL, this.getCurrentUrl());
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
	
	public void setWoopraCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie( (String) this.currentConfig.get(WoopraTrackerEE.COOKIE_NAME), (String) this.currentConfig.get(WoopraTrackerEE.COOKIE_VALUE));
		cookie.setMaxAge(60*60*24*365*2);
		response.addCookie(cookie);
	}
}
