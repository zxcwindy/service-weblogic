package org.zxc.service.service;

import static org.zxc.service.stock.Constans.BAO_DATA_URL;
import static org.zxc.service.stock.Constans.STOCK_DB_NAME;

import java.io.Serializable;
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

import org.apache.commons.lang.StringUtils;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.Period;
import org.zxc.service.util.Arithmetic;

/**
 *进行kdj、ma、macd，vol等指标的计算结果支持
 * @author david
 * 2022年1月1日
 */
@Service
public class BaoStockKpiService extends LogService{

	private List<String> codeList;

	private List<String> errorList;

	private static Map<Period, String[]> periodMap = new HashMap<>();

	private static final String CACHE_NAME = "stock-cache";

	private boolean isLogin = false;

	private static final CacheManager CACHE_MANAGER = CacheManagerBuilder.newCacheManagerBuilder()
		.with(CacheManagerBuilder.persistence(System.getProperty("java.io.tmpdir")+ "/" + CACHE_NAME))
		.build(true);

	static{
		CACHE_MANAGER.createCache(CACHE_NAME, CacheConfigurationBuilder.newCacheConfigurationBuilder(
				String.class, Serializable.class,
				ResourcePoolsBuilder.newResourcePoolsBuilder()
		    .heap(12, EntryUnit.ENTRIES)
		    .disk(1024, MemoryUnit.MB)
		    ).withExpiry(Expirations.timeToLiveExpiration(Duration.of(8, TimeUnit.HOURS))).build());
	}

	private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(),
			new ThreadPoolExecutor.AbortPolicy());

	@Autowired
	private RestTemplate restClient;

	@Autowired
	private DBDataService<Map> dbDataService;

	/**
	 * 优先从缓存中取数据，包含计算结果的api
	 * @param code
	 * @param period
	 * @param refresh
	 * @return
	 */
	public List<CandleEntry> queryData(String code,Period period,boolean refresh){
		Serializable obj =  getCache().get(code+period);
		if(obj != null && !refresh && ((List<CandleEntry>) obj).size()>0){
			return (List<CandleEntry>) obj;
		}else{
			try {
				if(!isLogin){
					login();
				}
				List<CandleEntry> entryList = new FetchDataRunnable(code,period).call();
				getCache().put(code+period, (ArrayList<CandleEntry>)entryList);
				return entryList;
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
		}
	}

//	@Scheduled(cron = "0 15 19 * * 1-5")
	public void updateData() {
		scheduleLog = "";
		updatePeriod();
		updatetStockList();
		login();
		log(new Date().toString() + " begin refresh");
		errorList = new ArrayList<>();
		this.codeList.stream().forEach(code -> {
			try {
				Future<List<CandleEntry>> m30 = EXECUTOR.submit(new FetchDataRunnable(code,Period.M30));
				Future<List<CandleEntry>> day = EXECUTOR.submit(new FetchDataRunnable(code,Period.Day));
				Future<List<CandleEntry>> week = EXECUTOR.submit(new FetchDataRunnable(code,Period.Week));
				Future<List<CandleEntry>> month = EXECUTOR.submit(new FetchDataRunnable(code,Period.Month));
				getCache().put(code+Period.M30, (ArrayList<CandleEntry>)m30.get());
				getCache().put(code+Period.Day, (ArrayList<CandleEntry>)day.get());
				getCache().put(code+Period.Week, (ArrayList<CandleEntry>)week.get());
				getCache().put(code+Period.Month, (ArrayList<CandleEntry>)month.get());
			} catch (Exception e) {
				errorList.add(code);
				e.printStackTrace();
			}
		});
		log(new Date().toString() + " end refresh");
		logout();
	}

	public void login(){
		this.restClient.getForEntity(BAO_DATA_URL+"login",String.class);
		updatePeriod();
		this.isLogin = true;
	}

	@Scheduled(cron = "0 15 1 * * 1-7")
	public void logout(){
		this.isLogin = false;
		this.restClient.getForEntity(BAO_DATA_URL+"logout",String.class);
	}

	private void updatePeriod() {
		periodMap.put(Period.M30, calcPeriodDate(Period.M30,-60));
		periodMap.put(Period.Day, calcPeriodDate(Period.Day,-450));
		periodMap.put(Period.Week, calcPeriodDate(Period.Week, -64 * 7 * 4));
		periodMap.put(Period.Month, calcPeriodDate(Period.Month,-365 * 6 * 4));
	}

	private String[] calcPeriodDate(Period period,int periodNum) {
		String[] dates = new String[2];
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();

		dates[0] = format.format(cal.getTime());
		cal.add(Calendar.DATE, periodNum);
		dates[1] = format.format(cal.getTime());
		return dates;
	}

	private List<String> updatetStockList() {
		this.codeList = new ArrayList<>();
		try {
			Map<String, List> resultMap = dbDataService.query(STOCK_DB_NAME, "select distinct(item_code) from dim_stock_group_code", 10000);
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

	private static org.ehcache.Cache<String, Serializable> getCache(){
		return CACHE_MANAGER.getCache(CACHE_NAME, String.class, Serializable.class);
	}

	private class FetchDataRunnable implements Callable<List<CandleEntry>> {

		private String code;

		private Period period;

		public FetchDataRunnable(String code,Period period) {
			this.code = code;
			this.period = period;
		}

		/*
		* 获取30,日,周,月数据
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public List<CandleEntry> call() throws Exception {
			List<CandleEntry> entryList = getBaseData();
			calcKpi(entryList);
			return entryList;
		}

		private  void calcKpi(List<CandleEntry> entryList){
//			计算ma
			Arithmetic.calcMA(entryList, 5);
			Arithmetic.calcMA(entryList, 10);
			Arithmetic.calcMA(entryList, 20);
			Arithmetic.calcMA(entryList, 30);
			Arithmetic.calcMA(entryList, 60);
//			计算kdj
			Arithmetic.calcKDJ(entryList, 9, 3, 3, 1);
//			计算macd
			Arithmetic.calcMACD(entryList, 12, 26, 9);
//			计算布林
			Arithmetic.calcMB(entryList, 20, 2);
		}

		private List<CandleEntry> getBaseData(){
			String dateFormat   = "yyyy-MM-dd";
			if(Period.M30.equals(period)){
				dateFormat   = "yyyyMMddHHmmssSSS";
			}
			List<CandleEntry> entryList = getOriginData(period, dateFormat,periodMap.get(period)[1],periodMap.get(period)[0]);

//			周和月时需要手动计算当前的值
			if(Period.Week.equals(period) || Period.Month.equals(period)){
				String[] dates = calcPeriodDate(Period.Day,-40);
				List<CandleEntry> entryDayList =getOriginData(Period.Day, dateFormat,dates[1],dates[0]);
				int daySize = entryDayList.size();
				CandleEntry lastEntry =  null;
				if(Period.Week.equals(period)){
					lastEntry = buildPeriodCandleEntry(entryDayList,getFirstDayOfWeek(daySize > 0?entryDayList.get(daySize - 1).getTime() : null));
				}else{
					lastEntry = buildPeriodCandleEntry(entryDayList,getFirstDayOfMonth(daySize > 0?entryDayList.get(daySize - 1).getTime() : null));
				}
				int size = entryList.size();
				if(size > 0 && !entryList.get(size -1).getTime().equals(lastEntry.getTime())){
					entryList.add(lastEntry);
				}

			}
			return entryList;
		}

		/**
		 * 获取基础数据
		 * @param period
		 * @param format
		 * @param startDate
		 * @param endDate
		 * @return
		 */
		private List<CandleEntry> getOriginData(Period period, String format,String startDate, String endDate) {
			ResponseEntity<Map> responseData = restClient.getForEntity(
					BAO_DATA_URL + "item/" + code + "?"
							+ buildParam(period.toString(), startDate, endDate),
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
				    result.add(new CandleEntry(sFormat.parse(nvlNum(strList.get(0))), Double.parseDouble(nvlNum(strList.get(1))),
							       Double.parseDouble(nvlNum(strList.get(2))), Double.parseDouble(nvlNum(strList.get(3))),
							       Double.parseDouble(nvlNum(strList.get(4))),Double.parseDouble(nvlNum(strList.get(5))),
							       Double.parseDouble(nvlNum(strList.get(6))),is30 ? 0 : Double.parseDouble(nvlNum(strList.get(7))),
							       is30? 0: Double.parseDouble(nvlNum(strList.get(8)))));
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

		private String nvlNum(String num){
			return StringUtils.isEmpty(num)? "0" : num;
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
				double sumVol = 0f;
				for(CandleEntry item : tempList){
					sumVol += item.getVolume();
				}
				result.setVolume(sumVol);
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
}
