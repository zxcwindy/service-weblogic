package org.zxc.service.stock;

public class Constans {

	/**
	 * 周期数据接口
	 */
	public static final String DATA_URL = "http://quotes.sina.cn/cn/api/json_v2.php/CN_MarketDataService.getKLineData?ma=no";

	/**
	 * 当前数据接口
	 */
	public static final String CURRENT_DATA_URL = "http://hq.sinajs.cn/list=";

	/**
	 * 数据获取最长周期
	 */
	public static final int MAX_LENGTH = 60;

	/**
	 * 30分钟周期
	 */
	public static final int MIN_LENGTH = 8;
	
	/**
	 * 盘后周期数据接口
	 */
	public static final String BAO_DATA_URL = "http://127.0.0.1:5551/baostock/";
	
	/**
	 * 数据库
	 */
	public static final String STOCK_DB_NAME = "pyspider";
}
