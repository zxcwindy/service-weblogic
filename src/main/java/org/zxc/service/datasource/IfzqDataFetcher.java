package org.zxc.service.datasource;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.Period;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 腾讯接口，获取前复权日、周、月数据
 * 
 * @author david
 *
 */
@Component
public class IfzqDataFetcher implements DataFetcher {

	private static final String BAO_DATA_URL = "https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?param=code,period,,,301,qfq";
	
	private static String dateFormat   = "yyyy-MM-dd";
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	private RestTemplate restClient;

	@Override
	public List<CandleEntry> fetchData(Period period, String code, String startDate, String endDate) {
		ResponseEntity<String> responseData = restClient.getForEntity(
				buildUrl(period,code), String.class);
		String result = responseData.getBody();
		Map data;
		try {
			data = (Map) mapper.readValue(result, Map.class).get("data");
			List<List<Object>> resultList = (List<List<Object>>) ((Map)data.get(code)).get("qfq"+convertPeriod(period));
			SimpleDateFormat sFormat = new SimpleDateFormat(dateFormat);
			return resultList.stream().map(os -> {
				try {
					return new CandleEntry(sFormat.parse(os.get(0).toString()), Double.parseDouble(DataFetcher.nvlNum(os.get(3).toString())), 
							Double.parseDouble(DataFetcher.nvlNum(os.get(4).toString())), Double.parseDouble(DataFetcher.nvlNum(os.get(1).toString())), 
							Double.parseDouble(DataFetcher.nvlNum(os.get(2).toString())), Double.parseDouble(DataFetcher.nvlNum(os.get(5).toString())),
							0, 0, 0);
				} catch (NumberFormatException | ParseException e) {
					e.printStackTrace();
					return null;
				}
			}).collect(Collectors.toList());
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		
	}

	@Override
	public SourceEnum getCode() {
		return SourceEnum.IFZQ;
	}

	@Override
	public void login() {
	}

	@Override
	public void logout() {
	}

	private String buildUrl(Period period, String code) {
		return BAO_DATA_URL.replaceAll("code", code).replaceAll("period", convertPeriod(period));
	}
	
	private String convertPeriod(Period period) {
		switch (period) {
			case Day:
				return "day";
			case Week:
				return "week";
			case Month:
				return "month";
			default:
				throw new RuntimeException("不支持的周期");
		}
	}
}
