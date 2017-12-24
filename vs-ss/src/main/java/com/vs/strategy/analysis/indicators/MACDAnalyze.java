package com.vs.strategy.analysis.indicators;

import com.google.common.collect.Lists;
import com.vs.strategy.domain.MACD;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by erix-mac on 2017/2/16.
 */
@Component
public class MACDAnalyze {

    private static final int DEFAULT_SHORT_PERIOD = 9;
    private static final int DEFAULT_MID_PERIOD = 12;
    private static final int DEFAULT_LONG_PERIOD = 26;

    public static final Double getEXPMA(final List<Double> list, final int number) {
        Double k = 2.0 / (number + 1.0);
        Double ema = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            ema = list.get(i) * k + ema * (1 - k);
        }

        return ema;
    }

    public static final MACD getMACD(final List<Double> list){
        return getMACD(list, DEFAULT_SHORT_PERIOD, DEFAULT_LONG_PERIOD, DEFAULT_MID_PERIOD);
    }

    public static final MACD getMACD(final List<Double> list, final int shortPeriod, final int longPeriod, int midPeriod) {
        List<Double> diffList = Lists.newArrayList();
        Double shortEMA;
        Double longEMA;
        Double dif = 0.0;
        Double dea;

        for (int i = list.size() - 1; i >= 0; i--) {
            List<Double> sublist = list.subList(0, list.size() - i);
            shortEMA = getEXPMA(sublist, shortPeriod);
            longEMA = getEXPMA(sublist, longPeriod);
            dif = shortEMA - longEMA;
            diffList.add(dif);
        }
        dea = getEXPMA(diffList, midPeriod);

        return new MACD(dif,dea);
    }

}
