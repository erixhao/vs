package com.vs.strategy.gann;

import com.google.common.collect.Lists;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradeAction;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.strategy.AbstractStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.common.MarketTrendAnalyze;
import com.vs.strategy.domain.MarketContext;
import com.vs.strategy.index.IndexTrendStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 15/10/24.
 */
@Slf4j
@Component
public class KeepProfitStrategy extends AbstractStrategy implements Strategy {

    private final static double DELTA_700_PROFIT = 250;
    private final static double DELTA_600_PROFIT = 150;
    private final static double DELTA_500_PROFIT = 100;
    private final static double DELTA_200_PROFIT = 80;
    private final static double DELTA_100_PROFIT = 40;
    private final static double DELTA_75_PROFIT = 35;
    private final static double DELTA_50_PROFIT = 30;
    private final static double DELTA_30_PROFIT = 15;
    private final static double DELTA_20_PROFIT = 10;

    private final static double[] PERCENTAGE      = {900,800,700,600,500,400,300,200,100,90,80,70,60,50,40,30,20,10};
    private final static double[] KEEP_PERCENTAGE = {500,450,400,350,260,200,150, 90, 40,30,30,30,20,20,15,10,7, 3 };

    @Autowired
    private IndexTrendStrategy indexTrendStrategy;
    @Autowired
    private MarketTrendAnalyze marketTrendAnalyze;

    @Override
    public String getName() {
        return Strategies.KeepTradeProfitStrategy.toString();
    }

    @Override
    public List<TradeAction> execute(MarketContext context){
        List<TradeAction> result = Lists.newArrayList();

        Stock stock = context.getStock();
        Date date = context.getAnalysisDate();
        TradingBook tradingBook = context.getTradingBook();

        if ( tradingBook.getPositions() == 0 )
            return result;

        double currentProfit = tradingBook.getPnL().getTotalProfitPercentage();
        double maxProfit = tradingBook.getMarkToMarket().getHighestProfit();
        if ( currentProfit > maxProfit ){
            tradingBook.getMarkToMarket().setHighestProfit(currentProfit);
            //System.out.println(">>>>>>>>>>>>>>>Date: " + DateUtils.toMarketDate(date) + " Market Price: " + info.getMarketPrice() + " Current Profit : " + currentProfit + " Highest Profit: " + maxProfit);
        }else{
            //BullBear indexTrend = indexTrendStrategy.analysisTrend(date);
            BullBear stockTrend = marketTrendAnalyze.analysisTrend(stock.getCode(),date);

            double ratio = stockTrend == null ? 1 : (stockTrend.isBigBull() ? 1.5 : 1);

            if ( triggerKeepProfit(maxProfit,currentProfit, ratio) ){
                //System.out.println(">>>>>>Index: " + indexTrend.toString() + "  Stock: " + stockTrend.toString());
                //log.info(">>>>>>>>>>>>>>>>>  Should Trigger Keep Profit: " + stock.getCode());
               // System.out.println(">>>>>>>>>>>>>>>Date: " + DateUtils.toMarketDate(date) + " Ratio: " + ratio + " Market Price: " + info.getMarketPrice() + " Current Profit : " + currentProfit + " Highest Profit: " + maxProfit);
                TradeAction action = new TradeAction(Strategies.KeepTradeProfitStrategy, TradeDirection.SELL,stock,date,date);
                action.setTradeDate(date);
                action.setTradePrice(context.getMarketPrice());

                result.add(action);
                //log.info(">>>>>>>>>>>>>>>>>  Actually Trigger Keep Profit: " + stock.getCode());
            }
        }

        return result;
    }

    private boolean triggerKeepProfit(double max, double current, double ratio){

        if ( current <=0 )
            return false;

        for ( int i=0;i<PERCENTAGE.length;i++ ){
            if ( max >= PERCENTAGE[i] ){
                return current * ratio <= KEEP_PERCENTAGE[i];
            }
        }

        return false;
    }

    private boolean triggerKeepProfit2(double max, double current, double ratio){

        if ( current <=0 )
            return false;

        boolean isTrigger = false;
        double delta = (max - current);

        if ( max <= 700 ){
            isTrigger = delta >= DELTA_700_PROFIT * ratio;
        }else if ( max <= 600 ){
            isTrigger = delta >= DELTA_600_PROFIT * ratio;
        }else if ( max <= 500 ){
            isTrigger = delta >= DELTA_500_PROFIT * ratio;
        }else if ( max <= 200 ){
            isTrigger = delta >= DELTA_200_PROFIT * ratio;
        }else if ( max <= 100 ){
            isTrigger = delta >= DELTA_100_PROFIT * ratio;
        }else if ( max <= 75 ){
            isTrigger = delta >= DELTA_75_PROFIT * ratio;
        }else if ( max <= 50 ){
            isTrigger = delta >= DELTA_50_PROFIT * ratio;
        }else if ( max <= 30){
            isTrigger = delta >= DELTA_30_PROFIT * ratio;
        }else if ( max <= 20){
            isTrigger = delta >= DELTA_20_PROFIT * ratio;
        }

        return isTrigger;
    }
}
