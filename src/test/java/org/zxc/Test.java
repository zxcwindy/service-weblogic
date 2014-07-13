import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Test {

	public static void main(String[] args) {
//		String ip = args[0];
//
//		String username = "u_test";
//		String password = "u_test";
//		String sql_ddl_1 = "drop table if exists s_temp.test_temptable;";
//		String sql_ddl_2 = "create dimension table s_temp.test_temptable(a int,b float);";
//		String sql_dml_1 = "insert into test_temptable values(1,1.0);";
//		String sql_dml_2 = "insert into test_temptable values(2,2.0);";
//		String sql_dml_3 = "insert into test_temptable values(1,4.0);";
//		String sql_ddl_3 = "drop table if exists aggtable;";
//		String sql_ddl_4 = "create dimension table aggtable as select a,sum(b) as sum_b from test_temptable group by 1;";
//		String url = "jdbc:ncluster://" + ip + ":2406/db_zj";
//		Connection conn = null;
//		try {
//			Class.forName("com.asterdata.ncluster.Driver");
//		} catch (java.lang.ClassNotFoundException e) {
//			System.err.print("ClassNotFoundException: ");
//			System.err.println(e.getMessage());
//		}
//
//		try {
//			conn = DriverManager.getConnection(url, username, password);
//			Statement stmt = conn.createStatement();
//			stmt.executeUpdate(sql_ddl_1);
//			System.out.println(sql_ddl_1);
//			stmt.executeUpdate(sql_ddl_2);
//			System.out.println(sql_ddl_2);
//			stmt.executeUpdate(sql_dml_1);
//			System.out.println(sql_dml_1);
//			stmt.executeUpdate(sql_dml_2);
//			System.out.println(sql_dml_2);
//			stmt.executeUpdate(sql_dml_3);
//			System.out.println(sql_dml_3);
//			stmt.executeUpdate(sql_ddl_3);
//			System.out.println(sql_ddl_3);
//			stmt.executeUpdate(sql_ddl_4);
//			System.out.println(sql_ddl_4);
//			System.out.println(aa.get_length(sql_ddl_4));
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
