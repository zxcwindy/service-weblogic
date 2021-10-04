package org.zxc.service.service;

import static org.zxc.service.stock.Constans.BAO_DATA_URL;
import static org.zxc.service.stock.Constans.STOCK_DB_NAME;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.zxc.service.resource.vo.KDJEntryVo;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.KDJEntry;
import org.zxc.service.stock.Kdj;
import org.zxc.service.stock.Period;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BaoStockService extends LogService{

	private List<String> codeList;

	private static Map<Period, String[]> periodMap = new HashMap<>();

	@Autowired
	private StockService stockService;

	private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(),
			new ThreadPoolExecutor.AbortPolicy());

	@Autowired
	private RestTemplate restClient;
	
	@Autowired
	private DBDataService<Map> dbDataService;

	@Scheduled(cron = "0 15 18 * * 1-5")
	public void updateData() {
		scheduleLog = "";
		updatePeriod();
		updatetStockList();
		log(new Date().toString() + " begin refresh");
		this.codeList.stream().forEach(code -> {
			try {
				Future<KDJEntry> futrue = EXECUTOR.submit(new FetchDataRunnable(code));
				KDJEntry entry = futrue.get();
				KDJEntryVo vo = convertVo(entry);
				saveVo(vo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		log(new Date().toString() + " end refresh");
	}

	private KDJEntryVo convertVo(KDJEntry entry) {
		KDJEntryVo vo = new KDJEntryVo(entry.getCode());
		vo.setM30(stockService.createCandelEntry(entry.getM30List()));
		vo.setDaym((stockService.createCandelEntry(entry.getDayList())));
		vo.setWeekm((stockService.createCandelEntry(entry.getWeekList())));
		vo.setMonthm(((stockService.createCandelEntry(entry.getMonthList()))));
		vo.setType30(entry.getType30());
		vo.setTypeDay(entry.getTypeDay());
		vo.setTypeWeek(entry.getTypeWeek());
		vo.setTypeMonth(entry.getTypeMonth());
		return vo;
	}

	private void saveVo(KDJEntryVo vo) {
		try {
			dbDataService.update(STOCK_DB_NAME, "delete from stock_bao_period_kdj where item_code = ?", vo.getCode());
			ObjectMapper mapper = new ObjectMapper();
			dbDataService.update(STOCK_DB_NAME,
					"insert into stock_bao_period_kdj(item_code,m_30,m_day,m_week,m_month,type_30,type_day,type_week,type_month,sort_value_1,sort_value_2) "
							+ " values (?,?,?,?,?,?,?,?,?,?,?)",
					new Object[] { vo.getCode(), mapper.writeValueAsString(vo.getM30()),
							mapper.writeValueAsString(vo.getDaym()), mapper.writeValueAsString(vo.getWeekm()),
							mapper.writeValueAsString(vo.getMonthm()), vo.getType30(), vo.getTypeDay(),
							vo.getTypeWeek(), vo.getTypeMonth() ,score1(vo),score２(vo)});
		} catch (SQLException | JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	// 先将负数从大到小排序，再将整数从小到大排序
	private double score1(KDJEntryVo vo) {
		if (vo.getScore() > 0) {
			return vo.getScore() * -1000;
		} else {
			return vo.getScore();
		}
	}

	// 将正数和负数取绝对值从小到大排序
	private double score２(KDJEntryVo vo) {
		if (vo.getScore() > 0) {
			return vo.getScore() * -1;
		} else {
			return vo.getScore();
		}
	}

	private void updatePeriod() {
		periodMap.put(Period.M30, calcPeriodDate(Period.M30));
		periodMap.put(Period.Day, calcPeriodDate(Period.Day));
		periodMap.put(Period.Week, calcPeriodDate(Period.Week));
		periodMap.put(Period.Month, calcPeriodDate(Period.Month));
	}

	private String[] calcPeriodDate(Period period) {
		String[] dates = new String[2];
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		;
		dates[0] = format.format(cal.getTime());

		switch (period) {
		case M30:
			cal.add(Calendar.DATE, -10);
			break;
		case Day:
			cal.add(Calendar.DATE, -90);
			break;
		case Week:
			cal.add(Calendar.DATE, -7 * 64);
			break;
		case Month:
			cal.add(Calendar.DATE, -365 * 6);
			break;
		}
		dates[1] = format.format(cal.getTime());
		return dates;
	}

	private List<String> updatetStockList() {
		this.codeList = new ArrayList<>();
		try (InputStream in = StockService.class.getClassLoader().getResourceAsStream("stockList");
				InputStreamReader isr = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(isr);) {
			String code = null;
			while ((code = br.readLine()) != null) {
				codeList.add(code.split("\t")[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return codeList;
	}

	class FetchDataRunnable implements Callable<KDJEntry> {

		private String code;

		public FetchDataRunnable(String code) {
			this.code = code;
		}

		@Override
		public KDJEntry call() throws Exception {
			KDJEntry entry = new KDJEntry(code);
			List<CandleEntry> entry30List = build(Period.M30, "yyyyMMddHHmmssSSS");
			Kdj.calc(entry30List, 9, 3, 3, 1);
			entry.setM30List(entry30List);
			List<CandleEntry> entryDayList = build(Period.Day, "yyyy-MM-dd");
			Kdj.calc(entryDayList, 9, 3, 3, 1);
			entry.setDayList(entryDayList);

			List<CandleEntry> entryWeekList = build(Period.Week, "yyyy-MM-dd");
			Kdj.calc(entryWeekList, 9, 3, 3, 1);
			entry.setWeekList(entryWeekList);

			List<CandleEntry> entryMonthList = build(Period.Month, "yyyy-MM-dd");
			Kdj.calc(entryMonthList, 9, 3, 3, 1);
			entry.setMonthList(entryMonthList);
			return entry;
		}

		private List<CandleEntry> build(Period period, String format) {
			ResponseEntity<Map> result30 = restClient.getForEntity(
					BAO_DATA_URL + "/" + code + "?"
							+ buildParam(period.toString(), periodMap.get(period)[1], periodMap.get(period)[0]),
					Map.class);
			List<List<String>> dataList = (List<List<String>>) result30.getBody().get("data");

			SimpleDateFormat sFormat = new SimpleDateFormat(format);
			List<CandleEntry> result = new ArrayList<>();
			for (List<String> strList : dataList) {
				try {
					result.add(new CandleEntry(sFormat.parse(strList.get(0)), Double.parseDouble(strList.get(1)),
							Double.parseDouble(strList.get(2)), Double.parseDouble(strList.get(3)),
							Double.parseDouble(strList.get(4))));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			result.sort(new Comparator<CandleEntry>() {
				@Override
				public int compare(CandleEntry o1, CandleEntry o2) {
					return o1.getTime().getTime() < o2.getTime().getTime() ? -1 : 1;
				}
			});
			return result;
		}

		private String buildParam(String type, String startDate, String endDate) {
			return "&type=" + type + "&startDate=" + startDate + "&endDate=" + endDate;
		}
	}
}
