package com.vs.service.trade;

import com.google.common.collect.Lists;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.PerformanceUtils;
import com.vs.common.utils.PropertieUtils;
import com.vs.market.MarketDataService;
import com.vs.strategy.StrategeService;
import com.vs.strategy.domain.Dividends;
import com.vs.strategy.gann.PyramidStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by erix-mac on 15/9/5.
 */
@Service
@Slf4j
public class TraderService {
    private static final List<Stock> STOCKS = PropertieUtils.getStockList();
    private static final List<Stock> stocks34 = Lists.newArrayList(new Stock("000001"));
    //private static final List<Stock> stocks = Lists.newArrayList(new Stock("600577"), new Stock("600036"),new Stock("600030"));

    private static final List<Stock> stocks3 = Lists.newArrayList(new Stock("600577"), new Stock("600030"));

    @Getter @Setter
    @Autowired
    private StrategeService strategeService;
    @Autowired
    private MarketDataService marketService;
    @Autowired
    private PyramidStrategy pyramidStrategy;
    @Autowired
    private Dividends dividends;


    public List<TradingBook> autoTrade(TimeWindow time, double totalCapital, boolean mustWinStratege ) {
        if ( mustWinStratege ){
            strategeService.setAndOption(mustWinStratege);
        }
        return this.autoTrade(time, time.getPeriod(), totalCapital);
    }

    public List<TradingBook> autoTrade(TimeWindow time, double totalCapital) {
        return this.autoTrade(time, time.getPeriod(), totalCapital);
    }

    public List<TradingBook> autoTrade(TimeWindow timeWindow, TimePeriod period, double totalCapital) {
        return autoTrade(null, timeWindow, period, totalCapital);
    }


    public List<TradingBook> autoTrade(List<Stock> stock, TimeWindow timeWindow, TimePeriod period, double totalCapital) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> autoTrade Timewindow BEGIN: " + timeWindow.toString());
        List<TradingBook> tradingBooks = Lists.newArrayList();

        long beginTime = PerformanceUtils.beginTime("autoTrade");
        List<Stock> stocks = (stock == null || stock.size() == 0) ? STOCKS : stock;
        final ExecutorService TRADE_EXE_SERVICE = Executors.newFixedThreadPool(stocks.size());

        final List<Future<TradingBook>> futureList = Lists.newArrayList();
        final CountDownLatch countDownLatch = new CountDownLatch(stocks.size());

        try {
            for (Stock s : stocks) {
                final TradeManager m = new TradeManager(s, timeWindow, period, totalCapital);
                m.setMarketService(marketService);
                m.setStrategeService(strategeService);
                m.setPyramidStrategy(pyramidStrategy);
                m.setDividends(dividends);

                final TradeTaskThread task = new TradeTaskThread(m);
                task.setCountDownLatch(countDownLatch);

                Future<TradingBook> trade = TRADE_EXE_SERVICE.submit(task);
                futureList.add(trade);
            }

            PerformanceUtils.tick("count down latch await");
            countDownLatch.await();
            PerformanceUtils.tick("count down latch await end ");

            for (Future<TradingBook> t : futureList) {
                TradingBook tradingBook = t.get();
                if (tradingBook != null && tradingBook.hasTransaction()) {
                    tradingBooks.add(tradingBook);
                }
            }
        } catch (Throwable  e) {
            log.error("ERROR: " + e.toString());
        } finally {
            TRADE_EXE_SERVICE.shutdown();
            log.info("ExecutorService Shutdown");

        }

        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> autoTrade Timewindow END: " + timeWindow.toString());
        PerformanceUtils.endTime("autoTrade",beginTime);
        PerformanceUtils.tick("all done end ");

        return tradingBooks;
    }


}
