package org.zxc.service.datasource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.Period;

/**
 * 可以获取30分钟、日、周、月数据，前复权。接口有时候比较慢
 * @author david
 * 2022年1月16日
 */
@Component
public class BaoStockDataFetcher implements DataFetcher {
	/**
	 * 盘后周期数据接口
	 */
	public static final String BAO_DATA_URL = "http://127.0.0.1:5551/baostock/";

	@Autowired
	private RestTemplate restClient;
	
	private String dateFormat   = "yyyy-MM-dd";
	
	/**
	 * 为了方式短时间内重复登录
	 */
	private Map<Boolean,Long> isLoginMap = new HashMap<>();
	
	@Override
	public List<CandleEntry> fetchData(Period period, String code ,String startDate, String endDate) {
		ResponseEntity<Map> responseData = restClient.getForEntity(
				BAO_DATA_URL + "item/" + code + "?"
						+ buildParam(period.toString(), startDate, endDate),
				Map.class);
		List<List<String>> dataList = (List<List<String>>) responseData.getBody().get("data");
		SimpleDateFormat sFormat = new SimpleDateFormat(Period.M30.equals(period)? "yyyyMMddHHmmssSSS" : dateFormat);
		List<CandleEntry> result = new ArrayList<>();
		boolean is30 = false;
		if(dataList.size() > 0){
			is30 = dataList.get(0).size() == 7;
		}
		for (List<String> strList : dataList) {
			try {
			    result.add(new CandleEntry(sFormat.parse(DataFetcher.nvlNum(strList.get(0))), Double.parseDouble(DataFetcher.nvlNum(strList.get(1))),
						       Double.parseDouble(DataFetcher.nvlNum(strList.get(2))), Double.parseDouble(DataFetcher.nvlNum(strList.get(3))),
						       Double.parseDouble(DataFetcher.nvlNum(strList.get(4))),Double.parseDouble(DataFetcher.nvlNum(strList.get(5))),
						       Double.parseDouble(DataFetcher.nvlNum(strList.get(6))),is30 ? 0 : Double.parseDouble(DataFetcher.nvlNum(strList.get(7))),
						       is30? 0: Double.parseDouble(DataFetcher.nvlNum(strList.get(8)))));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public SourceEnum getCode() {
		return SourceEnum.BaoStock;
	}
	
	public void login(){
//		30秒内不再重复登录
		if(isLoginMap.get(true) == null || isLoginMap.get(true) - System.currentTimeMillis() < -30000){
			this.restClient.getForEntity(BAO_DATA_URL+"login",String.class);
			isLoginMap.put(true, System.currentTimeMillis());
		}
		
	}

	public void logout(){
//		避免重复登出
		if(isLoginMap.get(true) != null){
			this.restClient.getForEntity(BAO_DATA_URL+"logout",String.class);
			isLoginMap.put(true, null);
		}
	}

	private String buildParam(String type, String startDate, String endDate) {
		return "&type=" + type + "&startDate=" + startDate + "&endDate=" + endDate;
	}
}
