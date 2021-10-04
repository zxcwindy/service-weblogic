package org.zxc.service.resource.vo;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class KDJEntryVo  implements Serializable{

	private String code;
	
	private List<CandleEntryVo> m30;
	
	private List<CandleEntryVo> daym;
	
	private List<CandleEntryVo> weekm;
	
	private List<CandleEntryVo> monthm;
	
	public KDJEntryVo(){}
	
	public KDJEntryVo(String code){
		this.code = code;
	}
	
	@JsonIgnore
	private int type30;
	
	@JsonIgnore
	private int typeDay;
	
	@JsonIgnore
	private int typeWeek;
	
	@JsonIgnore
	private int typeMonth;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<CandleEntryVo> getM30() {
		return m30;
	}

	public void setM30(List<CandleEntryVo> m30) {
		this.m30 = m30;
	}

	public List<CandleEntryVo> getDaym() {
		return daym;
	}

	public void setDaym(List<CandleEntryVo> daym) {
		this.daym = daym;
	}

	public List<CandleEntryVo> getWeekm() {
		return weekm;
	}

	public void setWeekm(List<CandleEntryVo> weekm) {
		this.weekm = weekm;
	}

	public int getType30() {
		return type30;
	}

	public int getTypeDay() {
		return typeDay;
	}

	public int getTypeWeek() {
		return typeWeek;
	}

	public List<CandleEntryVo> getMonthm() {
		return monthm;
	}

	public void setMonthm(List<CandleEntryVo> monthm) {
		this.monthm = monthm;
	}

	public int getTypeMonth() {
		return typeMonth;
	}

	public void setTypeMonth(int typeMonth) {
		this.typeMonth = typeMonth;
	}

	public void setType30(int type30) {
		this.type30 = type30;
	}

	public void setTypeDay(int typeDay) {
		this.typeDay = typeDay;
	}

	public void setTypeWeek(int typeWeek) {
		this.typeWeek = typeWeek;
	}
	
	public double getScore(){
		return last(this.getM30()).getM() + last(this.getDaym()).getM()*10 + last(this.getWeekm()).getM()*100 + last(this.getMonthm()).getM()*1000;
	}
	
	private CandleEntryVo last(List<CandleEntryVo> list){
		if(list != null && list.size()> 0){
			return list.get(list.size() - 1);
		}else{
			return new CandleEntryVo();
		}
		 
	}
}