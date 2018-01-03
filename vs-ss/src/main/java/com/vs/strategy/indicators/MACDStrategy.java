package com.vs.strategy.indicators;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradeAction;
import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.MarketDataUtils;
import com.vs.repository.MarketDataRepository;
import com.vs.strategy.AbstractStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.analysis.indicators.MACDAnalyze;
import com.vs.strategy.domain.MACD;
import com.vs.strategy.domain.MarketContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 2017/2/16.
 */
@Component
public class MACDStrategy extends AbstractStrategy implements Strategy {

    @Override
    public String getName() {
        return Strategies.MACDStrategy.toString();
    }

    @Override
    public List<TradeAction> execute(MarketContext context) {
        List<TradeAction> result = Lists.newArrayList();


        Stock stock = context.getStock();
        LocalDate date = context.getAnalysisDate();

        List<HistoricalData> datas = MarketDataRepository.getAllMarketDataBy(stock.getCode());
        List<Double> closelist = MarketDataUtils.extractMarketClose(datas);

        MACD macd = MACDAnalyze.getMACD(closelist);

        System.out.println(">>>>> MCAD: " + DateUtils.toString(date) + ": MACD : " + macd.toString());

        return result;
    }
}
