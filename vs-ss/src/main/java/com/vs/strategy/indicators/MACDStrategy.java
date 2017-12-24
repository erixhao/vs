package com.vs.strategy.indicators;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.Order;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.enums.TradeStrategy;
import com.vs.strategy.domain.TradeContext;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.MarketDataUtils;
import com.vs.strategy.AbstractTradeStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.analysis.indicators.MACDAnalyze;
import com.vs.strategy.domain.MACD;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 2017/2/16.
 */
@Component
public class MACDStrategy extends AbstractTradeStrategy implements Strategy {

    @Override
    public String getName() {
            return TradeStrategy.MACDStrategy.toString();
    }

    @Override
    public List<Order> analysis(TradeContext info) {
        List<Order> result = Lists.newArrayList();


        Stock stock = info.getStock();
        Date date = info.getAnalysisDate();

        List<HistoricalData> datas = this.marketService.getMarketHistoricalData(stock.getCode(), TimePeriod.DAILY);
        List<Double> closelist = MarketDataUtils.extractMarketClose(datas);

        MACD macd = MACDAnalyze.getMACD(closelist);

        System.out.println(">>>>> MCAD: " + DateUtils.toMarketDate(date) + ": MACD : " + macd.toString());

        return result;
    }
}
