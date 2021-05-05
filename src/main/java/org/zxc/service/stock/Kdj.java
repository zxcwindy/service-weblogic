package org.zxc.service.stock;

import java.util.ArrayList;
import java.util.List;

/**
 * https://www.jianshu.com/p/fef81b372104 
 * https://www.jianshu.com/p/dcc2cd029922
 * https://www.jianshu.com/p/cca5ff8ba1e8
 * 
 * @author david 2021年5月2日
 */
public class Kdj {
	/**
	 * kdj 9,3,3 N:=9; P1:=3; P2:=3;
	 * RSV:=(CLOSE-L(LOW,N))/(H(HIGH,N)-L(LOW,N))*100; K:SMA(RSV,P1,1);
	 * D:SMA(K,P2,1); J:3*K-2*D;
	 * 
	 * @param entries
	 *            数据集合
	 * @param n
	 *            指标周期 9
	 * @param m
	 *            权重 1
	 * @param P1
	 *            参数值为3
	 * @param P2
	 *            参数值为3
	 * @return
	 */
	public static List<CandleEntry> calc(List<CandleEntry> entries, int n, int P1, int P2, int m) {
		List<CandleEntry> maxs = getPeriodHighest(entries, n);
		List<CandleEntry> mins = getPeriodLowest(entries, n);
		// 确保和 传入的list size一致，
		int size = entries.size() - maxs.size();
		for (int i = 0; i < size; i++) {
			maxs.add(0, new CandleEntry());
			mins.add(0, new CandleEntry());
		}
		double rsv = 0;
		double lastK = 50;
		double lastD = 50;

		for (int i = n - 1; i < entries.size(); i++) {
			if (i >= maxs.size())
				break;
			if (i >= mins.size())
				break;
			double div = maxs.get(i).getY() - mins.get(i).getY();
			if (div == 0) {
				// 使用上一次的
			} else {
				rsv = ((entries.get(i).getClose() - mins.get(i).getY()) / (div)) * 100;
			}

			double k = countSMA(rsv, P1, m, lastK);
			double d = countSMA(k, P2, m, lastD);
			double j = 3 * k - 2 * d;
			lastK = k;
			lastD = d;
			entries.get(i).setK(k);
			entries.get(i).setD(d);
			entries.get(i).setJ(j);
		}

		return entries;
	}

	/**
	 * SMA(C,N,M) = (M*C+(N-M)*Y')/N C=今天收盘价－昨天收盘价 N＝就是周期比如 6或者12或者24， M＝权重，一般取1
	 *
	 * @param c
	 *            今天收盘价－昨天收盘价
	 * @param n
	 *            周期
	 * @param m
	 *            1
	 * @param sma
	 *            上一个周期的sma
	 * @return
	 */
	private static double countSMA(double c, double n, double m, double sma) {
		return (m * c + (n - m) * sma) / n;
	}

	/**
	 * n周期内最低值集合
	 * 
	 * @param entries
	 * @param n
	 * @return
	 */
	private static List<CandleEntry> getPeriodLowest(List<CandleEntry> entries, int n) {
		List<CandleEntry> result = new ArrayList<>();
		double minValue = 0;
		for (int i = n - 1; i < entries.size(); i++) {
			for (int j = i - n + 1; j <= i; j++) {
				if (j == i - n + 1) {
					minValue = entries.get(j).getLow();
				} else {
					minValue = Math.min(minValue, entries.get(j).getLow());
				}
			}
			result.add(new CandleEntry(minValue));
		}
		return result;
	}

	/**
	 * N周期内最高值集合
	 * 
	 * @param entries
	 * @param n
	 * @return
	 */
	private static List<CandleEntry> getPeriodHighest(List<CandleEntry> entries, int n) {
		List<CandleEntry> result = new ArrayList<>();
		double maxValue = entries.get(0).getHigh();
		for (int i = n - 1; i < entries.size(); i++) {
			for (int j = i - n + 1; j <= i; j++) {
				if (j == i - n + 1) {
					maxValue = entries.get(j).getHigh();
				} else {
					maxValue = Math.max(maxValue, entries.get(j).getHigh());
				}
			}
			result.add(new CandleEntry(maxValue));
		}
		return result;
	}
}
