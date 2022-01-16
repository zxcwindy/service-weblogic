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
 * 只提供日、周、月数据，不包含分钟数据。前复权
 *
 * @author david 2022年1月16日
 */
@Component
public class TushareDataFetcher implements DataFetcher {
	/**
	 * 盘后周期数据接口
	 */
	public static final String BAO_DATA_URL = "http://127.0.0.1:5551/tushare/";

	@Autowired
	private RestTemplate restClient;

	private String dateFormat = "yyyyMMdd";

	/**
	 * 为了方式短时间内重复登录
	 */
	private Map<Boolean, Long> isLoginMap = new HashMap<>();

	@Override
	public List<CandleEntry> fetchData(Period period, String code, String startDate, String endDate) {
		ResponseEntity<Map> responseData = restClient.getForEntity(
				BAO_DATA_URL + "item/" + code + "?" + buildParam(period.toString(), startDate, endDate), Map.class);
		List<String> colList = (List<String>) responseData.getBody().get("columns");
		int high = 0, low = 0, open = 0, close = 0, volume = 0, amount = 0, pctChg = 0;

		for (int i = 2; i < colList.size(); i++) {
			if ("high".equals(colList.get(i))) {
				high = i;
			} else if ("low".equals(colList.get(i))) {
				low = i;
			} else if ("open".equals(colList.get(i))) {
				open = i;
			} else if ("close".equals(colList.get(i))) {
				close = i;
			} else if ("vol".equals(colList.get(i))) {
				volume = i;
			} else if ("amount".equals(colList.get(i))) {
				amount = i;
			} else if ("pct_chg".equals(colList.get(i))) {
				pctChg = i;
			}
		}
		List<List<Object>> dataList = (List<List<Object>>) responseData.getBody().get("data");
		SimpleDateFormat sFormat = new SimpleDateFormat(dateFormat);
		List<CandleEntry> result = new ArrayList<>();
		for (List<Object> strList : dataList) {
			try {
				result.add(new CandleEntry(sFormat.parse(strList.get(1).toString()),
						high == 0 || strList.get(high) == null ? 0 : (double) strList.get(high),
						low == 0 || strList.get(low) == null ? 0 : (double) strList.get(low),
						open == 0 || strList.get(open) == null ? 0 : (double) strList.get(open),
						close == 0 || strList.get(close) == null ? 0 : (double) strList.get(close),
						volume == 0 || strList.get(volume) == null ? 0 : (double) strList.get(volume),
						amount == 0 || strList.get(amount) == null ? 0
								: (Period.Day.equals(period) ? (double) strList.get(amount) * 1000
										: (double) strList.get(amount)),
						0, pctChg == 0 || strList.get(pctChg) == null ? 0 : (double) strList.get(pctChg)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public SourceEnum getCode() {
		return SourceEnum.Tushare;
	}

	public void login() {
		// 30秒内不再重复登录
		if (isLoginMap.get(true) == null || isLoginMap.get(true) - System.currentTimeMillis() < -30000) {
			this.restClient.getForEntity(BAO_DATA_URL + "login", String.class);
			isLoginMap.put(true, System.currentTimeMillis());
		}

	}

	public void logout() {
		if (isLoginMap.get(true) != null) {
			isLoginMap.put(true, null);
		}
	}

	private String buildParam(String type, String startDate, String endDate) {
		return "&type=" + type.toUpperCase() + "&startDate=" + startDate.replaceAll("-", "") + "&endDate="
				+ endDate.replaceAll("-", "");
	}
}
