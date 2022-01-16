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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CandleArrayJsonSerializer extends JsonSerializer<CandleEntry> {

	/**
	 * 原本这里是 ##.00 ,带来的问题是如果数据库数据为0.00返回“ .00 “经评论指正，改为0.00
	 */
	private DecimalFormat df = new DecimalFormat("0.00");
	
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public void serialize(CandleEntry entry, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
//		time,open,close,low,high,vol,amount,turn,pctChg,k,d,j,mb,up,dn,ma5,ma10,ma20,ma30,ma60,macd,dif,dea
		jgen.writeObject(new Object[]{
				dtf.format(LocalDateTime.ofInstant(entry.getTime().toInstant(), ZoneId.systemDefault())),
				getHalfUpValue(entry.getOpen()),getHalfUpValue(entry.getClose()),getHalfUpValue(entry.getLow()),getHalfUpValue(entry.getHigh()),
				getFEValue(entry.getVolume()),getFEValue(entry.getAmount()),
				entry.getTurn(),entry.getPctChg(),
				getHalfUpValue(entry.getK()),getHalfUpValue(entry.getD()),getHalfUpValue(entry.getJ()),
				getDoubleFlagValue(entry.getMb()),getDoubleFlagValue(entry.getUp()),getDoubleFlagValue(entry.getDn()),
				getDoubleFlagValue(entry.getMa5()),getDoubleFlagValue(entry.getMa10()),getDoubleFlagValue(entry.getMa20()),getDoubleFlagValue(entry.getMa30()),getDoubleFlagValue(entry.getMa60()),
				getHalfUpValue(entry.getMacd()),getHalfUpValue(entry.getDif()),getHalfUpValue(entry.getDea())
		});
	}

	private String getFEValue(Double value) {
		BigDecimal d = new BigDecimal(value.toString());
		return d.stripTrailingZeros().toPlainString();
	}

	private Double getHalfUpValue(Double value) {
		if (value != null) {
			BigDecimal bigDecimal = new BigDecimal(String.valueOf(value));
			df.setRoundingMode(RoundingMode.HALF_UP);
			// 四舍五入。需要将数据转成bigDecimal, 否则会存在经度丢失问题
			String format = df.format(bigDecimal);
			double aDouble = Double.parseDouble(format);
			return aDouble;// 返回数字格式
		}else{
			return 0.0;
		}
	}

	private Object getDoubleFlagValue(Double value) {
		double aDouble = getHalfUpValue(value);
		return aDouble ==0? "-" :aDouble;
	}
}
