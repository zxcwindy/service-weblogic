package org.zxc.service.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.KDJEntry;
import org.zxc.service.stock.Kdj;

@Service
public class StockService {

	/**
	 * 周期数据接口
	 */
	private static final String DATA_URL = "http://quotes.sina.cn/cn/api/json_v2.php/CN_MarketDataService.getKLineData?ma=no";

	/**
	 * 当前数据接口
	 */
	private static final String CURRENT_DATA_URL = "http://hq.sinajs.cn/list=";

	/**
	 * 数据获取最长周期
	 */
	private static final int MAX_LENGTH = 60;

	/**
	 * 30分钟周期
	 */
	private static final int MIN_LENGTH = 8;

	/**
	 * 存放kdj的数据
	 */
	private  Map<String, KDJEntry> kdjMap = new HashMap<>(6400);

	/**
	 * 代码列表
	 */
	private List<String> codeList;

	/**
	 * 当前执行失败的代码
	 */
	private List<String> errorCodeList;

	/**
	 * 日志信息
	 */
	private String scheduleLog = "";

	private List<KDJEntry> periodKdjList = new ArrayList<>();

	private static final ReentrantLock LOCK = new ReentrantLock();

	private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(20, 20, 1000, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(),
			new ThreadPoolExecutor.AbortPolicy());
	
	@Autowired
	private RestTemplate restClient;
	
	@Autowired
	private DBDataService<Map> dbDataService;

	/**
	 * 刷新基础数据
	 */
	@Scheduled(cron = "0 15 2 * * ?")
	@Scheduled(cron = "0 40 9 ? * 1-5")//	10点
	public void refreshAll() {
		codeList = getStockList();
		scheduleLog = "";
		errorCodeList = new ArrayList<>();
		kdjMap.clear();
		codeList.parallelStream().forEach(code -> {
			try {
				fetchData(code);
			} catch (Exception e) {
				try {
					fetchData(code);
				} catch (Exception e1) {
					try {
						fetchData(code);
					} catch (Exception e2) {
						errorCodeList.add(code);
					}
				}
			}
		});
		log(new Date().toString() + " finished refresh"); 
	}

	private void fetchData(String code) {
		ResponseEntity<List> result30 = restClient.getForEntity(DATA_URL + buildParam(code, MAX_LENGTH, 30),
				List.class);
		ResponseEntity<List> resultDay = restClient.getForEntity(DATA_URL + buildParam(code, MAX_LENGTH, 240),
				List.class);
		ResponseEntity<List> resultWeek = restClient.getForEntity(DATA_URL + buildParam(code, MAX_LENGTH, 1200),
				List.class);
		KDJEntry kdjEntry = new KDJEntry(code);
		kdjEntry.setM30List(build((List<Map>) result30.getBody(), "yyyy-MM-dd HH:mm:ss"));
		kdjEntry.setDayList(build((List<Map>) resultDay.getBody(), "yyyy-MM-dd"));
		kdjEntry.setWeekList(build((List<Map>) resultWeek.getBody(), "yyyy-MM-dd"));
		kdjEntry.setLastDate(kdjEntry.getM30List().get(result30.getBody().size() - 1).getTime());
		kdjMap.put(code, kdjEntry);
	}

	/**
	 * 计算当前时间满足条件的代码
	 * @return
	 */
	public List<KDJEntry> rfreashCurrent() {
		if(!LOCK.isLocked()){
			return calcKdj();
		}else{
			throw new RuntimeException("当前正在执行定时任务");
		}
	}

//	下面这个配置只能实现Tue May 04 10:12:50 GMT 2021的效果
//	@Scheduled(cron = "50 0/4 9-11,13-15 ? * 1-5")
	@Scheduled(cron = "50 59 9 ? * 1-5")//	10点
	@Scheduled(cron = "50 29 10 ? * 1-5")//	10:30
	@Scheduled(cron = "50 59 10 ? * 1-5")//	11:00
	@Scheduled(cron = "50 29 11 ? * 1-5")//	11:30
	@Scheduled(cron = "50 29 13 ? * 1-5")//	13:30
	@Scheduled(cron = "50 59 13 ? * 1-5")//	14:00
	@Scheduled(cron = "50 29 14 ? * 1-5")//	14:30
	@Scheduled(cron = "50 59 14 ? * 1-5")//	15:00
	public void periodCalc(){
		LOCK.lock();
		try{
			periodKdjList = calcKdj();
			log(new Date().toString() + " finished 30m calc");
			EXECUTOR.submit(new SaveDataRunnable(periodKdjList,dbDataService));
		}finally{
			LOCK.unlock();
		}
	}
	
	/**
	 * 获取周期最后一次执行的结果
	 * @return
	 */
	public List<KDJEntry> getLastPeriod(){
		return this.periodKdjList;
	}

	private List<KDJEntry> calcKdj(){
		List<String> paramsList = buildUrlParams();
		List<Future<List<KDJEntry>>> futureList = new ArrayList<>();
		List<KDJEntry> resultList = new ArrayList<>();
		for(String param : paramsList){
			Future<List<KDJEntry>> future = EXECUTOR.submit(new FetchDataRunnable(param));
			futureList.add(future);
		}
		for(Future<List<KDJEntry>> future : futureList){
			try {
				resultList.addAll(future.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return resultList;
	}

	private List<String> buildUrlParams() {
		List<String> resultList = new ArrayList<>();
		int range = 100;
		int total = codeList.size();
		int index = 0;
		List<String> strList = new ArrayList<>(range);
		for (int i = 0; i < total; i++) {
			if (index == range) {
				index = 0;
				resultList.add(StringUtils.join(strList, ","));
				strList = new ArrayList<>(range);
			}
			strList.add(codeList.get(i));
			index++;
		}
		resultList.add(StringUtils.join(strList, ","));
		return resultList;
	}

	private String buildParam(String code, int length, int scale) {
		return "&symbol=" + code + "&scale=" + scale + "&datalen=" + length;
	}

	private List<String> getStockList() {
		this.codeList = new ArrayList<>();
		try (InputStream in = StockService.class.getClassLoader().getResourceAsStream("stockList");
				InputStreamReader isr = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(isr);) {
			String code = null;
			while ((code = br.readLine()) != null) {
				String originCode = code.split("\t")[0];
				codeList.add(originCode.startsWith("6") ? "sh"+originCode : "sz"+originCode);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return codeList;
	}

	private List<CandleEntry> build(List<Map> list, String format) {
		SimpleDateFormat sFormat = new SimpleDateFormat(format);
		List<CandleEntry> result = new ArrayList<>();
		for (Map map : list) {
			try {
				result.add(new CandleEntry(sFormat.parse(map.get("day").toString()),
						Double.parseDouble(map.get("high").toString()), Double.parseDouble(map.get("low").toString()),
						Double.parseDouble(map.get("open").toString()),
						Double.parseDouble(map.get("close").toString())));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public List<String> getErrorCode() {
		return errorCodeList;
	}

	public  String getScheduleLog() {
		return scheduleLog;
	}

	class FetchDataRunnable implements Callable<List<KDJEntry>> {
		private String param;
		
//		/** 精度 */
//		private DecimalFormat df = new DecimalFormat("#.00");
		
		public FetchDataRunnable(String param){
			this.param = param;
		}

		@Override
		public List<KDJEntry> call() throws Exception {
			ResponseEntity<String> result = restClient.getForEntity(CURRENT_DATA_URL + param, String.class);
			String[] codeDatas = result.getBody().split("\n");
			List<KDJEntry> resultList = new ArrayList<>();
			for (String codeData : codeDatas) {
				String[] datas = codeData.split(",");
				CandleEntry entry = new CandleEntry(new Date(), Double.parseDouble(datas[4]), Double.parseDouble(datas[5]),
						Double.parseDouble(datas[1]), Double.parseDouble(datas[3]));
				String code = datas[0].replace("var hq_str_", "").replaceAll("=.*", "");
				KDJEntry kdjEntry = kdjMap.get(code);
				// 更新当日、当周值
				kdjEntry.updateLast(kdjEntry.getDayList(), entry);
				Kdj.calc(kdjEntry.getDayList(), 9, 3, 3, 1);
				if (kdjEntry.isDayRaise()) {
					ResponseEntity<List> result30 = restClient.getForEntity(DATA_URL + buildParam(code, MIN_LENGTH, 30),
							List.class);
					List<CandleEntry> list30 = build((List<Map>) result30.getBody(), "yyyy-MM-dd HH:mm:ss");
					CandleEntry lastEntry = list30.get(MIN_LENGTH - 1);
					kdjEntry.update30Last(lastEntry.getTime(), list30);
					Kdj.calc(kdjEntry.getM30List(), 9, 3, 3, 1);
					if (kdjEntry.is30Raise()) {
						entry.setTime(lastEntry.getTime());
						kdjEntry.updateLast(kdjEntry.getWeekList(), entry);
						Kdj.calc(kdjEntry.getWeekList(), 9, 3, 3, 1);

						kdjEntry.updateLast(kdjEntry.getDayList(), entry);
						Kdj.calc(kdjEntry.getDayList(), 9, 3, 3, 1);
						resultList.add(kdjEntry);
					} 
				}
			}
			codeDatas = null;
			return resultList;
		}
	}
	
	class SaveDataRunnable implements Runnable{

		private static final String STOCK_DB_NAME = "pyspider";
		
		private static final String REMOTE_STOCK_DB_NAME = "txpyspider";

		private List<KDJEntry> entryList;
		
		private DBDataService<Map> dbDataService;
		
		public SaveDataRunnable(List<KDJEntry> entryList,DBDataService<Map> dbDataService){
			this.entryList = entryList;
			this.dbDataService = dbDataService;
		}
		
		@Override
		public void run() {
			if(!entryList.isEmpty()){
				Date periodDate = entryList.get(0).getLastDate();	
				Object[][] paramObjs = new Object[entryList.size()][3];
				for(int i = 0; i < entryList.size(); i++){
					Object[] obj = new Object[2];
					obj[0] = entryList.get(i).getLastDate();
					obj[1] = entryList.get(i).getCode();
					obj[2] = entryList.get(i).isWeekRaise();
					paramObjs[i] = obj;
				}
				try {
					dbDataService.update(STOCK_DB_NAME, "delete from stock_period_kdj where op_time = ?",periodDate);
					dbDataService.batchUpdate(STOCK_DB_NAME, "insert into stock_period_kdj(op_time,item_code,is_week) values (?,?,?)", paramObjs);
					dbDataService.update(REMOTE_STOCK_DB_NAME, "delete from stock_period_kdj where op_time = ?",periodDate);
					dbDataService.batchUpdate(REMOTE_STOCK_DB_NAME, "insert into stock_period_kdj(op_time,item_code,is_week) values (?,?,?)", paramObjs);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void log(String logInfo){
		this.scheduleLog += "\r\n" + logInfo;
	}
}
