package org.zxc.service.provider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.zxc.service.stock.CandleEntry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 *将 "-" 反序列化时，重置为0.0
 * @author david
 * 2022年4月17日
 */
public class FlagDoubleDeserializer extends JsonDeserializer<Double> {

	@Override
	public Double deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String value = jp.getText();
		if("-".equals(value)){
			return 0.0;
		}else{
			return Double.parseDouble(value);
		}
	}

}
