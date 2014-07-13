package org.zxc.service.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.zxc.service.domain.DBTable;

public interface DBMetaService {

	public DBTable findTable(String dbName, DBTable dbTable);
	
	public List<String> findTableNames(String dbName, String table) throws SQLException;

	public DBTable findTable();	

}