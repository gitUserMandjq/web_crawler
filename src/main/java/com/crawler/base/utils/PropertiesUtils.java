package com.crawler.base.utils;

import java.util.Properties;

public class PropertiesUtils {
	public static String readValue(Properties property, String key) {
		String value = property.getProperty(key);
		if(value != null) {
			int startIndex = value.indexOf("${");
			if(startIndex != -1) {
				int endIndex = value.indexOf("}");
				if (endIndex != -1) {
					String placeholder = value.substring(startIndex + 2, endIndex);
					if(placeholder.indexOf(":") > -1) {
						String[] split = placeholder.split(":");
						String getenv = System.getenv(split[0]);
						if(getenv == null) {
							return split[1];
						}
						return getenv;
					}else {
						return System.getenv(placeholder);
					}
				}
			}
		}
		return value;
	}
}
