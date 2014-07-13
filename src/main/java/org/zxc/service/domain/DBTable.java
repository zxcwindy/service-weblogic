package org.zxc.service.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;


@XmlRootElement
public class DBTable {

	private String tableName;
	
	private String tableDesc;
	
	private String tableScheam;
	
	private String tableType = "TABLE";
	
	private List<DBColumn> colsList = new ArrayList<DBColumn>();

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableDesc() {
		return tableDesc;
	}

	public void setTableDesc(String tableDesc) {
		this.tableDesc = tableDesc;
	}

	public String getTableScheam() {
		return tableScheam;
	}

	public void setTableScheam(String tableScheam) {
		this.tableScheam = tableScheam;
	}	

	public String getTableType() {
		return tableType;
	}

	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	public List<DBColumn> getColsList() {
		return colsList;
	}

	public void setColsList(List<DBColumn> colsList) {
		this.colsList = colsList;
	}
	
	public void addColumn(DBColumn col){
		colsList.add(col);
	}
	
	public String descTable(){
		StringBuilder sb = new StringBuilder();
		for(DBColumn column : colsList){
			sb.append(column.descColumn() + "\n");
		}
		return sb.toString();
	}
	
	@JsonIgnore
	public String getCreateTableSql(){
		StringBuilder sb = new StringBuilder();
		String schema = getTableScheam() == null?"":getTableScheam()+".";
		sb.append("create table " + schema + this.getTableName() + "(\n");
		for(DBColumn column : colsList){
			sb.append(column.descColumn() + ",\n");
		}		
		return sb.subSequence(0, sb.length()-2) + ")";
	}
	
	@JsonIgnore
	public String getColumnNames(){
		StringBuilder sb = new StringBuilder();
		for(DBColumn column : colsList){
			sb.append(column.getColName() + ",");
		}
		
		if(sb.length() > 0){
			sb.deleteCharAt(sb.length()-1);
		}
		
		return sb.toString();
	}
	
	@JsonIgnore
	public String getSelectSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		for(DBColumn column : colsList){
			sb.append(column.getColName() + ",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(" from " + (getTableScheam() != null? getTableScheam()+"."+getTableName() : getTableName()));
		return sb.toString();
	}
		
}
