package org.zxc.service.stock;

public enum Period {

	M30("30"),Day("d"),Week("w"),Month("m");
	
	private String type;
	
	private Period(String type){
		this.type = type;
	}
	
	@Override
	public String toString(){
		return this.type;
	}
}
