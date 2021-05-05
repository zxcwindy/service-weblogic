package org.zxc.service.stock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class CandleEntry {
	
	/**
	 * 时间
	 */
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date time;
	
//	/** 精度 */
//	private DecimalFormat df = new DecimalFormat("#.00");
	
	/** shadow-high value */
	private double mShadowHigh = 0f;

	/** shadow-low value */
	private double mShadowLow = 0f;

	/** close value */
	private double mClose = 0f;

	/** open value */
	private double mOpen = 0f;

	private double y = 0f;

	// KDJ 指标的三个属性
	private double k;
	private double d;
	private double j;
	private double m;
	
	private SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public CandleEntry() {
	}

	public CandleEntry(double y) {
		this.y = y;
	}

	/**
	 * Constructor.
	 * 
	 * @param x
	 *            The value on the x-axis
	 * @param shadowH
	 *            The (shadow) high value
	 * @param shadowL
	 *            The (shadow) low value
	 * @param open
	 *            The open value
	 * @param close
	 *            The close value
	 */
	public CandleEntry(Date time,double shadowH, double shadowL, double open, double close) {
		this.time = time;
		this.mShadowHigh = shadowH;
		this.mShadowLow = shadowL;
		this.mOpen = open;
		this.mClose = close;
	}
	
	public CandleEntry(String time,double shadowH, double shadowL, double open, double close) {
		this.setTime(time);
		this.mShadowHigh = shadowH;
		this.mShadowLow = shadowL;
		this.mOpen = open;
		this.mClose = close;
	}

	/**
	 * Returns the overall range (difference) between shadow-high and
	 * shadow-low.
	 * 
	 * @return
	 */
	@JsonIgnore
	public double getShadowRange() {
		return Math.abs(mShadowHigh - mShadowLow);
	}

	/**
	 * Returns the body size (difference between open and close).
	 * 
	 * @return
	 */
	@JsonIgnore
	public double getBodyRange() {
		return Math.abs(mOpen - mClose);
	}

	/**
	 * Returns the upper shadows highest value.
	 * 
	 * @return
	 */
	public double getHigh() {
		return mShadowHigh;
	}

	public void setHigh(double mShadowHigh) {
		this.mShadowHigh = mShadowHigh;
	}

	/**
	 * Returns the lower shadows lowest value.
	 * 
	 * @return
	 */
	public double getLow() {
		return mShadowLow;
	}

	public void setLow(double mShadowLow) {
		this.mShadowLow = mShadowLow;
	}

	/**
	 * Returns the bodys close value.
	 * 
	 * @return
	 */
	public double getClose() {
		return mClose;
	}

	public void setClose(double mClose) {
		this.mClose = mClose;
	}

	/**
	 * Returns the bodys open value.
	 * 
	 * @return
	 */
	public double getOpen() {
		return mOpen;
	}

	public void setOpen(double mOpen) {
		this.mOpen = mOpen;
	}

	public double getY() {
		return this.y;
	}

	public double getK() {
		return k;
	}

	public void setK(double k) {
//		this.k = Double.parseDouble(df.format(k));
		this.k = k;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
//		this.d = Double.parseDouble(df.format(d));
		this.d = d;
	}

	public double getJ() {
		return j;
	}

	public void setJ(double j) {
//		this.j = Double.parseDouble(df.format(j));
		this.j = j;
	}
	 public double getM() {
//		return Double.parseDouble(df.format(this.getK() - getD()));
		return this.getK() - getD();
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
	
	public void setTime(String time) {
		try {
			this.time = sFormat.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	  public String toString() {
	    return "CandleEntry{" +
	        "open=" + getOpen() +
	        ", high=" + getHigh() +
	        ", low=" + getLow() +
	        ", close=" + getClose()+
	        ", time=" + sFormat.format(getTime()) +
	        ", k=" + getK() +
	        ", d=" + getD() +
	        ", j=" + getJ() +
	        '}';
	  }
}
