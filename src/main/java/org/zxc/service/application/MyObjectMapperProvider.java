package org.zxc.service.application;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;


public class MyObjectMapperProvider {
	
	public static final DateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	final ObjectMapper defaultObjectMapper;
	final ObjectMapper combinedObjectMapper;

	public MyObjectMapperProvider() {
		defaultObjectMapper = createDefaultMapper();
		combinedObjectMapper = createCombinedObjectMapper();
	}


	public ObjectMapper getContext(Class<?> type) {
		return defaultObjectMapper;
	}

	private static ObjectMapper createDefaultMapper() {
		final ObjectMapper result = new ObjectMapper();
		result.setDateFormat(DATA_FORMAT);
		return result;
	}

	private static ObjectMapper createCombinedObjectMapper() {
		return createDefaultMapper();
	}
	
}
