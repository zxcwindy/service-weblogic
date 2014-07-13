package org.zxc.service.resource;

import java.sql.SQLException;
import java.util.HashMap;
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
			@RequestParam(value="sql") String sql) throws SQLException{
		return dbDataService.query(dbName, sql);
	}

	@RequestMapping(value = "exec/{dbName:.+}",method = RequestMethod.POST)
	@ResponseBody
	public Map execSql(@PathVariable("dbName") String dbName,
			@RequestParam(value="sql") String sql) throws SQLException {
		return dbDataService.update(dbName, sql);
	}
}
