package org.zxc.service.provider;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

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
		result.configure(Feature.INDENT_OUTPUT, true);

		return result;
	}

}
