package org.zxc.service.service;

import static org.zxc.service.stock.Constans.STOCK_DB_NAME;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.zxc.service.datasource.DataFetcher;
import org.zxc.service.datasource.DataFetcherFactory;
import org.zxc.service.datasource.SinaDataFetcher;
import org.zxc.service.datasource.SourceEnum;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.Condition;
import org.zxc.service.stock.Period;
import org.zxc.service.stock.CandleEntryFuntion;
import org.zxc.service.util.Arithmetic;
import org.zxc.service.util.EvalutorHelper;

/**
 * 进行kdj、ma、macd，vol等指标的计算结果支持
 * 
 * @author david 2022年1月1日
 */
@Service
public class StockKpiService extends LogService {

	private List<String> codeList;

	private List<String> errorList;

	private static Map<Period, String[]> periodMap = new HashMap<>();

	// private static final String CACHE_NAME = "stock-cache";

	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private boolean isLogin = false;

	private static final ConcurrentHashMap<String, List<CandleEntry>> CACHE_MANAGER = new ConcurrentHashMap<>();

	// private static final CacheManager CACHE_MANAGER =
	// CacheManagerBuilder.newCacheManagerBuilder()
	// .with(CacheManagerBuilder.persistence(System.getProperty("java.io.tmpdir")+
	// "/" + CACHE_NAME))
	// .build(true);
	//
	// static{
	// CACHE_MANAGER.createCache(CACHE_NAME,
	// CacheConfigurationBuilder.newCacheConfigurationBuilder(
	// String.class, Serializable.class,
	// ResourcePoolsBuilder.newResourcePoolsBuilder()
	// .heap(1024, MemoryUnit.MB)
	// .disk(2048, MemoryUnit.MB)
	// ).withExpiry(Expirations.timeToLiveExpiration(Duration.of(72,
	// TimeUnit.HOURS))).build());//由于1-5更新时会自动清理缓存，故将缓存时间设置为3天，覆盖周末
	// }

	private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(),
			new ThreadPoolExecutor.AbortPolicy());

	@Autowired
	private DBDataService<Map> dbDataService;

	@Autowired
	private DataFetcherFactory dataFetcherFactory;

	private static final Map<Period, SourceEnum> PERIOD_DATA_FETCHER = new HashMap<>();

	@PostConstruct
	private void init() {
		PERIOD_DATA_FETCHER.put(Period.M1, SourceEnum.Sina);
		PERIOD_DATA_FETCHER.put(Period.M5, SourceEnum.Sina);
		PERIOD_DATA_FETCHER.put(Period.M15, SourceEnum.Sina);
		PERIOD_DATA_FETCHER.put(Period.M30, SourceEnum.Sina);
		PERIOD_DATA_FETCHER.put(Period.M60, SourceEnum.Sina);
		PERIOD_DATA_FETCHER.put(Period.M120, SourceEnum.Sina);
		PERIOD_DATA_FETCHER.put(Period.Day, SourceEnum.Tushare);
		PERIOD_DATA_FETCHER.put(Period.Week, SourceEnum.BaoStock);
		PERIOD_DATA_FETCHER.put(Period.Month, SourceEnum.BaoStock);
		updatePeriod();
	}

	public static Map<Period, SourceEnum> getPeriodDataFetcher() {
		return PERIOD_DATA_FETCHER;
	}

	/**
	 * 优先从缓存中取数据，包含计算结果的api
	 * 
	 * @param code
	 * @param period
	 * @param refresh
	 * @return
	 */
	public List<CandleEntry> queryData(String code, Period period, boolean refresh) {
		List<CandleEntry> obj = getCache().get(code + period);
		if (obj != null && !refresh && ((List<CandleEntry>) obj).size() > 0) {
			return (List<CandleEntry>) obj;
		} else {
			try {
				List<CandleEntry> entryList = new FetchDataRunnable(code, period).call();
				getCache().put(code + period, (ArrayList<CandleEntry>) entryList);
				return entryList;
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
		}
	}

	/**
	 * 每晚定时更新数据
	 */
	@Scheduled(cron = "0 15 18 * * 1-5")
	public void updateData() {
		scheduleLog = "";
		updatePeriod();
		updatetStockList();
		getCache().clear();
		login();
		log(dtf.format(LocalDateTime.now()) + " begin refresh");
		errorList = new ArrayList<>();
		this.codeList.stream().forEach(code -> {
			try {
				Future<List<CandleEntry>> m30 = null;
				Future<List<CandleEntry>> day = null;
				Future<List<CandleEntry>> week = null;
				// 避免由于访问次数太多被封掉权限，sina数据不做定时更新，只做实时获取
				if (!(getDataFetcher(Period.M30) instanceof SinaDataFetcher)) {
					m30 = EXECUTOR.submit(new FetchDataRunnable(code, Period.M30));
				}
				if (!(getDataFetcher(Period.Day) instanceof SinaDataFetcher)) {
					day = EXECUTOR.submit(new FetchDataRunnable(code, Period.Day));
				}
				if (!(getDataFetcher(Period.Week) instanceof SinaDataFetcher)) {
					week = EXECUTOR.submit(new FetchDataRunnable(code, Period.Week));
				}
				Future<List<CandleEntry>> month = EXECUTOR.submit(new FetchDataRunnable(code, Period.Month));

				if (m30 != null) {
					getCache().put(code + Period.M30, (ArrayList<CandleEntry>) m30.get());
				}
				if (day != null) {
					getCache().put(code + Period.Day, (ArrayList<CandleEntry>) day.get());
				}
				if (week != null) {
					getCache().put(code + Period.Week, (ArrayList<CandleEntry>) week.get());
				}
				getCache().put(code + Period.Month, (ArrayList<CandleEntry>) month.get());
			} catch (Exception e) {
				errorList.add(code);
				e.printStackTrace();
			}
		});
		log(dtf.format(LocalDateTime.now()) + " end refresh");
		logout();
	}

	/**
	 * 登录各个后端数据
	 */
	public void login() {
		// m5-m120分钟级暂时都用sina，不用登录
		getDataFetcher(Period.M30).login();
		getDataFetcher(Period.Day).login();
		getDataFetcher(Period.Week).login();
		getDataFetcher(Period.Month).login();
		updatePeriod();
		this.isLogin = true;
	}

	/**
	 * 登出各个后端数据
	 */
	public void logout() {
		this.isLogin = false;
		getDataFetcher(Period.M30).logout();
		getDataFetcher(Period.Day).logout();
		getDataFetcher(Period.Week).logout();
		getDataFetcher(Period.Month).logout();
	}

	/**
	 * 每天凌晨更新数据周期
	 */
	@Scheduled(cron = "0 1 0 * * ?")
	public void updatePeriod() {
		periodMap.put(Period.M1, calcPeriodDate(Period.M1, -60));
		periodMap.put(Period.M5, calcPeriodDate(Period.M5, -60));
		periodMap.put(Period.M15, calcPeriodDate(Period.M15, -60));
		periodMap.put(Period.M30, calcPeriodDate(Period.M30, -60));
		periodMap.put(Period.M60, calcPeriodDate(Period.M60, -60));
		periodMap.put(Period.M120, calcPeriodDate(Period.M120, -60));
		periodMap.put(Period.Day, calcPeriodDate(Period.Day, -450));
		periodMap.put(Period.Week, calcPeriodDate(Period.Week, -64 * 7 * 5));
		periodMap.put(Period.Month, calcPeriodDate(Period.Month, -365 * 7 * 4));
	}

	/**
	 * 根据一定条件，查询缓存结果集中符合条件的记录
	 * 
	 * @param condition
	 *            条件集合
	 * @param logicOperator
	 *            与或运算
	 * @return
	 */
	public List<String> findCodeByCond(String condition) {
		return codeList.stream().map((e -> {
			boolean result = EvalutorHelper.eval(condition, queryData(e,Period.M30,false),
					getCache().get(e + Period.Day.toString()), getCache().get(e + Period.Week.toString()),
					getCache().get(e + Period.Month.toString()));
			return result ? e : null;
		})).filter(t -> t != null).collect(Collectors.toList());
	}

	private String[] calcPeriodDate(Period period, int periodNum) {
		String[] dates = new String[2];
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();

		dates[0] = format.format(cal.getTime());
		cal.add(Calendar.DATE, periodNum);
		dates[1] = format.format(cal.getTime());
		return dates;
	}

	public List<String> updatetStockList() {
		this.codeList = new ArrayList<>();
		try {
			Map<String, List> resultMap = dbDataService.query(STOCK_DB_NAME,
					"select distinct(item_code) from dim_stock_group_code", 10000);
			List<Object[]> dataList = resultMap.get("data");
			for (Object[] data : dataList) {
				if (data.length > 0) {
					this.codeList.add(data[0].toString());
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return codeList;
	}

	// private static org.ehcache.Cache<String, Serializable> getCache(){
	// return CACHE_MANAGER.getCache(CACHE_NAME, String.class,
	// Serializable.class);
	// }

	private static ConcurrentHashMap<String, List<CandleEntry>> getCache() {
		return CACHE_MANAGER;
	}

	private DataFetcher getDataFetcher(Period period) {
		return dataFetcherFactory.getDataFetcher(PERIOD_DATA_FETCHER.get(period));
	}

	private class FetchDataRunnable implements Callable<List<CandleEntry>> {

		private String code;

		private Period period;

		public FetchDataRunnable(String code, Period period) {
			this.code = code;
			this.period = period;
		}

		/*
		 * 获取30,日,周,月数据
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public List<CandleEntry> call() throws Exception {
			List<CandleEntry> entryList = getBaseData();
			calcKpi(entryList);
			return entryList;
		}

		private void calcKpi(List<CandleEntry> entryList) {
			// 计算ma
			Arithmetic.calcMA(entryList, 5);
			Arithmetic.calcMA(entryList, 10);
			Arithmetic.calcMA(entryList, 20);
			Arithmetic.calcMA(entryList, 30);
			Arithmetic.calcMA(entryList, 60);
			// 计算kdj
			Arithmetic.calcKDJ(entryList, 9, 3, 3, 1);
			// 计算macd
			Arithmetic.calcMACD(entryList, 12, 26, 9);
			// 计算布林
			Arithmetic.calcMB(entryList, 20, 2);
		}

		private List<CandleEntry> getBaseData() {
			List<CandleEntry> entryList = getOriginData(period, periodMap.get(period)[1], periodMap.get(period)[0]);

			// 周和月时需要手动计算当前的值
			if (Period.Week.equals(period) || Period.Month.equals(period)) {
				String[] dates = calcPeriodDate(Period.Day, -40);
				List<CandleEntry> entryDayList = getOriginData(Period.Day, dates[1], dates[0]);
				int daySize = entryDayList.size();
				CandleEntry lastEntry = null;
				if (Period.Week.equals(period)) {
					lastEntry = buildPeriodCandleEntry(entryDayList,
							getFirstDayOfWeek(daySize > 0 ? entryDayList.get(daySize - 1).getTime() : null));
				} else {
					lastEntry = buildPeriodCandleEntry(entryDayList,
							getFirstDayOfMonth(daySize > 0 ? entryDayList.get(daySize - 1).getTime() : null));
				}
				int size = entryList.size();
				if (size > 0 && !entryList.get(size - 1).getTime().equals(lastEntry.getTime())) {
					entryList.add(lastEntry);
				}

			}
			return entryList;
		}

		/**
		 * 获取基础数据
		 * 
		 * @param period
		 * @param format
		 * @param startDate
		 * @param endDate
		 * @return
		 */
		private List<CandleEntry> getOriginData(Period period, String startDate, String endDate) {
			List<CandleEntry> result = null;
			// 当查询的代码为三大指数时，采用新浪接口查询日级别以下的数据，baostock查询周级别以上的数据。
			if (isZhishu(code)) {
				if (Period.Week.equals(period) || Period.Month.equals(period)) {
					result = getDataFetcher(Period.Month).fetchData(period, code, startDate, endDate);
				} else {
					result = getDataFetcher(Period.M30).fetchData(period, code, startDate, endDate);
				}
			} else {
				result = getDataFetcher(period).fetchData(period, code, startDate, endDate);
			}
			result.sort(new Comparator<CandleEntry>() {
				@Override
				public int compare(CandleEntry o1, CandleEntry o2) {
					return o1.getTime().getTime() < o2.getTime().getTime() ? -1 : 1;
				}
			});
			return result;
		}

		private boolean isZhishu(String code) {
			return "sh000001,sz399001,sz399006".contains(code);
		}

		/**
		 * 由于baostock的周和月数据需要等本周/月完结后再统计，为保证数据的实时性，需要手动计算当周/月的数据．
		 * 
		 * @param entryDayList
		 * @param beginDate
		 * @param endDate
		 * @return
		 */
		private CandleEntry buildPeriodCandleEntry(List<CandleEntry> entryDayList, Date beginDate) {
			CandleEntry result = new CandleEntry();
			List<CandleEntry> tempList = new ArrayList<>();
			// 第一个元素是最后一天，最后一个元素是第一天
			for (int i = entryDayList.size() - 1; i > -1; i--) {
				if (entryDayList.get(i).getTime().getTime() >= beginDate.getTime()) {
					tempList.add(entryDayList.get(i));
				} else {
					break;
				}
			}
			int length = tempList.size();
			if (length > 0) {
				result.setTime(tempList.get(0).getTime());
				result.setClose(tempList.get(0).getClose());
				result.setOpen(tempList.get(length - 1).getOpen());
				result.setHigh(tempList.stream().max((a, b) -> {
					return a.getHigh() > b.getHigh() ? 1 : -1;
				}).get().getHigh());
				result.setLow(tempList.stream().min((a, b) -> {
					return a.getLow() > b.getLow() ? 1 : -1;
				}).get().getLow());
				result.setVolume(tempList.stream().mapToDouble(CandleEntry::getVolume).sum());
				result.setAmount(tempList.stream().mapToDouble(CandleEntry::getAmount).sum());
				if (length > 1) {
					result.setPctChg(((result.getClose() - tempList.get(length - 1).getClose())
							/ tempList.get(length - 1).getClose()) * 100);
				}
			}
			return result;
		}

		private Date getFirstDayOfWeek(Date date) {
			Calendar cal = Calendar.getInstance();
			if (date != null) {
				cal.setTime(date);
			}
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			cal.add(Calendar.DATE, dayOfWeek > 1 ? (dayOfWeek - 2) * -1 : 0);
			resetDate(cal);
			return cal.getTime();
		}

		private Date getFirstDayOfMonth(Date date) {
			Calendar cal = Calendar.getInstance();
			if (date != null) {
				cal.setTime(date);
			}
			cal.set(Calendar.DAY_OF_MONTH, 1);
			resetDate(cal);
			return cal.getTime();
		}

		private void resetDate(Calendar cal) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		}
	}
}
