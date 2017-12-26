package com.vs.strategy.common;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradeAction;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.strategy.AbstractStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.domain.MarketContext;
import com.vs.strategy.index.IndexTrendStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 2017/1/9.
 */
@Slf4j
@Component
public class MarketTrendStrategy extends AbstractStrategy implements Strategy {

    @Autowired
    IndexTrendStrategy indexTrendStrategy;

    @Autowired
    private MarketTrendAnalyze marketTrendAnalyze;

    @Override
    public String getName() {
        return Strategies.MarketTrendStrategy.toString();
    }

    @Override
    public List<TradeAction> execute(MarketContext context){
        List<TradeAction> result = Lists.newArrayList();

        Stock stock = context.getStock();
        Date date = context.getAnalysisDate();

        List<HistoricalData> datas = this.marketService.getMarketHistoricalData(stock.getCode(), TimePeriod.DAILY);
        HistoricalData next  = this.getNextHistoricalDate(context);
        BullBear indexTrend = indexTrendStrategy.analysisTrend(date);
        BullBear currentTrend = marketTrendAnalyze.getCachedMarketTrend(stock.getCode(), date);
        BullBear lastTrend = marketTrendAnalyze.getCachedLastWeekMarketTrend(stock.getCode(),date);

        if ( currentTrend == null || lastTrend == null )
            return result;

        TradeAction action = new TradeAction(Strategies.MarketTrendStrategy, TradeDirection.NONE,stock,date, date);

        if ( !lastTrend.isBull() && currentTrend.isBull() ){
            action.setTradeDirection(TradeDirection.BUY);
        }else if (  !lastTrend.isBear() && currentTrend.isBear() ){
            action.setTradeDirection(TradeDirection.SELL);
        }else {
            action.setTradeDirection(TradeDirection.NONE);
        }

        result = this.addTradeAction(result, action, next, date);
        return result;
    }
}
