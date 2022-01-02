package org.zxc.service.provider;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 *修正科学技术法
 * @author david
 * 2022年1月2日
 */
public class CustomerDoubleFESerialize extends JsonSerializer<Double>{

	@Override
	public void serialize(Double value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		BigDecimal d = new BigDecimal(value.toString());
		jgen.writeNumber(d.stripTrailingZeros().toPlainString());
	}

}
