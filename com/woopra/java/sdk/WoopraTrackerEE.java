package com.woopra.java.sdk;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.woopra.json.JSONObject;
import com.woopra.sss.test.AsyncClient;

public class WoopraTrackerEE extends WoopraTracker {
	
	//Request, response and writer
		private HttpServletRequest request;
		
		//Public Constructor
		public WoopraTrackerEE(HttpServletRequest request) {
			
			//Request, response, and writer
			this.request = request;
			
			//Set domain, the cookieDomain, and the IP of the client
			this.currentConfig.put(WoopraTrackerEE.DOMAIN, request.getServerName());
			this.currentConfig.put(WoopraTrackerEE.COOKIE_DOMAIN, request.getServerName());
			if(request.getHeader("HTTP_X_FORWARDED_FOR") != null) {
				this.currentConfig.put(WoopraTrackerEE.IP_ADDRESS, request.getHeader("HTTP_X_FORWARDED_FOR"));
			} else {
				this.currentConfig.put(WoopraTrackerEE.IP_ADDRESS, request.getRemoteAddr());
			}
			
			Cookie[] cookies = request.getCookies();
			for(int i = 0; cookies != null && i < cookies.length; i++) {
				if(cookies[i].getName().equals(this.currentConfig.get(WoopraTrackerEE.COOKIE_NAME))) {
					this.currentConfig.put(WoopraTrackerEE.COOKIE_VALUE, cookies[i].getValue());
				}
			}
			if (this.currentConfig.get(WoopraTrackerEE.COOKIE_VALUE) == "") {
				this.currentConfig.put(WoopraTrackerEE.COOKIE_VALUE, WoopraTracker.randomCookie());
			}
		}
		
		
		public WoopraTracker config(String key, Object value) {
			if(WoopraTracker.defaultConfig.has(key)) {
				if (WoopraTracker.defaultConfig.get(key).getClass() == value.getClass()) {
					this.currentConfig.put(key, value);
					if(! key.equals(WoopraTrackerEE.IP_ADDRESS) && ! key.equals(WoopraTrackerEE.COOKIE_VALUE)) {
						if(this.customConfig == null) {
							this.customConfig = new JSONObject();
						}
						this.customConfig.put(key, value);
						if (key.equals(WoopraTrackerEE.COOKIE_NAME)) {
							Cookie[] cookies = request.getCookies();
							for(int i = 0; i < cookies.length; i++) {
								if(cookies[i].getName().equals(this.currentConfig.get(WoopraTrackerEE.COOKIE_NAME))) {
									this.currentConfig.put(WoopraTrackerEE.COOKIE_VALUE, cookies[i].getValue());
								}
							}
						}
					}
				}
			}
			return this;
		}
		
		protected void woopraHttpRequest(boolean isTracking, WoopraEvent event) {
			try {
				String baseUrl = "http://www.woopra.com/track/";
		
				//Config params
				String configParams = "?host=".concat(URLEncoder.encode((String) this.currentConfig.get(WoopraTrackerEE.DOMAIN), "UTF-8"));
				configParams = configParams.concat("&cookie=").concat(URLEncoder.encode( (String) this.currentConfig.get(WoopraTrackerEE.COOKIE_VALUE), "UTF-8"));
				configParams = configParams.concat("&ip=").concat(URLEncoder.encode( (String) this.currentConfig.get(WoopraTrackerEE.IP_ADDRESS), "UTF-8"));
				configParams = configParams.concat("&timeout=").concat(URLEncoder.encode(this.currentConfig.get(WoopraTrackerEE.IDLE_TIMEOUT).toString(), "UTF-8"));
		
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
						eventParams = eventParams.concat("&ce_name=pv&ce_url=").concat(request.getRequestURL().toString());
					}
					url = baseUrl.concat("ce/").concat(configParams).concat(userParams).concat(eventParams);
				}
				AsyncClient.getInstance().send(new URL(url), request.getHeader("User-Agent"));
			} catch(Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public void setWoopraCookie(HttpServletResponse response) {
			Cookie cookie = new Cookie( (String) this.currentConfig.get(WoopraTrackerEE.COOKIE_NAME), (String) this.currentConfig.get(WoopraTrackerEE.COOKIE_VALUE));
			cookie.setMaxAge(60*60*24*365*2);
			response.addCookie(cookie);
		}
}
