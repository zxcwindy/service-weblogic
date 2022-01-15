package org.zxc.service.stock;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zxc.service.provider.CandleArrayJsonSerializer;
import org.zxc.service.provider.CustomerDoubleFESerialize;
import org.zxc.service.provider.CustomerDoubleFlagSerialize;
import org.zxc.service.provider.CustomerDoubleSerialize;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 *
 * @author david
 * 2022年1月1日
 */
@JsonSerialize(using = CandleArrayJsonSerializer.class)
public class CandleEntry implements Serializable {

	/**
	 * 时间
	 */
//	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date time;

	/** shadow-high value */
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double mShadowHigh = 0f;

	/** shadow-low value */
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double mShadowLow = 0f;

	/** close value */
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double mClose = 0f;

	/** open value */
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double mOpen = 0f;
	/**
	 * 成交量
	 */
//	@JsonSerialize(using = CustomerDoubleFESerialize.class)
	private double volume =0f;

	/**
	 * 成交额
	 */
//	@JsonSerialize(using = CustomerDoubleFESerialize.class)
	private double amount = 0f;

	/**
	 * 换手率
	 */
	private double turn = 0f;

	/**
	 * 涨跌幅
	 */
	private double pctChg = 0f;

	private double y = 0f;

	// KDJ 指标的三个属性
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double k;
	
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double d;
	
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double j;
	
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double m;

	// boll指标三个属性
//	@JsonSerialize(using = CustomerDoubleFlagSerialize.class)
	private double mb;
	
//	@JsonSerialize(using = CustomerDoubleFlagSerialize.class)
	private double up;
	
//	@JsonSerialize(using = CustomerDoubleFlagSerialize.class)
	private double dn;

//	5日、10日、20日、30日、60日日均线
//	@JsonSerialize(using = CustomerDoubleFlagSerialize.class)
	private double ma5;
	
//	@JsonSerialize(using = CustomerDoubleFlagSerialize.class)
	private double ma10;
	
//	@JsonSerialize(using = CustomerDoubleFlagSerialize.class)
	private double ma20;
	
//	@JsonSerialize(using = CustomerDoubleFlagSerialize.class)
	private double ma30;
	
//	@JsonSerialize(using = CustomerDoubleFlagSerialize.class)
	private double ma60;

//	ema
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double ema;

//	macd
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double dif;
	
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double dea;
	
//	@JsonSerialize(using = CustomerDoubleSerialize.class)
	private double macd;


//	private SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
    public CandleEntry(Date time,double shadowH, double shadowL, double open, double close,double volume,double amount ,double turn, double pctChg) {
		this.time = time;
		this.mShadowHigh = shadowH;
		this.mShadowLow = shadowL;
		this.mOpen = open;
		this.mClose = close;
		this.volume = volume;
		this.amount = amount;
		this.turn = turn;
		this.pctChg = pctChg;
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
	@JsonSerialize(using = CustomerDoubleSerialize.class)
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
	@JsonSerialize(using = CustomerDoubleSerialize.class)
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
	@JsonSerialize(using = CustomerDoubleSerialize.class)
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
	@JsonSerialize(using = CustomerDoubleSerialize.class)
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

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getTurn() {
		return turn;
	}

	public void setTurn(double turn) {
		this.turn = turn;
	}

	public double getPctChg() {
		return pctChg;
	}

	public void setPctChg(double pctChg) {
		this.pctChg = pctChg;
	}

	public double getMb() {
		return mb;
	}

	public void setMb(double mb) {
		this.mb = mb;
	}

	public double getUp() {
		return up;
	}

	public void setUp(double up) {
		this.up = up;
	}

	public double getDn() {
		return dn;
	}

	public void setDn(double dn) {
		this.dn = dn;
	}

	public double getMa5() {
		return ma5;
	}

	public void setMa5(double ma5) {
		this.ma5 = ma5;
	}
	
	public double getMa10() {
		return ma10;
	}

	public void setMa10(double ma10) {
		this.ma10 = ma10;
	}

	public double getMa20() {
		return ma20;
	}

	public void setMa20(double ma20) {
		this.ma20 = ma20;
	}

	public double getMa30() {
		return ma30;
	}

	public void setMa30(double ma30) {
		this.ma30 = ma30;
	}

	public double getMa60() {
		return ma60;
	}

	public double getEma() {
		return ema;
	}

	public void setEma(double ema) {
		this.ema = ema;
	}

	public double getDif() {
		return dif;
	}

	public void setDif(double dif) {
		this.dif = dif;
	}

	public double getDea() {
		return dea;
	}

	public void setDea(double dea) {
		this.dea = dea;
	}

	public double getMacd() {
		return macd;
	}

	public void setMacd(double macd) {
		this.macd = macd;
	}

	public void setMa60(double ma60) {
		this.ma60 = ma60;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	@Override
	  public String toString() {
	    return "CandleEntry{" +
		"open=" + getOpen() +
		", high=" + getHigh() +
		", low=" + getLow() +
		", close=" + getClose()+
		", time=" + getTime() +
		", k=" + getK() +
		", d=" + getD() +
		", j=" + getJ() +
		'}';
	  }
}
