package org.zxc.service.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataFetcherFactory implements InitializingBean{

	@Autowired
	private List<DataFetcher> listDataFetcher;
	
	private static final Map<SourceEnum,DataFetcher> DATA_FETCHERS = new HashMap<>();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		listDataFetcher.forEach(d -> DATA_FETCHERS.putIfAbsent(d.getCode(), d));
	}

	public DataFetcher getDataFetcher(SourceEnum sourceEnum){
		return DATA_FETCHERS.get(sourceEnum);
	}
}
