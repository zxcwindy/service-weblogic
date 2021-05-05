package org.zxc.service.service;

import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.SQLException;

import org.zxc.service.util.DataBaseUtil;
import org.zxc.service.util.TemplateUtil;

public aspect RunSqlAspect {
		
	pointcut querySqlPointcut(Connection conn, String sql,int limit):
				call(* org.zxc.service.service.DBDataServiceImpl.query(Connection,String,int))
				&&args(conn,sql,limit);
	
	pointcut updateSqlPointcut(String dbName,String sql):
		call(* org.zxc.service.service.DBDataService.update(String,String))
		&&args(dbName,sql);
 
	Object around(Connection conn, String sql,int limit): querySqlPointcut(conn,sql,limit) {
		Map<String,Object> dataMap = new HashMap<String,Object>();
		try {					
			dataMap.put("querySql", sql);
			dataMap.put("limit", limit);	
			sql = TemplateUtil.getSql(dataMap, DataBaseUtil.getDBProduct(conn) + "-head");
			return proceed(conn,sql,limit);
		} catch (SQLException e) {			
			dataMap.put("errorMsg", e.getMessage());		
		}		
		return dataMap;
	}
	
	Object around(String dbName,String sql): updateSqlPointcut(dbName,sql) {
		Map<String,Object> dataMap = new HashMap<String,Object>();
		try {								
			return proceed(dbName,sql);
		} catch (SQLException e) {
			dataMap.put("errorMsg", e.getMessage());		
		}		
		return dataMap;
	}
	
}
