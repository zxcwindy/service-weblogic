package org.zxc.service.datasource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.Period;

/**
 *腾讯数据接口，只能获取最后的不复权日数据，为当前实时数据
 * @author david
 * 2022年4月4日
 */
@Component
public class TencentDataFetcher implements DataFetcher{

	private static final String BAO_DATA_URL = "http://qt.gtimg.cn/q=";
	
	@Autowired
	private RestTemplate restClient;
	
	private String dateFormat   = "yyyy-MM-dd";
	
	@Override
	public List<CandleEntry> fetchData(Period period, String code, String startDate, String endDate) {
		return null;
	}
	
	/**
	 * 分组查询最后一天的实时数据，用于盘中更新
	 * @param codes
	 * @return
	 */
	public Map<String,CandleEntry> fetchData(List<String> codes){
		List<String> params = buildParam(codes);
		Map<String,CandleEntry> result = new HashMap<>();
		for(int i = 0 ;i < params.size(); i++){
			ResponseEntity<String> responseData = restClient.getForEntity(
					BAO_DATA_URL + params.get(i),
					String.class);
			String[] dataList = responseData.getBody().split(";");
			for (String strData : dataList) {
//				v_sz000858="51~五 粮 液~000858~159.90~155.06~155.00~287495~147166~140329~159.89~82~159.88~5~159.87~12~159.86~37~159.85~78~159.90~350~159.91~36~159.92~10~159.93~13~159.94~23~~20220401161403~4.84~3.12~162.23~154.45~159.90/287495/4581621784~287495~458162~0.74~27.30~~162.23~154.45~5.02~6206.53~6206.69~6.67~170.57~139.55~1.15~-218~159.36~26.87~31.10~~~1.47~458162.1784~0.0000~0~ ~GP-A~-28.19~1.72~1.61~24.44~20.17~323.65~149.11~-4.76~-9.34~-29.09~3881508125~3881608125~-33.75~-41.78~3881508125~";
				String[] codeArray = strData.split("=");
				if(codeArray.length==2){
					String[] values = codeArray[1].split("~");
					result.put(codeArray[0].replace("v_", "").replace("\n",""), new CandleEntry(new Date(), Double.parseDouble(DataFetcher.nvlNum(values[33])),
						       Double.parseDouble(DataFetcher.nvlNum(values[34])), Double.parseDouble(DataFetcher.nvlNum(values[5])),
						       Double.parseDouble(DataFetcher.nvlNum(values[3])),Double.parseDouble(DataFetcher.nvlNum(values[6])),
						       Double.parseDouble(DataFetcher.nvlNum(values[37]))*10000,Double.parseDouble(DataFetcher.nvlNum(values[38])),
						       Double.parseDouble(DataFetcher.nvlNum(values[32]))));
				}
			}
		}
		return result;
	}
	
	/**
	 * 每组50个code参数
	 * @param codes
	 * @return
	 */
	private List<String> buildParam(List<String> codes){
		List<String> resultList = new ArrayList<>();
		int index = 0;
		List<String> tempList = new ArrayList<>();
		for(int i = 0; i < codes.size(); i++){
			tempList.add(codes.get(i));
			if(index == 50){
				resultList.add(StringUtils.join(tempList, ","));
				tempList = new ArrayList<>();
				index = 0;
			}
		}
		resultList.add(StringUtils.join(tempList, ","));
		return resultList;
	}

	@Override
	public SourceEnum getCode() {
		return SourceEnum.Tencent;
	}

	@Override
	public void login() {
	}

	@Override
	public void logout() {
	}

}
