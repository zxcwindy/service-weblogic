package org.zxc.service.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataBaseUtil {
	private static final String DATABASE_REGEX=".*(db2|oracle|mysql|sql server|hive).*";
	
	public static String getDBProduct(Connection conn){
		DatabaseMetaData metaData = null;
		String result = "";
		try {
			metaData = conn.getMetaData();
			String driverName = metaData.getDriverName();
			
			Pattern pattern = Pattern.compile(DATABASE_REGEX);
			Matcher m = pattern.matcher(driverName.toLowerCase());
			if (m.matches()) {
				result = m.group(1).replace(" ", "");
			}			
		} catch (SQLException e) {
			e.printStackTrace();			
		}	
		return result;
	}
}
