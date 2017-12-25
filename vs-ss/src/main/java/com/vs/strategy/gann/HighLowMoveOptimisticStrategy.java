package com.vs.strategy.gann;


import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradeAction;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.domain.enums.Trend;
import com.vs.common.utils.MarketDataUtils;
import com.vs.strategy.domain.MarketContext;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.vs.strategy.gann.HighLowMoveStrategy.*;


/**
 * Created by erix-mac on 2016/10/7.
 */
@Component
public final class HighLowMoveOptimisticStrategy {


    public TradeAction execute(final MarketContext context, final List<HistoricalData> datas, HistoricalData next, double DELTA_INDEX_PERCENTAGE, double DELTA_PERCENTAGE, BullBear trend){

        Stock stock = context.getStock();
        Date date = context.getAnalysisDate();

        HistoricalData d0 = MarketDataUtils.getMarketCurrent(datas,date);
        HistoricalData p1 = MarketDataUtils.getMarketT(datas, date, -1);
        HistoricalData p2 = MarketDataUtils.getMarketT(datas,date,-2);
        HistoricalData p3 = MarketDataUtils.getMarketT(datas,date,-3);
        HistoricalData p4 = MarketDataUtils.getMarketT(datas,date,-4);

        if ( d0 == null )
            return null;

        TradeAction action = new TradeAction(Strategies.OptimisticHLMoveStrategy, TradeDirection.NONE,stock,date, date);

        // use close/open
        double delta = calcDelta(p4.getLow(), d0.getClose());
        boolean isDelta = stock.isIndex() ? (Math.abs(delta) >= DELTA_INDEX_PERCENTAGE) : (Math.abs(delta) >= DELTA_PERCENTAGE);

        if ( isDelta && isMoveToHigh(p4,p3,p2,p1,d0) ){
            action.setTradeDirection(TradeDirection.BUY);
        }else if ( isDelta && isMoveToLow(p4,p3,p2,p1,d0) && isMeetIndexTrendDelta(stock,delta,trend, Trend.DOWN) ){
            action.setTradeDirection(TradeDirection.SELL);
        }

        return action;
    }



    private static boolean isMoveToHigh(HistoricalData p4, HistoricalData p3, HistoricalData p2, HistoricalData p1, HistoricalData d0){
        int count = 0;

        if ( moveToHigh(p4, p3) )
            count ++;
        if ( moveToHigh(p3, p2) )
            count ++;
        if ( moveToHigh(p2, p1) )
            count ++;
        if ( moveToHigh(p1, d0) )
            count ++;

        return (count >= 3 && moveToHighOptimistic(p1,d0));
    }




    private static boolean isMoveToLow(HistoricalData p4, HistoricalData p3, HistoricalData p2, HistoricalData p1, HistoricalData d0){
        int count = 0;

        if ( moveToLow(p4, p3) )
            count ++;
        if ( moveToLow(p3, p2) )
            count ++;
        if ( moveToLow(p2, p1) )
            count ++;
        if ( moveToLow(p1, d0) )
            count ++;

        return (count >= 3 && moveToLowOptimistic(p1,d0));
    }

    private static double calcDelta(double open, double close){
        return ((close - open)/open) * 100;
    }


    private static boolean moveToHighOptimistic(HistoricalData d1, HistoricalData d2){

        if ( d1 == null || d2 == null )
            return false;

        double d1High = Math.max(d1.getOpen(), d1.getClose());
        double d2High = Math.max(d2.getOpen(), d2.getClose());

        return d2High > d1High;
    }

    private static boolean moveToLowOptimistic(HistoricalData d1, HistoricalData d2){
        if ( d1 == null || d2 == null )
            return false;

        double d1Low  = Math.min(d1.getOpen(), d1.getClose());
        double d2Low  = Math.min(d2.getOpen(), d2.getClose());

        return d2Low < d1Low;
    }

}
