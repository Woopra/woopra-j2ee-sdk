package com.woopra.java.sdk;

import java.util.Enumeration;
import java.util.Properties;

public class WoopraEvent extends Properties {
	
	public String name;
	
	public WoopraEvent setName(String eventName) {
		this.name = eventName;
		return this;
	}
	
	public String toJavaScriptArray() {
		String JSarray = "{";
		for (Enumeration<?> e = this.propertyNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			JSarray = JSarray.concat("'").concat(key).concat("' : '").concat(this.getProperty(key)).concat("', ");
		}
		JSarray = JSarray.concat("}");
		return JSarray;
	}
}