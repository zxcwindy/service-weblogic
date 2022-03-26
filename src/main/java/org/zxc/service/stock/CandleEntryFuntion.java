package org.zxc.service.stock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.zxc.service.exception.StockException;

/**
 *查找符合条件的Entry
 * @author david
 * 2022年3月13日
 */
public class CandleEntryFuntion{
	private static Map<String,Method> METHODS_MAP = new HashMap<>();
	static{
		Method[] methods = CandleEntry.class.getMethods();
		for(int i = 0; i < methods.length; i++){
			String methodName = methods[i].getName();
			if(methodName.startsWith("get")){
				METHODS_MAP.put(methodName.replace("get", "").toLowerCase(), methods[i]);
			}
		}
	}

	public static Double calc(CandleEntry entry ,String attr){
		Method method = METHODS_MAP.get(attr);
		if(method == null){
			throw new StockException("未找到对应的属性");
		}
		try {
			Object result = method.invoke(entry);
			if (result != null) {
				DecimalFormat df = new DecimalFormat("0.00");
				BigDecimal bigDecimal = new BigDecimal(String.valueOf(result));
				df.setRoundingMode(RoundingMode.HALF_UP);
				// 四舍五入。需要将数据转成bigDecimal, 否则会存在经度丢失问题
				String format = df.format(bigDecimal);
				double aDouble = Double.parseDouble(format);
				return aDouble;// 返回数字格式
			}else{
				return 0.0;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new StockException("计算异常");
		}
	}
}
