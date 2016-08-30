package org.zxc.service.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DBDataService<T> {

	T update(String dbName, String sql) throws SQLException;

	T update(String dbName,String sql, Object... params) throws SQLException;
	
	T query(String dbName,String sql) throws SQLException;
	          
	T query(String dbName,String sql, Object... params) throws SQLException;
	
	T queryTableNames(String dbName, Map<String,Object> dataMap) throws SQLException;
	
	T update(String dbName, List<String> sqlList) throws SQLException;
}
