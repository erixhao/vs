package com.vs.strategy.gann;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradeAction;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.strategy.AbstractStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.analysis.ExtremeAnalyze;
import com.vs.strategy.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by erix-mac on 15/10/25.
 */
@Slf4j
@Component
public class PercentageStrategy extends AbstractStrategy implements Strategy {

    private final static double DOWN_HALF = -0.618;
    private final static int TIME_WINDOW = -1;
    private final static int TOP_RANK = 1;

    @Autowired
    private ExtremeAnalyze extremeAnalyze;

    private static Map<String, Map<TimeWindow,List<PeakStrategy>>> topMap = Maps.newConcurrentMap();


    private List<PeakStrategy> getPeaks(String code, TimeWindow window){
        Map<TimeWindow,List<PeakStrategy>> map = topMap.get(code);
        return map == null ? null : map.get(window);
    }

    private void setPeaks(String code, TimeWindow window, List<PeakStrategy> peaks){
        Map<TimeWindow,List<PeakStrategy>> map = topMap.get(code);

        if ( map == null ){
            map = Maps.newConcurrentMap();
            map.put(window,peaks);
            topMap.put(code,map);
        }else {
            map.put(window,peaks);
        }
    }

    @Override
    public String getName() {
        return Strategies.PercentageStrategy.toString();
    }

    public Strategy init(Stock stock, TimeWindow window, LocalDate date){

        List<PeakStrategy> currentPeaks = getPeaks(stock.getCode(), window);
        if ( currentPeaks == null || currentPeaks.size() == 0  ){
            Map<Peak, List<MarketPeak>> peaks = extremeAnalyze.determinMarketPeaks(stock, TimeWindow.getTimeWindow(TimePeriod.DAILY, date, TIME_WINDOW, 0, 0), TOP_RANK);
            this.setPeaks(stock.getCode(), window, PeakStrategy.newPeakStrategy(MarketPeak.extractPeakNumber(peaks.get(Peak.TOP))));
        }

        return this;
    }

    @Override
    public List<TradeAction> execute(MarketContext context){
        List<TradeAction> result = Lists.newArrayList();

        Stock stock = context.getStock();
        LocalDate date = context.getAnalysisDate();
        TradingBook tradingBook = context.getTradingBook();
        TimeWindow window = context.getTimeWindow();

        HistoricalData market = this.getCurrentMarket(stock.getCode(),date);
        List<Double> top = PeakStrategy.toPeaks(getPeaks(stock.getCode(), window));
        LocalDate today = date;

        this.init(stock, window, date);

        if (Dividends.isSplitDate(stock, today)) {
            //System.out.println(">>>>> PercentageStrategy : Before Split :" + top.toString());
            top = Dividends.adjustDividendsSplit(stock, today, market, top);
            this.setPeaks(stock.getCode(),window, PeakStrategy.updateTops(this.getPeaks(stock.getCode(),window), top));
            //System.out.println(">>>>>>> After Split : " + top.toString());
        }

        double marketPrice = context.getMarketPrice();
        updateLatestTop(stock,window,marketPrice);

        TradeAction action = new TradeAction(Strategies.PercentageStrategy, TradeDirection.NONE, stock, today, today, marketPrice);
        if ( isTriggerPercentageTrade(stock, window, action, marketPrice)) {
            //System.out.println("----------------------------------------->>>>> PercentageStrategy :  Action" + action.toString());
            result.add(action);
        }

        return result;
    }

    private void updateLatestTop(Stock stock, TimeWindow window, double market){

        List<PeakStrategy> peaks = this.getPeaks(stock.getCode(),window);

        if ( peaks == null ){
            peaks = Lists.newArrayList();
            peaks.add(new PeakStrategy(market, false));

            this.setPeaks(stock.getCode(), window, peaks);
        }else if ( peaks.size() <= TOP_RANK ){
            peaks.add(new PeakStrategy(market,false));
        }else {
            double latestTop = peaks.get(TOP_RANK).peak; //last index
            if ( market > latestTop ){
                peaks.get(TOP_RANK).peak = market;
            }
        }
    }

    private boolean isTriggerPercentageTrade(Stock stock, TimeWindow window, TradeAction action, double market) {

        boolean isTrigger = false;

        List<PeakStrategy> peaks = this.getPeaks(stock.getCode(), window);

        for ( PeakStrategy p : peaks ){
            double delta = (market - p.peak) / p.peak;

            if ( !p.traded && delta <= DOWN_HALF) {
                action.setTradeDirection(TradeDirection.BUY);
                //System.out.println("+++++++++++++++++++++++++++++++++++>>>>> PercentageStrategy Delta : " + delta + " Peak:" + p.toString() + " Current: " + market + " :  Action" + action.toString());

                p.traded = true;
                isTrigger = true;
                break;
            }
        }

        return isTrigger;
    }



}
