package org.zxc.service.context;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zxc.service.cache.TableCache;
import org.zxc.service.service.DBDataService;

@Scope("prototype")
@Component
public class UpdateTableNamesCache implements Runnable{

	@Autowired
	private DBDataService<Map> dbDataService;
	
	@Autowired
	private TableNamesCacheContext cacheContext;

	private String dbName;	
	
	private static BeanFactory beanFactory = null;

	@SuppressWarnings("unchecked")
	public void run() {	
		Map<String, Object> resultMap = null;
		try {			
			resultMap = dbDataService.queryTableNames(dbName, null);
			List<String> resultList = (List<String>) resultMap.get("data");
			cacheContext.setTableNamesCache(dbName, new TableCache(resultList));
		} catch (Exception e) {
			cacheContext.resetUpdate(dbName);
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
}
