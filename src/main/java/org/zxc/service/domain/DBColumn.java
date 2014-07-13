package org.zxc.service.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DBColumn {

	private String colName;
	
	private String colTypeName;
	
	private int precision;
	
	private int scale;

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public String getColTypeName() {
		return colTypeName;
	}

	public void setColTypeName(String colTypeName) {
		this.colTypeName = colTypeName;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}
	
	public String descColumn(){
		if(getPrecision() < 1 
				|| getColTypeName().toLowerCase().contains("date") 
				|| getColTypeName().toLowerCase().contains("timestamp")
				|| getColTypeName().toLowerCase().contains("int")){
			return this.getColName() + "	" + this.getColTypeName() ;
		}		
		String scale = getScale() > 0 ? "," + getScale() : "";
		return this.getColName() + "	" + this.getColTypeName() + "(" + this.getPrecision() + scale + ")";
	}
}
