package org.zxc.service.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-config.xml","classpath:dispatch-servlet.xml"})
@WebAppConfiguration
public class StockKpiServiceTest {

	@Autowired
	private StockKpiService stockKpiService;
	
	@Test
	public void testUpdateData(){
		stockKpiService.updatePeriod();
		stockKpiService.updatetStockList();
		stockKpiService.updateData();
		List<String> codeList = stockKpiService.findCodeByCond("1.d.macd == 0.36 && 1.w.macd < 0");
		System.out.println(codeList);
	}
}
