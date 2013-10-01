package com.woopra.java.sdk;

import com.woopra.json.JSONObject;

public class WoopraEvent {
	
	public String name;
	public JSONObject properties = new JSONObject();
	
	public WoopraEvent setName(String eventName) {
		this.name = eventName;
		return this;
	}
	
	public void setProperty(String key, Object value) {
		this.properties.put(key, value);
	}
	
	public String toString() {
		return this.properties.toString();
	}
}