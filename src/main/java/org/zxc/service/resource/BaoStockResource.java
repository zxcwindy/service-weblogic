package org.zxc.service.resource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zxc.service.resource.vo.KDJEntryVo;
import org.zxc.service.service.BaoStockService;
import org.zxc.service.service.StockService;
import org.zxc.service.stock.KDJEntry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;


@Controller
@RequestMapping("/api/baostock")
public class BaoStockResource {
	
	@Autowired
	private BaoStockService stockService;

	@RequestMapping(value= "/refreshAll",method = RequestMethod.GET)
	@ResponseBody
	public void refreshAll() {
		stockService.updateData();
	}
//	
//	@RequestMapping(value= "/current",method = RequestMethod.GET)
//	@ResponseBody
//	public List<KDJEntry> current() {
//		return stockService.rfreashCurrent();
//	}
//	
//	@RequestMapping(value= "/period",method = RequestMethod.GET)
//	@ResponseBody
//	public List<KDJEntry> period() {
//		return stockService.getLastPeriod();
//	}
//	
//	@RequestMapping(value= "/errorCode",method = RequestMethod.GET)
//	@ResponseBody
//	public List<String> errorCode() {
//		return stockService.getErrorCode();
//	}
//	
	@RequestMapping(value= "/log",method = RequestMethod.GET)
	@ResponseBody
	public String log() {
		return stockService.getScheduleLog();
	}
	
	@RequestMapping(value= "/mList",method = RequestMethod.GET)
	@ResponseBody
	public KDJEntryVo queryM(@RequestParam String code) throws JsonParseException, JsonMappingException, SQLException, IOException{
		return stockService.queryM(code);
	}
}
