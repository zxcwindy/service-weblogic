package org.zxc.service.datasource;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.Period;

public interface DataFetcher {

	/**
	 * @param period
	 * @param code sh600000 | sz300300
	 * @param startDate yyyy-MM-dd
	 * @param endDate yyyy-MM-dd
	 * @return
	 */
	public  List<CandleEntry> fetchData(Period period, String code ,String startDate, String endDate) ;
	
	public SourceEnum getCode();
	
	public void login();
	
	public void logout();
	
	public static  String nvlNum(String num){
		return StringUtils.isEmpty(num)? "0" : num;
	}
	
	public static  Double nvlNum(double num){
		return  num;
	}
}
