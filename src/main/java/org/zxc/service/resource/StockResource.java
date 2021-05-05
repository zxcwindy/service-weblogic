package org.zxc.service.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zxc.service.service.StockService;
import org.zxc.service.stock.KDJEntry;


@Controller
@RequestMapping("/api/stock")
public class StockResource {
	
	@Autowired
	private StockService stockService;

	@RequestMapping(value= "/refreshAll",method = RequestMethod.GET)
	@ResponseBody
	public void refreshAll() {
		stockService.refreshAll();
	}
	
	@RequestMapping(value= "/current",method = RequestMethod.GET)
	@ResponseBody
	public List<KDJEntry> current() {
		return stockService.rfreashCurrent();
	}
	
	@RequestMapping(value= "/period",method = RequestMethod.GET)
	@ResponseBody
	public List<KDJEntry> period() {
		return stockService.getLastPeriod();
	}
	
	@RequestMapping(value= "/errorCode",method = RequestMethod.GET)
	@ResponseBody
	public List<String> errorCode() {
		return stockService.getErrorCode();
	}
	
	@RequestMapping(value= "/log",method = RequestMethod.GET)
	@ResponseBody
	public String log() {
		return stockService.getScheduleLog();
	}
}
