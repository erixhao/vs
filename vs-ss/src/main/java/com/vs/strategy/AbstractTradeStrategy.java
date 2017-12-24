package com.vs.strategy;


import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Order;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.strategy.domain.TradeContext;
import com.vs.common.utils.MarketDataUtils;
import com.vs.market.MarketDataService;
import com.vs.strategy.common.MarketTrendAnalyze;
import com.vs.strategy.index.IndexTrendStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 15/8/22.
 */
@Slf4j
public abstract class AbstractTradeStrategy implements Strategy {
    @Autowired
    protected MarketDataService marketService;

    @Autowired
    private MarketTrendAnalyze marketTrendAnalyze;

    @Autowired
    IndexTrendStrategy indexTrendStrategy;



    @Override
    public abstract List<Order> analysis(TradeContext info);

    protected BullBear analysisMarketTrend(String code, final Date date){
        return  marketTrendAnalyze.analysisTrend(code,date);
    }

    protected BullBear analysisIndexTrend(Date date){
        return  indexTrendStrategy.analysisTrend(date);
    }

    protected HistoricalData getCurrentMarket(String code, Date date){
        return getMarketDataT(code, date, 0);
    }

    protected HistoricalData getMarketDataT(TradeContext info){
        return getMarketDataT(info,0);
    }

    protected HistoricalData getMarketDataT(String code, Date date, int T){
        List<HistoricalData> datas = this.getMarketDataList(code);
        return MarketDataUtils.getMarketT(datas, date, T);    }

    protected HistoricalData getMarketDataT(TradeContext info, int T){
        return getMarketDataT(info.getStock().getCode(), info.getAnalysisDate(), T);
    }

    protected List<HistoricalData> getMarketDataList(String code){
        return this.marketService.getMarketHistoricalData(code, TimePeriod.DAILY);
    }

    protected HistoricalData getNextHistoricalDate(TradeContext info){
        List<HistoricalData> datas = this.getMarketDataList(info.getStock().getCode());
        HistoricalData d0 = MarketDataUtils.getMarketCurrent(datas,info.getAnalysisDate());
        HistoricalData d1 = MarketDataUtils.getMarketT(datas, info.getAnalysisDate(), 1);

        if ( d1 != null ){
            return d1;
        }else {
            if ( d0 == null ){
                log.error(">>>> Exception, Please sync-up with Market Data: " + info.getAnalysisDate().toString());
                return null;
            }

            Date next = (d1 == null ? MarketDataUtils.getNextTradeDate(datas,d0.getDate()) : d1.getDate());
            double price = (d1 == null ? d0.getClose() : d1.getOpen());

            return new HistoricalData(next,TimePeriod.DAILY,price,d0.getClose(),info.getStock());
        }
    }

    protected List<Order> addTradeAction(List<Order> result, Order action, HistoricalData next, Date date){
        if ( action.getTradeDirection().equals(TradeDirection.SELL) || action.getTradeDirection().equals(TradeDirection.BUY) ){
            Date nextTradeDate = next.getDate() == null ? MarketDataUtils.getNextTradeDate(date) : next.getDate();
            action.setTradeDate(nextTradeDate);
            action.setTradePrice(next.getOpen());
            result.add(action);
        }

        return result;
    }

}
