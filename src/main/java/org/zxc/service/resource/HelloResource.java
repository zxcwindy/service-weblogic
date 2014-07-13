package org.zxc.service.resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zxc.service.domain.DBColumn;

@Controller
@RequestMapping("hello")
public class HelloResource {
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String hello(){
		return "hello rest";
	}
	
	@RequestMapping(value= "world",method = RequestMethod.GET)
	@ResponseBody
	public DBColumn world(){
		DBColumn dbColumn = new DBColumn();
		dbColumn.setColName("fas杏色");
		return dbColumn;
	}
}
