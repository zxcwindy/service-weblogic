package org.zxc.service.datasource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.Period;

/**
 * 无法获取月k数据，没有金额、换手率，涨跌幅信息，没有复权
 * @author david
 * 2022年1月15日
 */
@Component
public class SinaDataFetcher implements DataFetcher {
	/**
	 * 盘后周期数据接口
	 */
	private static final String BAO_DATA_URL = "https://quotes.sina.cn/cn/api/json_v2.php/CN_MarketDataService.getKLineData?datalen=301&ma=no";

	@Autowired
	private RestTemplate restClient;
	
	private String dateFormat   = "yyyy-MM-dd";
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<CandleEntry> fetchData(Period period, String code , String startDate, String endDate) {
		ResponseEntity<List> responseData = restClient.getForEntity(
				BAO_DATA_URL + buildParam(period,code, startDate, endDate),
				List.class);
		List<Map<String,String>> dataList = (List<Map<String,String>>) responseData.getBody();

		SimpleDateFormat sFormat = new SimpleDateFormat(Period.M30.equals(period)? "yyyy-MM-dd HH:mm:ss" : dateFormat);
		List<CandleEntry> result = new ArrayList<>();
		for (Map<String,String> strMap : dataList) {
			try {
			    result.add(new CandleEntry(sFormat.parse(strMap.get("day")), Double.parseDouble(DataFetcher.nvlNum(strMap.get("high"))),
						       Double.parseDouble(DataFetcher.nvlNum(strMap.get("low"))), Double.parseDouble(DataFetcher.nvlNum(strMap.get("open"))),
						       Double.parseDouble(DataFetcher.nvlNum(strMap.get("close"))),Double.parseDouble(DataFetcher.nvlNum(strMap.get("volume"))),
						       0f,0f,0f));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public SourceEnum getCode() {
		return SourceEnum.Sina;
	}
	
	public void login(){
	
	}

	public void logout(){
		
	}

	private String buildParam(Period period,String code, String startDate, String endDate) {
		String codeParam = "&symbol=" + code;
		switch(period){
			case M30 : return codeParam + "&scale=30";
			case Day : return codeParam + "&scale=240";
			case Week : return codeParam + "&scale=1200";
			default: return codeParam + "&scale=240";
		}
	}
}
