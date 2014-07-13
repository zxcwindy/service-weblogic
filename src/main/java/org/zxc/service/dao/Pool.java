package org.zxc.service.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public interface Pool {

	public abstract void release(Connection conn, Statement st, ResultSet rs);

	public abstract Connection getConnection(String dbName) throws SQLException;

}
