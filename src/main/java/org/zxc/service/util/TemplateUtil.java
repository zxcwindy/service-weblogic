package org.zxc.service.util;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class TemplateUtil {
	private static final String TEMPLATE_PATH = File.separator + "velocity"
			+ File.separator;
	private static final String LOCALE = "utf-8";

	static {
		Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		Velocity.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init();
	}

	public static String getSql(Map<String, Object> dataMap, String vmName) {
		VelocityContext context = new VelocityContext();
		if(dataMap != null){
			for (Entry<String, Object> entry : dataMap.entrySet()) {
				context.put(entry.getKey(), entry.getValue());
			}
		}
		
		Template template = Velocity.getTemplate(
				TEMPLATE_PATH + vmName + ".vm", LOCALE);
		StringWriter sw = new StringWriter();
		template.merge(context, sw);
		return sw.toString();
	}
}
