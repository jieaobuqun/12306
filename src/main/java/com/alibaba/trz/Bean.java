package com.alibaba.trz;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Bean {
	
	private String randCode;
	
	private Map<String, String> cookie = new HashMap<String, String>();
	
	public Bean () {
		cookie.put("current_captcha_type", "Z");
	}

	public void updateCookie(String name, String value) {
		cookie.put(name, value);
	}
	
	public void removeCookie(String name) {
		cookie.remove(name);
	}
	
	public String getCookie() {
		String result = "";
		Iterator<Map.Entry<String, String>> it = cookie.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> head = it.next();
			result += head.getKey() + "=" + head.getValue();
			
			if (it.hasNext())
				result += "; ";
		}
		
		return result;
	}
	
	public String getRandCode() {
		return randCode;
	}

	public void setRandCode(String randCode) {
		this.randCode = randCode;
	}
}
