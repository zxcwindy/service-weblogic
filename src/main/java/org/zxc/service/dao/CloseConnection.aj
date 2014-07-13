package org.zxc.service.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;

public aspect CloseConnection {
	
	private pointcut closeConnection (Connection conn):
		call(* org.apache.commons.dbutils.QueryRunner.* (Connection,..))
		&&args(conn,..);
	
	after(Connection conn) :closeConnection(conn){
		try {
			DbUtils.close(conn);
		} catch (SQLException e) {			
			e.printStackTrace();
		}
	}
}
