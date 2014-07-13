package org.zxc.service.resource;

import java.sql.SQLException;
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
import org.zxc.service.domain.DBTable;
import org.zxc.service.service.DBMetaService;


@Controller
@RequestMapping("dbMeta")
public class DBMetaResource {

	@Autowired
	private DBMetaService dbMetaService;

	@RequestMapping(value= "getDetail/{dbName}/{tableName:.+}",method = RequestMethod.GET)
	@ResponseBody
	public DBTable findTable(@PathVariable("dbName") String dbName,
			@RequestParam("tableName") String tableName) {
		return dbMetaService.findTable(dbName,parseTable(tableName));
	}

	@RequestMapping(value="{dbName}/{tableName}",method = RequestMethod.GET)
	@ResponseBody
	public List<String> findTableNames(@PathVariable("dbName") String dbName,
			@PathVariable("tableName") String tableName) throws SQLException {
		return dbMetaService.findTableNames(dbName,tableName);
	}

	@RequestMapping(value="getDesc/{dbName}/{tableName:.+}",method = RequestMethod.GET)
	@ResponseBody
	public String descTable(@PathVariable("dbName") String dbName,
			@PathVariable("tableName") String tableName){
		DBTable dbTable = dbMetaService.findTable(dbName,parseTable(tableName));
		return dbTable.descTable();
	}


	@RequestMapping(value="getCreateSql/{dbName}/{tableName:.+}",method = RequestMethod.GET)
	@ResponseBody
	public String getCreateTableSql(@PathVariable("dbName") String dbName,
			@PathVariable("tableName") String tableName) {
		DBTable dbTable = dbMetaService.findTable(dbName,parseTable(tableName));
		return dbTable.getCreateTableSql();
	}

	
	@RequestMapping(value="getSelectSql/{dbName}/{tableName:.+}",method = RequestMethod.GET)
	@ResponseBody
	public String getSelectSql(@PathVariable("dbName") String dbName,
			@PathVariable("tableName") String tableName) {
		DBTable dbTable = dbMetaService.findTable(dbName,parseTable(tableName));
		return dbTable.getSelectSql();
	}
	
	@RequestMapping(value="getColumnNames/{dbName}/{tableName:.+}",method = RequestMethod.GET)
	@ResponseBody
	public String getColumnNames(@PathVariable("dbName") String dbName,
			@PathVariable("tableName") String tableName) {
		DBTable dbTable = dbMetaService.findTable(dbName,parseTable(tableName));
		return dbTable.getColumnNames();
	}

	private DBTable parseTable(String tableName){
		DBTable dbTable = new DBTable();
		tableName = tableName.toUpperCase();
		if (tableName.indexOf(".") != -1) {
			tableName = tableName.toUpperCase();
			String[] strs = tableName.split("\\.");
			dbTable.setTableScheam(strs[0]);
			if(strs.length == 2){
				dbTable.setTableName(strs[1]);
			}
		}else{
			dbTable.setTableName(tableName);
		}
		return dbTable;
	}
}
