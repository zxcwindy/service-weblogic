package org.zxc.service.resource;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zxc.service.service.OcrService;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/api/ai")
public class AIResource {
	
	@Autowired
	private OcrService ocrService;
	
	@RequestMapping(value= "/ocr",method = RequestMethod.GET)
	@ResponseBody
	public Object orc(@RequestParam  String imagePath) throws JsonParseException, JsonMappingException, IOException {
		String result = ocrService.ocrPicture(URLDecoder.decode(imagePath, "utf-8"));
		return new ObjectMapper().readValue(result,Map.class);
	}
}
