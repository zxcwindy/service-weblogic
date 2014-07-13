package org.zxc.service.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.stereotype.Component;

/**
 * @author zhengxc@asiainfo-linkage.com 2013-9-9
 *         Class.forName("org.logicalcobwebs.proxool.ProxoolDriver"); Properties
 *         info = new Properties();
 *         info.setProperty("proxool.maximum-connection-count", "10");
 *         info.setProperty("proxool.house-keeping-test-sql",
 *         "select CURRENT_DATE"); info.setProperty("user", "sa");
 *         info.setProperty("password", ""); String alias = "test"; String
 *         driverClass = "org.hsqldb.jdbcDriver"; String driverUrl =
 *         "jdbc:hsqldb:test"; String url = "proxool." + alias + ":" +
 *         driverClass + ":" + driverUrl;
 *         ProxoolFacade.registerConnectionPool(url, info);
 */
@Component("pool")
public class ProxoolPool implements Pool{			

	public Connection getConnection(String dbName) throws SQLException {		
		return DriverManager.getConnection("proxool."+dbName);		
	}

	
	public void release(Connection conn, Statement st, ResultSet rs) {
		try {
			DbUtils.close(rs);
			DbUtils.close(st);
			DbUtils.close(conn);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
