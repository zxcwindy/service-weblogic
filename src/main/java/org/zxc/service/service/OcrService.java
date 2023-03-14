package org.zxc.service.service;

import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.json.JSONObject;
import com.baidu.aip.ocr.AipOcr;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Service
public class OcrService {

	private static final String APP_ID = "30646459";
    private static final String API_KEY = "Pazw6rGayvR6aLDHsgoiQeMI";
    private static final String SECRET_KEY = "BsL5voYVYd4ZRnzFgEO1sBn1n0UkGIWZ";
    private static final HashMap<String,String> OPTIONS = new HashMap<>();
    private static final AipOcr CLIENT = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
    
    static {
    	OPTIONS.put("detect_direction", "true");
    	OPTIONS.put("language_type","CHN_ENG");
    }
    
    private static final Cache<String, String> CACHE = CacheBuilder.newBuilder()
            .maximumSize(50)
            .build();
    
    public String ocrPicture(String imagePath) {
    	if(CACHE.getIfPresent(imagePath) == null) {
			String result = CLIENT.basicGeneral(imagePath,OPTIONS).toString(2);
			CACHE.put(imagePath,result);
		}  
    	return CACHE.getIfPresent(imagePath);
    }
}
