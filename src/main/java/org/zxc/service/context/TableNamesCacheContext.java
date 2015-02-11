package org.zxc.service.context;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Component;
import org.zxc.service.cache.TableCache;


@Component
public class TableNamesCacheContext extends ApplicationObjectSupport{

	private ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(10);
	
	private static final Properties INTERVAL_PRO = new Properties();
	
	private static final String INTERVAL_SUFFIX="-interval";
	
	static{
		try {
			INTERVAL_PRO.load(TableNamesCacheContext.class.getResourceAsStream("/interval.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private volatile Map<String, TableCache> cacheMap = new Hashtable<String, TableCache>();
	
	private volatile Map<String, Boolean> isUpdateMap = new HashMap<String, Boolean>();

	public TableCache getTableNamesCache(String dbName) {
		TableCache tableCache = cacheMap.get(dbName);		
		
		if (isUpdateMap.get(dbName) == null) {						
			synchronized (isUpdateMap) {
				if(isUpdateMap.get(dbName) == null){								
					 if(INTERVAL_PRO.getProperty(dbName+INTERVAL_SUFFIX) != null){
						 UpdateTableNamesCache cache = this.getApplicationContext().getBean(UpdateTableNamesCache.class);	
							cache.setDbName(dbName);
						 int interval =Integer.valueOf(INTERVAL_PRO.getProperty(dbName+INTERVAL_SUFFIX));					
							scheduler.scheduleAtFixedRate(cache, 0, interval, TimeUnit.SECONDS);
					 }					
					isUpdateMap.put(dbName, true);
				}
			}			
		}
		return tableCache == null ? new TableCache(new ArrayList<String>())
				: tableCache;
	}

	public void setTableNamesCache(String dbName, TableCache cache) {	
		cacheMap.put(dbName, cache);		
	}
	
	public void resetUpdate(String dbName){
		isUpdateMap.remove(dbName);
	}
}
