package org.zxc.service.resource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zxc.service.service.DBDataService;

@Controller
@RequestMapping("data")
public class DBDataResource {

	@Autowired
	private DBDataService<Map> dbDataService;


	@RequestMapping(value= "query/{dbName:.+}",method = RequestMethod.POST)
	@ResponseBody
	public Map querySql(@PathVariable("dbName") String dbName,
			@RequestParam(value="sql") String sql, @RequestParam(value="limit",required=false,defaultValue="100") int limit) throws SQLException{
		return dbDataService.query(dbName, sql,limit);
	}

	/**
	 * 根据数据库与指定的sql脚本执行dml语句，sql脚本按 分号分隔，忽略注释脚本
	 * @param dbName 数据库名
	 * @param sql sql语句，可以包含多条sql，按;分隔
	 * @return sql语句执行成功的数组
	 * @throws SQLException 返回出错的sql信息
	 */
	@RequestMapping(value = "exec/{dbName:.+}",method = RequestMethod.POST)
	@ResponseBody
	public Map execSql(@PathVariable("dbName") String dbName,
			@RequestParam(value="sql") String sql) throws SQLException {
		String[] sqls = sql.split("(;\n)");
		List<String> sqlList = new ArrayList<String>();
		for(int i =0; i < sqls.length; i++){
			if( ! sqls[i].matches("^( |	)*--.*")){
				sqlList.add(sqls[i].replaceAll(";$", ""));
			}
		}
		
		return dbDataService.update(dbName, sqlList);
	}
}
