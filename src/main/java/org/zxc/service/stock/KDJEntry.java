package org.zxc.service.stock;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class KDJEntry implements Serializable{

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date lastDate;
	
	private String code;

	@JsonIgnore
	private List<CandleEntry> m30List = new LinkedList<>();

	@JsonIgnore
	private List<CandleEntry> dayList = new LinkedList<>();

	@JsonIgnore
	private List<CandleEntry> weekList = new LinkedList<>();
	
	@JsonIgnore
	private List<CandleEntry> monthList = new LinkedList<>();
	
	private int type30;
	
	private int typeDay;
	
	private int typeWeek;
	
	private int typeMonth;

	/**
	 * 周期
	 */
	private int period = 16;
	
	public KDJEntry(String code){
		this.code = code;
	}

	public Date getLastDate() {
		return lastDate;
	}

	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}

	public int getPeriod() {
		return period;
	}

	public List<CandleEntry> getM30List() {
		return m30List;
	}

	public void setM30List(List<CandleEntry> m30List) {
		this.m30List = m30List;
	}

	public List<CandleEntry> getDayList() {
		return dayList;
	}

	public void setDayList(List<CandleEntry> dayList) {
		this.dayList = dayList;
	}

	public List<CandleEntry> getWeekList() {
		return weekList;
	}

	public void setWeekList(List<CandleEntry> weekList) {
		this.weekList = weekList;
	}
	
	public List<CandleEntry> getMonthList() {
		return monthList;
	}

	public void setMonthList(List<CandleEntry> monthList) {
		this.monthList = monthList;
	}

	public int getType30() {
		type30 = raiseType(getM30List());
		return type30;
	}

	public int getTypeDay() {
		typeDay = raiseType(getDayList());
		return typeDay;
	}

	public int getTypeWeek() {
		typeWeek =  weekRaiseType(getWeekList());
		return typeWeek;
	}
	
	public int getTypeMonth() {
		typeMonth = weekRaiseType(getMonthList());
		return typeMonth;
	}

	/**
	 * 根据30分钟周期更新30、日、周数据
	 * 
	 * @param currentTime
	 * @param m30List
	 */
	public void update30Last(Date date, List<CandleEntry> m30List) {
		long currentTime = date.getTime();
		long lastTime = getLastDate().getTime();
		int index = m30List.size() - 1;
		// 时间更新
		if (currentTime > lastTime) {
			long periodTime = (currentTime - lastTime) / (1000 * 60 * 30);
			// 上个交易日收盘时间和当前交易日开盘时间
			if (periodTime > period && isBegin(date) && isEnd(lastDate)) {
				updatePeriod30(1, m30List);
			} else {
				updatePeriod30(periodTime, m30List);
			}
		}
		// 更新最后一个数据
//		updateLast(getM30List(), m30List.get(index));
//		updateLast(getDayList(), m30List.get(index));
//		updateLast(getWeekList(), m30List.get(index));
		setLastDate(date);
	}

	private void updatePeriod30(long periodTime, List<CandleEntry> m30List) {
		int size = m30List.size();
		for (int i = (int) (size - periodTime); i < size; i++) {
			if(this.getM30List().size() > 0){
				this.getM30List().remove(0);
			}
			this.getM30List().add(m30List.get(i));
		}
	}

	public void updateLast(List<CandleEntry> list, CandleEntry entry) {
		int index = list.size() - 1;
		list.set(index, entry);
	}
	
	public String getCode() {
		return code;
	}

	/**
	 * 是否开盘时间
	 * 
	 * @param date
	 * @return
	 */
	private boolean isBegin(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY) == 10;
	}

	/**
	 * 是否收盘时间
	 * 
	 * @param date
	 * @return
	 */
	private boolean isEnd(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY) == 15;
	}

	/**
	 * 规则一比规则二更为严格
	 * @param list
	 * @return 0: 没有交叉;1：第一种规则交叉；2：第二种规则交叉
	 */
	private int raiseType(List<CandleEntry> list) {
		try{
			int index = list.size() - 1;
			CandleEntry lastEntry = list.get(index);
			CandleEntry lastEntry1 = list.get(index - 1);
			CandleEntry lastEntry2 = list.get(index - 2);
			
			if (lastEntry.getM() > -5 && lastEntry.getM() < 0 && lastEntry.getM() > lastEntry1.getM()
					&& lastEntry1.getM() > lastEntry2.getM()){
				return 1;
			}else if(lastEntry.getM() > -5 && lastEntry.getM() < 0 && lastEntry1.getM() > -7.5 &&  lastEntry1.getM() < 0
							&& lastEntry2.getM() > -7.5 && lastEntry2.getM() < 0){
				return 2;
			}else{
				return 0;
			}
		}catch(Exception e){
			System.out.println(this.code);
			return 0;
		}
	}
	
	private int weekRaiseType(List<CandleEntry> list) {
		try{
			int index = list.size() - 1;
			CandleEntry lastEntry = list.get(index);
			CandleEntry lastEntry1 = list.get(index - 1);
			CandleEntry lastEntry2 = list.get(index - 2);
			
			if(lastEntry.getM() > -5 && lastEntry.getM() < 0 && lastEntry.getM() > lastEntry1.getM()
					&& lastEntry1.getM() > lastEntry2.getM()){
				return 1;
			}else{
				return 0;
			}
		}catch(Exception e){
			System.out.println(this.code);
			return 0;
		}
	}
}
