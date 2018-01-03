package com.vs.strategy.gann;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradeAction;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.domain.enums.Trend;
import com.vs.strategy.AbstractStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.domain.MarketContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 15/9/5.
 */
@Component
public class HighLowMoveStrategy extends AbstractStrategy implements Strategy {

    final static double DELTA_PERCENTAGE = 5;
    final static double DELTA_INDEX_PERCENTAGE = 3;
    final static double DELTA_STRONG_PERCENTAGE = 16;

    final static double MKT_TREND_RATIO = 2;
    final static double MKT_VOLATILE_RATIO = 1.5;

    @Autowired
    HighLowMoveOptimisticStrategy highLowMoveOptimisticStrategy;


    @Override
    public String getName() {
        return Strategies.HighLowMoveStrategy.toString();
    }

    @Override
    public List<TradeAction> execute(final MarketContext context){
        List<TradeAction> result = Lists.newArrayList();

        Stock stock = context.getStock();
        LocalDate date = context.getAnalysisDate();

        HistoricalData d0 = this.getMarketDataT(context);
        HistoricalData p1 = this.getMarketDataT(context, -1);
        HistoricalData p2 = this.getMarketDataT(context,-2);
        HistoricalData p3 = this.getMarketDataT(context,-3);
        HistoricalData next  = this.getNextHistoricalDate(context);

        if ( d0 == null )
            return result;

        TradeAction action = new TradeAction(Strategies.HighLowMoveStrategy, TradeDirection.NONE,stock,date, date);

        // use close/open
        double delta = this.calcDelta(p3, d0.getClose());
        boolean isDelta = stock.isIndex() ? (Math.abs(delta) >= DELTA_INDEX_PERCENTAGE) : (Math.abs(delta) >= DELTA_PERCENTAGE);

        BullBear stockTrend = this.analysisMarketTrend(stock.getCode(),date);

        if ( moveToHigh(p3, p2) && moveToHigh(p2, p1) && moveToHigh(p1, d0) && isDelta ){
            if ( stockTrend.isBigBear() && Math.abs(delta) < DELTA_STRONG_PERCENTAGE) {

            }else {
                action.setTradeDirection(TradeDirection.BUY);
            }
        }else if (moveToLow(p3, p2) && moveToLow(p2, p1) && moveToLow(p1, d0) && isDelta){
            if ( stockTrend.isBigBull() && context.isTradeProfitable() ) {
                //System.out.println(">>>>>>> Profit: " + info.getTrade().getProfit().getTotalProfitPercentage());
            }else{

            }
            action.setTradeDirection(TradeDirection.SELL);
        }else{
            //BullBear indexTrend = indexTrendStrategy.analysisTrend(date);
            //action = highLowMoveOptimisticStrategy.execute(info,datas,next,DELTA_INDEX_PERCENTAGE,DELTA_PERCENTAGE, indexTrend);
        }

        return this.addTradeAction(result, action, next, date);
    }

    static boolean isMeetIndexTrendDelta(Stock stock, double delta , BullBear trend, Trend currentTrend ){

        if ( currentTrend.equals(Trend.UP) ){
            if ( trend.isBigBear() ){
                return false;
            }else if ( trend.isBear() ){
                return stock.isIndex() ? (Math.abs(delta) >= DELTA_INDEX_PERCENTAGE * MKT_TREND_RATIO) : (Math.abs(delta) >= DELTA_PERCENTAGE * MKT_TREND_RATIO);
            }
            else if ( trend.isVolatile() ){
                return stock.isIndex() ? (Math.abs(delta) >= DELTA_INDEX_PERCENTAGE * MKT_VOLATILE_RATIO) : (Math.abs(delta) >= DELTA_PERCENTAGE * MKT_VOLATILE_RATIO);
            }
        }else if ( currentTrend.equals(Trend.DOWN) ){
            if ( trend.isBigBull() ){
               return false;
            }else if ( trend.isBull() ){
                return stock.isIndex() ? (Math.abs(delta) >= DELTA_INDEX_PERCENTAGE * MKT_TREND_RATIO) : (Math.abs(delta) >= DELTA_PERCENTAGE * MKT_TREND_RATIO);
            }else if ( trend.isVolatile() ){
                return stock.isIndex() ? (Math.abs(delta) >= DELTA_INDEX_PERCENTAGE * MKT_VOLATILE_RATIO) : (Math.abs(delta) >= DELTA_PERCENTAGE * MKT_VOLATILE_RATIO);
            }
        }

        return true;
    }


    protected double calcDelta(HistoricalData h1, double close){
        if ( h1 == null )
            return 0;

        double low = (close >= h1.getClose()) ? h1.getLow() : h1.getHigh();

        return ((close - low)/low) * 100;

    }


    public static boolean moveToHigh(HistoricalData d1, HistoricalData d2){

        if ( d1 == null || d2 == null )
            return false;

        double d1Low  = Math.min(d1.getOpen(), d1.getClose());
        double d1High = Math.max(d1.getOpen(), d1.getClose());

        double d2Low  = Math.min(d2.getOpen(), d2.getClose());
        double d2High = Math.max(d2.getOpen(), d2.getClose());

        return (d2Low >= d1Low && d2High >= d1High);
    }

    public static boolean moveToLow(HistoricalData d1, HistoricalData d2){
        if ( d1 == null || d2 == null )
            return false;

        double d1Low  = Math.min(d1.getOpen(), d1.getClose());
        double d1High = Math.max(d1.getOpen(), d1.getClose());

        double d2Low  = Math.min(d2.getOpen(), d2.getClose());
        double d2High = Math.max(d2.getOpen(), d2.getClose());

        return (d2Low < d1Low && d2High < d1High);
    }



    /*private boolean moveToHigh1(HistoricalData d1, HistoricalData d2){

        if ( d1 == null || d2 == null )
            return false;

        double d1Low  = d1.getLow();
        double d1High = d1.getHigh();

        double d2Low  = d2.getLow();
        double d2High = d2.getHigh();

        return (d2Low > d1Low && d2High > d1High);
    }

    private boolean moveToLow1(HistoricalData d1, HistoricalData d2){
        if ( d1 == null || d2 == null )
            return false;

        double d1Low  = d1.getLow();
        double d1High = d1.getHigh();

        double d2Low  = d2.getLow();
        double d2High = d2.getHigh();

        return (d2Low < d1Low && d2High < d1High);
    }*/
}
