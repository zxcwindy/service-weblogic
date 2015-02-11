package org.zxc.service.provider;

import org.zxc.service.application.MyObjectMapperProvider;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;



public class MyObjectMapper {

	private final ObjectMapper defaultObjectMapper;
	public MyObjectMapper() {
		defaultObjectMapper = createDefaultMapper();
	}

	public ObjectMapper getContext(Class<?> type) {
		return defaultObjectMapper;
	}
	
	private static ObjectMapper createDefaultMapper() {

		ObjectMapper result = new ObjectMapper();
		result.setDateFormat(MyObjectMapperProvider.DATA_FORMAT);
		return result;
	}

}
