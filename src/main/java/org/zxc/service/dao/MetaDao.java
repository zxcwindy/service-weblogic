package org.zxc.service.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.zxc.service.domain.DBColumn;
import org.zxc.service.domain.DBTable;

@Repository("metaDao")
public class MetaDao {

	private static final Logger logger = Logger.getLogger(MetaDao.class);

	private static final int LIMIT_NUM = 15;
	
	@Autowired
	private Pool pool;

	public DBTable findTable(String dbName, String schema, String tableName) {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			conn = pool.getConnection(dbName);
			st = conn.createStatement();
			String sql = "";
			if (schema == null) {
				sql = "select * from " + tableName + " where 1 = 0";
			} else {
				sql = "select * from " + schema + "." + tableName
						+ " where 1 = 0";
			}
			logger.info(sql);
			rs = st.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			DBTable table = new DBTable();
			table.setTableName(tableName);
			table.setTableScheam(schema);
			table.setColsList(findCols(rsmd));

			return table;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.release(conn, st, rs);
		}
		return null;
	}

	public List<DBColumn> findCols(ResultSetMetaData rsmd) throws SQLException {

		List<DBColumn> list = new ArrayList<DBColumn>();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			DBColumn col = new DBColumn();
			col.setColName(rsmd.getColumnName(i));
			col.setColTypeName(rsmd.getColumnTypeName(i));
			col.setPrecision(rsmd.getPrecision(i));
			col.setScale(rsmd.getScale(i));
			list.add(col);
		}
		return list;
	}

	public List<String> findColNames(ResultSetMetaData rsmd) throws SQLException {

		List<String> list = new ArrayList<String>();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {		
			list.add(rsmd.getColumnName(i));
		}
		return list;
	}

	public List<DBTable> matchTable(String dbName, DBTable dbTable) {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		List<DBTable> dbTableList = new ArrayList<DBTable>();
		try {
			conn = pool.getConnection(dbName);
			DatabaseMetaData database = conn.getMetaData();
			rs = database.getTables(null, dbTable.getTableScheam(),
					dbTable.getTableName() + "%", new String[] { "TABLE",
							"VIEW" });
			while (rs.next()) {
				DBTable tempTable = new DBTable();
				tempTable.setTableScheam(rs.getString(1) == null ? rs
						.getString(2) : rs.getString(1));
				tempTable.setTableName(rs.getString(3));
				tempTable.setTableType(rs.getString(4));
				dbTableList.add(tempTable);
				if (dbTableList.size() > LIMIT_NUM) {
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				DbUtils.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return dbTableList;
	}
}
