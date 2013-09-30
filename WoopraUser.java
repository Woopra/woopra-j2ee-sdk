package com.woopra.java.sdk;

import java.util.Enumeration;
import java.util.Properties;

public class WoopraUser extends Properties{

	public WoopraUser setName(String userName) {
		this.setProperty("name", userName);
		return this;
	}
	
	public WoopraUser setEmail(String email) {
		this.setProperty("email", email);
		return this;
	}
	
	public WoopraUser setCompany(String company) {
		this.setProperty("company", company);
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