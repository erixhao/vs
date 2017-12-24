package com.vs.service.trade;

import com.google.common.collect.Lists;
import com.vs.common.domain.*;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.domain.vo.*;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.MarketDataUtils;
import com.vs.market.MarketDataService;
import com.vs.strategy.StrategeService;
import com.vs.strategy.Strategy;
import com.vs.strategy.domain.Dividends;
import com.vs.strategy.domain.TradeContext;
import com.vs.strategy.gann.PyramidStrategy;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by erix-mac on 16/2/20.
 */
@Slf4j
@Data
public class TradeManager {
    private Stock stock;
    private TimeWindow timeWindow;
    private TimePeriod period;
    private double totalCapital;

    @Setter
    private StrategeService strategeService;
    @Setter
    private MarketDataService marketService;
    @Setter
    private PyramidStrategy pyramidStrategy;
    @Setter
    private Dividends dividends;


    public TradeManager(Stock stock, TimeWindow timeWindow, TimePeriod period, double totalCapital){
        this.stock = stock;
        this.timeWindow = timeWindow;
        this.period = period;
        this.totalCapital = totalCapital;
    }

    public TradingBook trade(){
        TradingBook tradingBook = new TradingBook(stock, -1, totalCapital);

        Date begin = timeWindow.getBegin();
        Date end = timeWindow.getEnd();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);

        final ExecutorService STRATEGY_EXE_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        while ( calendar.getTime().before(this.timeWindow.getEnd()) || calendar.getTime().equals(this.timeWindow.getEnd()) ) {
            Date marketDate = calendar.getTime();

            if (! MarketDataUtils.isTradingDate(marketDate)) {
                calendar.add(Calendar.DATE, 1);
                continue;
            }

            HistoricalData market = this.marketService.getMarketHistoricalData(stock.getCode(), period, marketDate);
            if ( !stock.isIndex() && dividends.isSplitDate(stock, marketDate) && (tradingBook.getPositions() > 0) ){
                log.info("++++++++++++ Before Split Date, Adjust Dividends Split : " + stock.getCode() + " : " + HistoricalData.toMarketDate(marketDate) + " Positions: " + tradingBook.getPositions());
                dividends.adjustTradeDividendsSplit(tradingBook, marketDate,market);
                log.info("++++++++++++ After Split Date, Adjust Dividends Split : " + stock.getCode() + " : " + HistoricalData.toMarketDate(marketDate) + " Positions: " + tradingBook.getPositions());
            }

            List<Order> actions = this.executeStrategy(STRATEGY_EXE_SERVICE, marketDate, tradingBook, timeWindow, market);
            if ( actions.size() > 0 ){
                this.executeTradeAction(tradingBook, actions);
            }

            calendar.add(Calendar.DATE, 1);
        }

        this.updateMarketEndPrice(tradingBook, end);

        return tradingBook;
    }

    private void updateMarketEndPrice(TradingBook tradingBook, Date end){
        if ( MarketDataUtils.isTradingDate(end) ){
            double endMarket = getTradeEndMarket(stock.getCode(), end);
            if ( endMarket > 0 ){
                tradingBook.getMarkToMarket().setMarketPrice(endMarket);
            }
        }
    }

    private double getTradeEndMarket(String code, Date end ){
        if ( DateUtils.isToday(end) && DateUtils.isTradingPriceAvaiable()){
            LiveData live = this.marketService.getMarketLiveData(code);
            return (live == null ? -1 : live.getCurrentPrice());
        }else{
            HistoricalData endMarket = this.marketService.getMarketHistoricalData(stock.getCode(), period, end);

            return (endMarket == null ? -1 : endMarket.getClose());

        }

    }


    private List<Order> executeStrategy(final ExecutorService executorService, Date date, TradingBook tradingBook, TimeWindow timeWindow, HistoricalData market) {
        List<Order> orders = Lists.newArrayList();

        if ( market == null )
            return orders;

        List<? extends Strategy> strategies = this.strategeService.getTradingStrategys();
        tradingBook.getMarkToMarket().setMarketPrice(market.getClose());

        final List<Future<List<Order>>> futureList = Lists.newArrayList();
        final CountDownLatch countDownLatch = new CountDownLatch(strategies.size());
        final TradeContext info = new TradeContext(tradingBook,date, timeWindow, market.getPeriod(), market.getClose());
        try {
            for (Strategy s : strategies) {
                final StrategyTaskThread task = new StrategyTaskThread(s, info);
                task.setCountDownLatch(countDownLatch);

                Future<List<Order>> futureAction = executorService.submit(task);
                futureList.add(futureAction);
            }

            countDownLatch.await();

            for (Future<List<Order>> t : futureList) {
                List<Order> actions = t.get();
                if (actions != null && actions.size() > 0) {
                    orders.addAll(actions);
                }
            }
        } catch (Throwable  e) {
            log.error("ERROR: " + e.toString());
        } finally {
            //STRATEGY_EXE_SERVICE.shutdown();
            //log.info("ExecutorService Shutdown");

        }


        return Order.merge(orders, this.strategeService.isAndOption());
    }

    private List<Order> executeStrategy2(Date date, TradingBook tradingBook, TimeWindow timeWindow, HistoricalData market) {
        List<Order> orders = Lists.newArrayList();

        if ( market == null )
            return orders;

        List<? extends Strategy> strategies = this.strategeService.getTradingStrategys();
        tradingBook.getMarkToMarket().setMarketPrice(market.getClose());
        for (Strategy s : strategies) {
            List<Order> actions = s.analysis(new TradeContext(tradingBook,date, timeWindow, market.getPeriod(), market.getClose()));
            if ( actions  != null && actions.size() > 0 ) {
                orders.addAll(actions);
            }
        }

        return Order.merge(orders);
    }


    private boolean executeTradeAction(TradingBook tradingBook, List<Order> actions){

        for ( Order action : actions ) {

            PyramidPosition p = this.pyramidStrategy.analysis(tradingBook, action);
            if ( p.getPositions() <= 0 && action.getTradeDirection().equals(TradeDirection.SELL) ) {
                action.setTradeDirection(TradeDirection.SHORT);
            }

            //trade.setPyramidPosistion(p);
            tradingBook.bookTrade(action, p);
        }

        return true;
    }


}
