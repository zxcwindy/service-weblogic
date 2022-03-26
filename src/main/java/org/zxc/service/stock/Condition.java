package org.zxc.service.stock;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 *查询条件
 * @author david
 * 2022年3月13日
 */
public class Condition implements Serializable{

	/**
	 * 周期
	 */
	private Period period;
	
	/**
	 * 运算符，> | < | = | >= | <=
	 */
	private String operator;
	
	/**
	 * 执行运算符对比的属性
	 */
	private String field;
	
	/**
	 * 条件值，为double类型或者字符串，double类型为值，字符串为其他做为对比的属性
	 */
	private String value;

	public Period getPeriod() {
		return period;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}		
}
