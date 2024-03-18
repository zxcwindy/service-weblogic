package org.zxc.service.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 存放kdj趋势数据
 * @author david
 * 2021年10月3日
 */
public class KdjTrend {

	private String itemCode;
	
	private String itemName;
	
	/**
	 * 类型：30,d,w,m
	 */
	private String type;
	
	private List<KdjItem> itemList = new ArrayList<>();
	
	public KdjTrend(){}
	

	public KdjTrend(String itemCode, String itemName,String type) {
		this.itemCode = itemCode;
		this.itemName = itemName;
		this.type = type;
	}


	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public List<KdjItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<KdjItem> itemList) {
		this.itemList = itemList;
	} 
	
	public void addKdjItem(KdjItem kdjItem){
		this.itemList.add(kdjItem);
	}
}
