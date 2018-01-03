package com.vs.strategy.indicators;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradeAction;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.repository.MarketDataRepository;
import com.vs.strategy.AbstractStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.analysis.indicators.MAAnalyze;
import com.vs.strategy.common.MarketTrendAnalyze;
import com.vs.strategy.domain.MAPeriod;
import com.vs.strategy.domain.MarketContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;


/**
 * Created by erix-mac on 2017/2/16.
 */
@Component
public class MAStrategy extends AbstractStrategy implements Strategy {

    private final static int STAY_DAYS = 3;

    @Override
    public String getName() {
        return Strategies.MAStrategy.toString();
    }

    @Autowired
    private MarketTrendAnalyze marketTrendAnalyze;


    @Override
    public List<TradeAction> execute(MarketContext context) {
        List<TradeAction> result = Lists.newArrayList();

        Stock stock = context.getStock();
        LocalDate date = context.getAnalysisDate();

        BullBear stockTrend = marketTrendAnalyze.analysisTrend(stock.getCode(), date);

        //System.out.println(">>>>>> Current Trend is : " + );

        if (stockTrend.isVolatile())
            return result;


        List<HistoricalData> datas = MarketDataRepository.getAllMarketDataBy(stock.getCode());
        HistoricalData next = this.getNextHistoricalDate(context);

        TradeAction action = new TradeAction(Strategies.MAStrategy, TradeDirection.NONE, stock, date, date);

        //DebugUtils.debugAt(date, "2017-08-24");
        TradeDirection direction20 = this.analysisMAStrategy(datas, date, MAPeriod.MA20);
        //TradeDirection direction60 = this.analysisMAStrategy(datas, date, MAPeriod.MA60);
        //System.out.println("Date: " + date.toString() + " Acton20: " + direction20.toString() + " Action 60:" + direction60 + " action 60 == 20 ?" + direction20.equals(direction60));
        action.setTradeDirection(and(direction20));
        return this.addTradeAction(result, action, next, date);
    }

    public TradeDirection and(TradeDirection... directions) {
        TradeDirection first = directions[0];

        for (TradeDirection dir : directions) {
            if (!dir.equals(first))
                return TradeDirection.NONE;
        }

        return first;
    }

    public TradeDirection analysisMAStrategy(final List<HistoricalData> datas, LocalDate date, MAPeriod period) {
        TradeDirection direction = TradeDirection.NONE;

        List<HistoricalData> closeList = MarketDataRepository.filterBy(datas, date, date.plusDays(period.getPeriod() + STAY_DAYS));
        //extractMarkets(datas, date, period.getPeriod() + STAY_DAYS);
        final int SIZE = closeList.size();
        if (SIZE <= STAY_DAYS)
            return direction;

        List<HistoricalData> d0List = closeList;
        List<HistoricalData> p1List = closeList.subList(1, SIZE);
        List<HistoricalData> p2List = closeList.subList(2, SIZE);
        List<HistoricalData> p3List = closeList.subList(3, SIZE);
        List<HistoricalData> p4List = closeList.subList(4, SIZE);


        HistoricalData p0Data = closeList.get(0);
        HistoricalData p1Data = closeList.get(1);
        HistoricalData p2Data = closeList.get(2);
        HistoricalData p3Data = closeList.get(3);
        HistoricalData p4Data = closeList.get(4);


        double d0MA = MAAnalyze.getMAByHistoricalData(d0List, period);
        double p1MA = MAAnalyze.getMAByHistoricalData(p1List, period);
        double p2MA = MAAnalyze.getMAByHistoricalData(p2List, period);
        double p3MA = MAAnalyze.getMAByHistoricalData(p3List, period);
        double p4MA = MAAnalyze.getMAByHistoricalData(p4List, period);


        //if ( p0Data.getClose() >= d0MA && p1Data.getClose() >= p1MA && p2Data.getClose() >= p2MA && p3Data.getClose() >= p3MA ){
        if (p0Data.getClose() >= d0MA) {
            direction = TradeDirection.BUY;
        } else if (p0Data.getClose() < d0MA && p1Data.getClose() < p1MA && p2Data.getClose() < p2MA) {
            direction = TradeDirection.SELL;
        }

        return direction;
    }


    public static void main(String[] args) {
//        String stock = "600030";
//
//        MAStrategy maStrategy = BeanContext.getBean(MAStrategy.class);
//        MarketService marketService = BeanContext.getBean(MarketService.class);
//        MarketDataService dataService = BeanContext.getBean(MarketDataService.class);
//
//
//        marketService.updateMarketData(stock, -100);
//
//        List<HistoricalData> datas = dataService.getMarketHistoricalData(stock, TimePeriod.DAILY);
//        System.out.println("size: " + datas.size());
//
//        double marketPrice = 0;
//        Date date = DateUtils.toMarketDate("2017-08-24");
//
//        TradingBook tradingBook = new TradingBook(stock, marketPrice);
//        MarketContext info = new MarketContext(tradingBook, date, TimeWindow.getLastMonths(TimePeriod.DAILY, -1), TimePeriod.DAILY, marketPrice);
//        maStrategy.execute(info);
    }
}
