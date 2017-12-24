package com.vs.strategy.analysis.indicators;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.utils.MarketDataUtils;
import com.vs.strategy.domain.MAPeriod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 2017/8/12.
 */
@Slf4j
public class MAAnalyze {



        public static final Double getMAByPair(final List<Pair<Date,Double>> pairList, MAPeriod period) {
            List<Double> list = Lists.newArrayList();

            for ( Pair<Date, Double> p : pairList ){
                list.add(p.getRight());
            }

            return getMA(list, period);
        }

        public static final Double getMA(final List<Double> list, MAPeriod period) {
            if ( list.size() < period.getPeriod() ) {
                log.error("List's size less than MA period, size: " + list.size() + " MA:" + period.getPeriod());
                return Double.NaN;
            }

            Double sum = 0.;
            for (int i = 0; i <period.getPeriod() ; i++) {
               sum += list.get(i);
            }

            return sum / period.getPeriod();
        }

    public static final Double getMAByHistoricalData(final List<HistoricalData> list, MAPeriod period) {
        return getMA(MarketDataUtils.extractMarketClose(list), period);
    }
}
