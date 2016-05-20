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
				if(rsmd.getColumnType(i+1) == Types.CLOB){
					Reader reader = null;
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
							result[i] = builder.toString();
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
				}else if(rsmd.getColumnType(i+1) == Types.DATE){
					try{
						if(rs.getDate(i+1) != null){
							result[i] = DATA_FORMAT.format(rs.getDate(i+1));
						}else{
							result[i] = null;
						}
					}catch(Exception e){
						result[i] = null;
					}					
				}else if(rsmd.getColumnType(i+1) == Types.TIMESTAMP){
					try{
						if(rs.getTimestamp(i+1) != null){
							result[i] = DATA_FORMAT.format(rs.getTimestamp(i+1));
						}else{
							result[i] = null;
						}
					}catch(Exception e){
						result[i] = null;
					}
					
				}else if(rsmd.getColumnType(i+1) == Types.SQLXML){
					SQLXML sqlXml = rs.getSQLXML(i+1);
					result[i] = sqlXml.getString();
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
}
