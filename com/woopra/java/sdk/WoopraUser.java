package com.woopra.java.sdk;

import com.woopra.json.JSONObject;

public class WoopraUser {
	
	public JSONObject properties = new JSONObject();
	
	public void setProperty(String key, Object value) {
		this.properties.put(key, value);
	}
	
	public String toString() {
		return this.properties.toString();
	}
	
}