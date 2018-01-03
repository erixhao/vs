package com.vs.service.trade;

import com.google.common.collect.Lists;
import com.vs.common.domain.*;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.domain.vo.PyramidPosition;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.MarketDataUtils;
import com.vs.repository.MarketDataRepository;
import com.vs.strategy.Strategy;
import com.vs.strategy.StrategyService;
import com.vs.strategy.domain.Dividends;
import com.vs.strategy.domain.MarketContext;
import com.vs.strategy.gann.PyramidStrategy;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
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
    private StrategyService strategyService;
    @Setter
    private PyramidStrategy pyramidStrategy;
    @Setter
    private Dividends dividends;


    public TradeManager(Stock stock, TimeWindow timeWindow, TimePeriod period, double totalCapital) {
        this.stock = stock;
        this.timeWindow = timeWindow;
        this.period = period;
        this.totalCapital = totalCapital;
    }

    public TradingBook trade() {
        TradingBook tradingBook = new TradingBook(stock, -1, totalCapital);

        LocalDate begin = timeWindow.getBegin();
        LocalDate end = timeWindow.getEnd();

        LocalDate cur = begin;

        final ExecutorService STRATEGY_EXE_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        while (cur.isBefore(this.timeWindow.getEnd()) || cur.equals(this.timeWindow.getEnd())) {
            LocalDate marketDate = cur;

            if (!MarketDataUtils.isTradingDate(marketDate)) {
                cur = cur.plusDays(1);
                continue;
            }

            HistoricalData market = MarketDataRepository.getMarketDataBy(stock.getCode(), marketDate);
            // /this.marketService.getMarketHistoricalData(stock.getCode(), period, marketDate);
            if (!stock.isIndex() && dividends.isSplitDate(stock, marketDate) && (tradingBook.getPositions() > 0)) {
                log.info("++++++++++++ Before Split Date, Adjust Dividends Split : " + stock.getCode() + " : " + DateUtils.toString(marketDate) + " Positions: " + tradingBook.getPositions());
                dividends.adjustTradeDividendsSplit(tradingBook, marketDate, market);
                log.info("++++++++++++ After Split Date, Adjust Dividends Split : " + stock.getCode() + " : " + DateUtils.toString(marketDate) + " Positions: " + tradingBook.getPositions());
            }

            List<TradeAction> actions = this.executeStrategy(STRATEGY_EXE_SERVICE, marketDate, tradingBook, timeWindow, market);
            if (actions.size() > 0) {
                this.executeTradeAction(tradingBook, actions);
            }

            cur = cur.plusDays(1);
        }

        this.updateMarketEndPrice(tradingBook, end);

        return tradingBook;
    }

    private void updateMarketEndPrice(TradingBook tradingBook, LocalDate end) {
        if (MarketDataUtils.isTradingDate(end)) {
            double endMarket = getTradeEndMarket(stock.getCode(), end);
            if (endMarket > 0) {
                tradingBook.getMarkToMarket().setMarketPrice(endMarket);
                tradingBook.getMarkToMarket().setMarketDate(end);
            }
        }
    }

    private double getTradeEndMarket(String code, LocalDate end) {
        if (LocalDate.now().equals(end) && DateUtils.isTradingPriceAvaiable()) {
            LiveData live = null;//TODO need erix confirm//this.marketService.getMarketLiveData(code);
            return (live == null ? -1 : live.getCurrentPrice());
        } else {
            HistoricalData endMarket = MarketDataRepository.getMarketDataBy(stock.getCode(), end);
            //this.marketService.getMarketHistoricalData(stock.getCode(), period, end);

            return (endMarket == null ? -1 : endMarket.getClose());

        }

    }


    private List<TradeAction> executeStrategy(final ExecutorService executorService, LocalDate date, TradingBook tradingBook, TimeWindow timeWindow, HistoricalData market) {
        List<TradeAction> tradeActions = Lists.newArrayList();

        if (market == null)
            return tradeActions;

        List<? extends Strategy> strategies = this.strategyService.getTradingStrategys();
        tradingBook.getMarkToMarket().setMarketPrice(market.getClose());

        final List<Future<List<TradeAction>>> futureList = Lists.newArrayList();
        final CountDownLatch countDownLatch = new CountDownLatch(strategies.size());
        final MarketContext info = new MarketContext(tradingBook, date, timeWindow, TimePeriod.NONE, market.getClose());
        try {
            for (Strategy s : strategies) {
                final StrategyTaskThread task = new StrategyTaskThread(s, info);
                task.setCountDownLatch(countDownLatch);

                Future<List<TradeAction>> futureAction = executorService.submit(task);
                futureList.add(futureAction);
            }

            countDownLatch.await();

            for (Future<List<TradeAction>> t : futureList) {
                List<TradeAction> actions = t.get();
                if (actions != null && actions.size() > 0) {
                    tradeActions.addAll(actions);
                }
            }
        } catch (Throwable e) {
            log.error("ERROR: " + e.toString());
        } finally {
            //STRATEGY_EXE_SERVICE.shutdown();
            //log.info("ExecutorService Shutdown");

        }


        return TradeAction.merge(tradeActions, this.strategyService.isAndOption());
    }

    private List<TradeAction> executeStrategy2(LocalDate date, TradingBook tradingBook, TimeWindow timeWindow, HistoricalData market) {
        List<TradeAction> tradeActions = Lists.newArrayList();

        if (market == null)
            return tradeActions;

        List<? extends Strategy> strategies = this.strategyService.getTradingStrategys();
        tradingBook.getMarkToMarket().setMarketPrice(market.getClose());
        for (Strategy s : strategies) {
            List<TradeAction> actions = s.execute(new MarketContext(tradingBook, date, timeWindow, TimePeriod.NONE, market.getClose()));
            if (actions != null && actions.size() > 0) {
                tradeActions.addAll(actions);
            }
        }

        return TradeAction.merge(tradeActions);
    }


    private boolean executeTradeAction(TradingBook tradingBook, List<TradeAction> actions) {

        for (TradeAction action : actions) {

            PyramidPosition p = this.pyramidStrategy.execute(tradingBook, action);
            if (p.getPositions() <= 0 && action.getTradeDirection().equals(TradeDirection.SELL)) {
                action.setTradeDirection(TradeDirection.SHORT);
            }

            //trade.setPyramidPosistion(p);
            tradingBook.bookTrade(action, p);
        }

        return true;
    }


}
