package org.zxc.service.service;

import junit.framework.Assert;

//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zxc.service.domain.DBTable;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:application-config.xml"})
public class DBMetaServiceTest {

//	@Autowired
	private DBMetaService dbMetaService;
	
//	@Test
	public void testFindTable(){
		DBTable dbTable = dbMetaService.findTable();
		Assert.assertNotNull(dbTable);
	}
}
