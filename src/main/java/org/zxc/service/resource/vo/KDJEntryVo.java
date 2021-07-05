package org.zxc.service.resource.vo;

import java.io.Serializable;
import java.util.List;

public class KDJEntryVo  implements Serializable{

	private String code;
	
	private List<CandleEntryVo> m30;
	
	private List<CandleEntryVo> daym;
	
	private List<CandleEntryVo> weekm;

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
}