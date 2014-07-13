package org.zxc.service.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.PropertyConfigurator;

public class DBUtil implements Pool{

	private static Context context;

	private static DataSource mysql;

	private static DataSource db2;

	private static final Logger logger = Logger.getLogger(DBUtil.class);

	static {
		 try {
		 context = new InitialContext();
		 } catch (NamingException e1) {
		 logger.error(e1);
		 }
		
		 try {
		 mysql = (DataSource) context.lookup("jdbc/mysql");
		 } catch (NamingException e1) {
		 logger.warn(e1.getMessage());
		 }
		
		 try {
		 db2 = (DataSource) context.lookup("jdbc/db2");
		 } catch (NamingException e) {
		 logger.warn(e.getMessage());
		 }		
	}

	public Connection getConnection(String dbName) throws SQLException {
		DataSource ds = null;
		if ("mysql".equals(dbName)) {
			ds = mysql;
		} else {
			ds = db2;
		}
		return ds.getConnection();
	}

	public void release(Connection conn, Statement st, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}
}
