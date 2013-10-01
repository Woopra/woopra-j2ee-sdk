package com.woopra.java.sdk;

import com.woopra.json.JSONObject;

public class WoopraEvent {
	
	public String name;
	public JSONObject properties = new JSONObject();
	
	public WoopraEvent(String eventName) {
		this.name = eventName;
	}
	
	public WoopraEvent(String eventName, Object[][] properties) {
		this.name = eventName;
		for(Object[] keyValue : properties) {
			this.properties.put((String) keyValue[0], keyValue[1]);
		}
	}
	
	public WoopraEvent() {}
	
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