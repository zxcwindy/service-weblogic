package org.zxc.service.util;

import java.util.ArrayList;
import java.util.List;

import org.zxc.service.stock.CandleEntry;

/**
 * Created by jianglixuan on 2019/7/30
 *
 * 各种指标算法
 *
 */
public class Arithmetic {
    /**
     * 布林带BOLL（20， 2） 一般n默认取20，k取2, mb为计算好的中轨线
     * 中轨线MB: n日移动平均线 MA(n)
     * 上轨线：MB + 2*MD
     * 下轨线：MB - 2*MD
     * MD：n日方差
     *
     * @param entries
     * @param n
     * @param k
     * @return
     */
    public static List<CandleEntry> calcMB(List<CandleEntry> entries, int n, int k) {
        for (int i = 0, len = entries.size(); i < len; i++) {
            if (i < n - 1) {
                continue;
            }
            float sumMB = 0;
            float sumMD = 0;
            for (int j = n - 1; j >= 0; j--) {
            	double thisClose = entries.get(i - j).getClose();
                sumMB += thisClose;
            }
            float mb = sumMB / n;
            entries.get(i).setMb(mb);
            for (int j = n - 1; j >= 0; j--) {
            	double thisClose = entries.get(i - j).getClose();
            	double cma = thisClose - mb; // C-MB
                sumMD += cma * cma;
            }

            float md = (float) Math.pow(sumMD / (n - 1), 1.0 / k); //MD=前n日C-MB的平方和来开根
            entries.get(i).setUp(mb + 2 * md);// UP=MB+2*MD
            entries.get(i).setDn(mb - 2 * md); // DN=MB+2*MD
        }
        return entries;
    }

    // n日均线MA, 一般计算5，10，20，30
    public static List<CandleEntry> calcMA(List<CandleEntry> entries, int n) {
        for (int i = 0, len = entries.size(); i < len; i++) {
            if (i < n - 1) {
                continue;
            }
            float sum = 0;
            for (int j = 0; j < n; j++) {
                sum += entries.get(i - j).getClose();
            }
           switch(n){
        	   case 5: entries.get(i).setMa5(sum/n); break;
        	   case 10: entries.get(i).setMa10(sum/n); break;
        	   case 20: entries.get(i).setMa20(sum/n); break;
        	   case 30: entries.get(i).setMa30(sum/n); break;
        	   case 60: entries.get(i).setMa60(sum/n); break;
           }
        }
        return entries;
    }

    /**
     * EMA算法
     * EMA(N) = (2C + (N-1)EMA')/(N+1), EMA'为前一天的ema; 通常N取12和26
     *
     * @param entries
     * @param n
     * @return
     */
    public static List<CandleEntry> calcEMA(List<CandleEntry> entries, int n) {
        double lastEma = entries.get(0).getClose();// 第一个EMA为当第一个数据的价格
        entries.get(0).setEma(lastEma);

        float[] emaFactor = getEMAFactor(n);
        for (int i = 1; i < entries.size(); i++) {
            double ema = emaFactor[0] * entries.get(i).getClose() + emaFactor[1] * lastEma;
            entries.get(i).setEma(ema);
            lastEma = ema;
        }
        return entries;
    }

    /**
     * MACD算法：
     * DIF：EMA(short) - EMA(long) 一般short取12，long取26
     * DEA: EMA(DIF, mid), mid一般取9
     * MACD:(DIF-DEA)*2
     *
     * @param entries
     * @param s
     * @return
     */
    public static List<CandleEntry> calcMACD(List<CandleEntry> entries, int s, int l, int m) {

        double lastEmaS = entries.get(0).getClose();
        double lastEmaL = lastEmaS;
        double lastDIF = 0;
        entries.get(0).setDif(0);
        entries.get(0).setDea(0);
        entries.get(0).setMacd(0);

        float[] factorShort = getEMAFactor(s);
        float[] factorLong = getEMAFactor(l);
        float[] factorMid = getEMAFactor(m);
        for (int i = 1; i < entries.size(); i++) {
//            float x = entries.get(i).getX();
            // 短线EMA
            double valueS = factorShort[0] * entries.get(i).getClose() + factorShort[1] * lastEmaS;
            lastEmaS = valueS;
            // 长线EMA
            double valueL = factorLong[0] * entries.get(i).getClose() + factorLong[1] * lastEmaL;
            lastEmaL = valueL;
            // DIF：EMA(short) - EMA(long)
            double valueDIF = valueS - valueL;
            entries.get(i).setDif(valueDIF);
            // EMA(DIF, mid)
            double valueDEA = factorMid[0] * valueDIF + factorMid[1] * lastDIF;
            entries.get(i).setDea(valueDEA);
            lastDIF = valueDEA;
            // MACD:(DIF-DEA)*2
            entries.get(i).setMacd((valueDIF - valueDEA) * 2);
        }
        return entries;
    }

    /**
     * 获取EMA计算时的相关系数
     * @param n
     * @return
     */
    private static float[] getEMAFactor(int n) {
        return new float[]{2f / (n + 1), (n - 1) * 1.0f / (n + 1)};
    }

    /**
     * kdj 9,3,3
     * N:=9; P1:=3; P2:=3;
     * RSV:=(CLOSE-L(LOW,N))/(H(HIGH,N)-L(LOW,N))*100;
     * K:SMA(RSV,P1,1);
     * D:SMA(K,P2,1);
     * J:3*K-2*D;
     * @param entries 数据集合
     * @param n 指标周期 9
     * @param m 权重 1
     * @param P1 参数值为3
     * @param P2 参数值为3
     * @return
     */
    public static List<CandleEntry> calcKDJ(List<CandleEntry> entries, int n, int P1, int P2, int m) {
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
     * SMA(C,N,M) = (M*C+(N-M)*Y')/N
     * C=今天收盘价－昨天收盘价    N＝就是周期比如 6或者12或者24， M＝权重，其实就是1
     *
     * @param c   今天收盘价－昨天收盘价
     * @param n   周期
     * @param m   1
     * @param sma 上一个周期的sma
     * @return
     */
    private static double countSMA(double c, double n, double m, double sma) {
        return (m * c + (n - m) * sma) / n;
    }

    /**
     * n周期内最低值
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
     *  N周期内最高值
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

    /**
     * RSI（n)
     * RSI(N):= SMA(MAX(Close-LastClose,0),N,1)/SMA(ABS(Close-LastClose),N,1)*100
     *
     * @param entries
     * @param n
     * @param m 加权 1
     * @return
     */
//    public static List<Entry> getRSI(List<CandleEntry> entries, int n, int m) {
//        List<Entry> result = new ArrayList();
//        float preIn = 0;
//        float preAll = 0;
//        for (int i = 1; i < entries.size(); i++) {
//            float diff = entries.get(i).getClose() - entries.get(i - 1).getClose();
//            preIn = countSMA(Math.max(diff, 0), n, m, preIn);
//            preAll = countSMA(Math.abs(diff), n, m, preAll);
//            if (i >= n) {
//                float x = entries.get(i).getX();
//                result.add(new Entry(x, preIn / preAll * 100));
//            }
//        }
//        return result;
//    }
}

