package org.zxc.service.stock;

public enum Period {

	M1("1"),M5("5"),M15("15"),M30("30"),M60("60"),M120("120"),Day("d"),Week("w"),Month("m");
	
	private String type;
	
	private Period(String type){
		this.type = type;
	}
	
	@Override
	public String toString(){
		return this.type;
	}
}
