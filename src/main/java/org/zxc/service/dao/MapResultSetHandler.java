package org.zxc.service.dao;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("rsh")
public class MapResultSetHandler implements ResultSetHandler<Map> {
	
	private static final DateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 存放特定字段type类型和对应的数据处理接口，包含CLOB,DATE,TIMESTAMP,SQLXML
	 */
	private static final Map<Integer,DbValueDealer> DB_VALUE_DEALER_MAP = new HashMap<Integer,DbValueDealer>();
	static{
		//CLOB类型处理
		DB_VALUE_DEALER_MAP.put(Types.CLOB, new DbValueDealer(){
			@Override
			public Object executer(ResultSet rs, int i) throws SQLException {
				Reader reader = null;
				Object resultObj = null;
				try {
					Clob clob = rs.getClob(i+1);
					if(clob != null){
						reader = clob.getCharacterStream();						
						char[] chars = new char[2048];
						StringBuilder builder = new StringBuilder();
						int a = 0;
						while((a = reader.read(chars)) != -1){
							builder.append(chars);
						}
						resultObj= builder.toString();
					}						
				} catch (IOException e) {
					e.printStackTrace();
				} finally{
					if(reader != null){
						try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				return resultObj;
			}
		});
		//Date类型处理
		DB_VALUE_DEALER_MAP.put(Types.DATE, new DbValueDealer(){
			@Override
			public Object executer(ResultSet rs, int i)
					throws SQLException {
				Object resultObj = null;
				try{
					if(rs.getDate(i+1) != null){
						resultObj = DATA_FORMAT.format(rs.getDate(i+1));
					}else{
						resultObj = null;
					}
				}catch(Exception e){
					resultObj = null;
				}					
				return resultObj;
			}
		});
		//TIMESTAMP类型处理
		DB_VALUE_DEALER_MAP.put(Types.TIMESTAMP, new DbValueDealer(){
			@Override
			public Object executer(ResultSet rs, int i)
					throws SQLException {
				Object resultObj = null;
				try{
					if(rs.getTimestamp(i+1) != null){
						resultObj = DATA_FORMAT.format(rs.getTimestamp(i+1));
					}else{
						resultObj = null;
					}
				}catch(Exception e){
					resultObj = null;
				}
				return resultObj;
			}
		});
		//SQLXML处理
		DB_VALUE_DEALER_MAP.put(Types.SQLXML, new DbValueDealer(){
			@Override
			public Object executer(ResultSet rs, int i)
					throws SQLException {
				Object resultObj = null;
				SQLXML sqlXml = rs.getSQLXML(i+1);
				resultObj = sqlXml.getString();
				return resultObj;
			}
			
		});
	}

	@Autowired
	private MetaDao metaDao;

	@Override
	public Map<String, List> handle(ResultSet rs) throws SQLException {
		Map<String, List> map = new HashMap<String, List>();
		ResultSetMetaData rsmd = rs.getMetaData();
		
		int cols = rsmd.getColumnCount();
		List<Object[]> dataList = new ArrayList<Object[]>();
		while (rs.next()) {
			Object[] result = new Object[cols];
			for (int i = 0; i < cols; i++) {			
				DbValueDealer dealer = DB_VALUE_DEALER_MAP.get(rsmd.getColumnType(i+1) ); 
				if( dealer != null){
					result[i]  = dealer.executer(rs, i);
				}else{
					result[i] = rs.getObject(i + 1);
				}		
			}
			dataList.add(result);
		}

		map.put("metadata", metaDao.findColNames(rsmd));
		map.put("data", dataList);
		return map;
	}
	
	/**
	 * 根据不同的字段类型进行不同的处理
	 * @author david
	 *
	 */
	interface DbValueDealer{
		/**
		 * 根据不同的类型处理指定列的数据
		 * @param rs
		 * @param colIndex
		 * @return 处理后的结果
		 */
		Object executer(ResultSet rs,int colIndex) throws SQLException;
	}
}
