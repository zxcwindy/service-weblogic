package org.zxc.service.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.zxc.service.dao.Pool;
import org.zxc.service.util.DataBaseUtil;
import org.zxc.service.util.TemplateUtil;

@Service("dbDataService")
public class DBDataServiceImpl implements DBDataService<Map> {

	@Autowired
	private QueryRunner runner;
	
	@Autowired
	private Pool pool;
	
	@Autowired
	@Qualifier("rsh")
	private ResultSetHandler<Map> rsh;
	
	@Autowired
	@Qualifier("strListRsh")
	private ResultSetHandler<Map<String,List<String>>> strListRsh;	
	
	@Override
	public Map update(String dbName, String sql) throws SQLException {
		return getResultMap(runner.update(pool.getConnection(dbName), sql));
	}

	@Override
	public Map update(String dbName,String sql, Object... params) throws SQLException {
		return getResultMap(runner.update(pool.getConnection(dbName), sql, params));
	}

	@Override
	public Map query(String dbName,String sql) throws SQLException {
		Connection conn = pool.getConnection(dbName);
		return query(conn, sql);
	}

	@Override
	public Map query(String dbName, String sql, Object... params) throws SQLException {
		return runner.query(pool.getConnection(dbName), sql, rsh, params);
	}	
	
	public Map queryTableNames(String dbName,Map<String,Object> dataMap) throws SQLException {
		Connection conn = pool.getConnection(dbName);
		String dbProduc = DataBaseUtil.getDBProduct(conn);
		String sql = TemplateUtil.getSql(dataMap, dbProduc + "-all-tables");
		return runner.query(conn, sql,strListRsh);
	}

	private Map query(Connection conn, String sql) throws SQLException {
		return runner.query(conn, sql, rsh);		
	}
	
	private Map getResultMap(int num){
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("result", num);
		return result;
	}
	
	@Override
	public Map update(String dbName, List<String> sqlList) {
		Map<String,Object> result = new HashMap<String,Object>();		
		int[] resultNums = new int[sqlList.size()];
		for(int i = 0; i < resultNums.length; i++){
			try {
				resultNums[i] = runner.update(pool.getConnection(dbName),sqlList.get(i));
			} catch (SQLException e) {
				Map<String,Object> errorMap = new HashMap<String,Object>();
				errorMap.put("success", Arrays.copyOf(resultNums, i));
				errorMap.put("error-sql", " execute error  ->" +sqlList.get(i));
				errorMap.put("error-result", e.getMessage() );
				result.put("result", errorMap);
				return result;
			}
		}
		result.put("result", resultNums);
		return result;
	}
}
