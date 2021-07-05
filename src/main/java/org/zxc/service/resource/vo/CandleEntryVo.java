package org.zxc.service.resource.vo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class CandleEntryVo implements Serializable{
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date time;
	
	private double m;
	
	public CandleEntryVo(Date time, double m) {
		super();
		this.time = time;
		this.m = m;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public double getM() {
		return m;
	}

	public void setM(double m) {
		this.m = m;
	}
}
