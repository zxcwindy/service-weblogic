package org.zxc.service.service;

import static org.zxc.service.datasource.BaoStockDataFetcher.BAO_DATA_URL;
import static org.zxc.service.stock.Constans.STOCK_DB_NAME;

import java.io.IOException;
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
import org.zxc.service.resource.vo.CandleEntryVo;
import org.zxc.service.resource.vo.KDJEntryVo;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.KDJEntry;
import org.zxc.service.stock.Kdj;
import org.zxc.service.stock.Period;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *主要提供kdj判断
 * @author david
 * 2022年1月1日
 */
@Service
public class BaoStockService extends LogService{

	private List<String> codeList;
	
	private List<String> errorList;

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

//	@Scheduled(cron = "0 15 18 * * 1-5")
	public void updateData() {
		scheduleLog = "";
		updatePeriod();
		updatetStockList();
		try {
			dbDataService.update(STOCK_DB_NAME, "truncate table stock_bao_period_kdj"); 
			login();
			log(new Date().toString() + " begin refresh");
			errorList = new ArrayList<>();
			this.codeList.stream().forEach(code -> {
				try {
					Future<KDJEntry> futrue = EXECUTOR.submit(new FetchDataRunnable(code));
					KDJEntry entry = futrue.get();
					KDJEntryVo vo = convertVo(entry);
					saveVo(vo);
				} catch (Exception e) {
					errorList.add(code);
					e.printStackTrace();
				}
			});
			log(new Date().toString() + " end refresh");
			logout();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void login(){
		this.restClient.getForEntity(BAO_DATA_URL+"login",String.class);
	}
	
	private void logout(){
		this.restClient.getForEntity(BAO_DATA_URL+"logout",String.class);
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
		try {
			Map<String, List> resultMap = dbDataService.query(STOCK_DB_NAME, "select item_code from dim_stock_50y", 10000);
			List<Object[]> dataList = resultMap.get("data");
			for(Object[] data : dataList){
				if(data.length > 0){
					this.codeList.add(data[0].toString());
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return codeList;
	}

	class FetchDataRunnable implements Callable<KDJEntry> {

		private String code;

		public FetchDataRunnable(String code) {
			this.code = code;
		}

		/* 
		* 获取30,日,周,月数据
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public KDJEntry call() throws Exception {
			KDJEntry entry = new KDJEntry(code);
			List<CandleEntry> entry30List = build(Period.M30, "yyyyMMddHHmmssSSS");
			Kdj.calc(entry30List, 9, 3, 3, 1);
			entry.setM30List(entry30List);
			List<CandleEntry> entryDayList = build(Period.Day, "yyyy-MM-dd");
			Kdj.calc(entryDayList, 9, 3, 3, 1);
			entry.setDayList(entryDayList);
			
			int daySize = entryDayList.size();
			
			List<CandleEntry> entryWeekList = build(Period.Week, "yyyy-MM-dd");
			CandleEntry lastWeekEntry = buildPeriodCandleEntry(entryDayList,getFirstDayOfWeek(daySize > 0?entryDayList.get(daySize - 1).getTime() : null));
			int weekSize = entryWeekList.size();
			if(weekSize > 0 && !entryWeekList.get(weekSize -1).getTime().equals(lastWeekEntry.getTime())){
				entryWeekList.add(lastWeekEntry);
			}
			Kdj.calc(entryWeekList, 9, 3, 3, 1);
			entry.setWeekList(entryWeekList);

			List<CandleEntry> entryMonthList = build(Period.Month, "yyyy-MM-dd");
			CandleEntry lastMonthEntry = buildPeriodCandleEntry(entryDayList,getFirstDayOfMonth(daySize > 0?entryDayList.get(daySize - 1).getTime() : null));
			int monthSize = entryMonthList.size();
			if(monthSize > 0 && !entryMonthList.get(monthSize -1).getTime().equals(lastMonthEntry.getTime())){
				entryMonthList.add(lastMonthEntry);
			}
			Kdj.calc(entryMonthList, 9, 3, 3, 1);
			entry.setMonthList(entryMonthList);
			return entry;
		}

		private List<CandleEntry> build(Period period, String format) {
			ResponseEntity<Map> responseData = restClient.getForEntity(
					BAO_DATA_URL + "item/" + code + "?"
							+ buildParam(period.toString(), periodMap.get(period)[1], periodMap.get(period)[0]),
					Map.class);
			List<List<String>> dataList = (List<List<String>>) responseData.getBody().get("data");

			SimpleDateFormat sFormat = new SimpleDateFormat(format);
			List<CandleEntry> result = new ArrayList<>();
			boolean is30 = false;
			if(dataList.size() > 0){
				is30 = dataList.get(0).size() == 7;
			}
			for (List<String> strList : dataList) {
				try {
					result.add(new CandleEntry(sFormat.parse(strList.get(0)), Double.parseDouble(strList.get(1)),
							Double.parseDouble(strList.get(2)), Double.parseDouble(strList.get(3)),
							Double.parseDouble(strList.get(4)),Double.parseDouble(strList.get(5)),
							Double.parseDouble(strList.get(6)),is30 ? 0 : Double.parseDouble(strList.get(7)),
									is30? 0: Double.parseDouble(strList.get(8))));
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
		
		/**
		 * 由于baostock的周和月数据需要等本周/月完结后再统计，为保证数据的实时性，需要手动计算当周/月的数据．
		 * @param entryDayList
		 * @param beginDate
		 * @param endDate
		 * @return
		 */
		private CandleEntry buildPeriodCandleEntry(List<CandleEntry> entryDayList,Date beginDate){
			CandleEntry result = new CandleEntry();
			List<CandleEntry>  tempList = new ArrayList<>();
//			第一个元素是最后一天，最后一个元素是第一天
			for(int i = entryDayList.size() -1; i> -1;i--){
				if(entryDayList.get(i).getTime().getTime() >= beginDate.getTime()){
					tempList.add(entryDayList.get(i));
				}else{
					break;
				}
			}			
			int length = tempList.size();
			if(length > 0){
				result.setTime(tempList.get(0).getTime());
				result.setClose(tempList.get(0).getClose());
				result.setOpen(tempList.get(length - 1).getOpen());
				result.setHigh(tempList.stream().max((a,b) -> {return a.getHigh() > b.getHigh() ? 1 : -1;}).get().getHigh());
				result.setLow(tempList.stream().min((a,b) -> {return a.getLow() > b.getLow() ? 1 : -1;}).get().getLow());
			}
			return result;	
		}
		
		private Date getFirstDayOfWeek(Date date){
			Calendar cal = Calendar.getInstance();
			if(date != null){
				cal.setTime(date);
			}
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			cal.add(Calendar.DATE, dayOfWeek > 1 ? (dayOfWeek - 2) * -1 : 0);
			resetDate(cal);
			return cal.getTime();
		}
		
		private Date getFirstDayOfMonth(Date date){
			Calendar cal = Calendar.getInstance();
			if(date != null){
				cal.setTime(date);
			}
			cal.set(Calendar.DAY_OF_MONTH,1);
			resetDate(cal);
			return cal.getTime();
		}
		
		private void resetDate(Calendar cal){
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		}
	}

	public KDJEntryVo queryM(String code) throws SQLException, JsonParseException, JsonMappingException, IOException {
		Map<String, List> result = dbDataService.query(STOCK_DB_NAME, "select item_code,m_30,m_day,m_week,m_month from stock_bao_period_kdj where item_code = ?", code);
		
		if(result.get("data").size() > 0){
			Object[] datas = (Object[]) result.get("data").get(0);
			ObjectMapper mapper = new ObjectMapper();
			KDJEntryVo vo = new KDJEntryVo(code);
			vo.setM30(mapper.readValue(datas[1].toString(),new TypeReference<List<CandleEntryVo>>() {}));
			vo.setDaym(mapper.readValue(datas[2].toString(),new TypeReference<List<CandleEntryVo>>() {}));
			vo.setWeekm(mapper.readValue(datas[3].toString(),new TypeReference<List<CandleEntryVo>>() {}));
			vo.setMonthm(mapper.readValue(datas[4].toString(),new TypeReference<List<CandleEntryVo>>() {}));
			return vo;
		}else{
			return stockService.queryM(code.startsWith("6")? "sh"+code : "sz"+code);
		}
	}
}
