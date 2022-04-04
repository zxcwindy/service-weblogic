package org.zxc.service.resource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zxc.service.datasource.SourceEnum;
import org.zxc.service.resource.vo.ConditionVo;
import org.zxc.service.service.StockKpiService;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.Condition;
import org.zxc.service.stock.Period;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;


@Controller
@RequestMapping("/api/stockkpi")
public class StockKpiResource {
	
	@Autowired
	private StockKpiService stockKpiService;

	@RequestMapping(value= "/refreshAll",method = RequestMethod.GET)
	@ResponseBody
	public void refreshAll() {
		new Thread( () -> {
			stockKpiService.updateData();
		}).start();
	}
	
	@RequestMapping(value= "/fetchers",method = RequestMethod.GET)
	@ResponseBody
	public Map<Period,SourceEnum> getPeriodDataFetcher() {
		return StockKpiService.getPeriodDataFetcher();
	}
	
	@RequestMapping(value= "/fetchers",method = RequestMethod.POST)
	@ResponseBody
	public Map<Period,SourceEnum> updatePeriodDataFetcher(@RequestParam Period period,@RequestParam SourceEnum source) {
		StockKpiService.getPeriodDataFetcher().put(period, source);
		return StockKpiService.getPeriodDataFetcher();
	}
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
		return stockKpiService.getScheduleLog();
	}
	
	@RequestMapping(value= "/login",method = RequestMethod.GET)
	@ResponseBody
	public void login() {
		stockKpiService.login();
	}
	
	@RequestMapping(value= "/data",method = RequestMethod.GET)
	@ResponseBody
	public List<CandleEntry> queryM(@RequestParam String code,@RequestParam Period period ,@RequestParam(required=false) boolean refresh) throws JsonParseException, JsonMappingException, SQLException, IOException{
		return stockKpiService.queryData(code, period, refresh);
	}
	
	@RequestMapping(value= "/findCode",method = RequestMethod.GET)
	@ResponseBody
	public List<String> findByCondition(@RequestParam String condition) {
		return stockKpiService.findCodeByCond(condition);
	}
	
	@RequestMapping(value= "/updateNow",method = RequestMethod.GET)
	@ResponseBody
	public void updateNow() {
		new Thread( () -> {
			stockKpiService.updateRealTimeData();
		}).start();
	}
}
