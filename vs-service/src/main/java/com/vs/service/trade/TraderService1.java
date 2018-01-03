package com.vs.service.trade;

import com.google.common.collect.Lists;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.PropertieUtils;
import com.vs.strategy.StrategyService;
import com.vs.strategy.domain.Dividends;
import com.vs.strategy.gann.PyramidStrategy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by erix-mac on 15/9/5.
 */
@Service
@Slf4j
public class TraderService1 {
    private static final List<Stock> stocks = PropertieUtils.getStockList();
    private static final List<Stock> stocks333 = Lists.newArrayList(new Stock("600577"), new Stock("600036"),new Stock("600030"));
    private static final List<Stock> stocks3343 = Lists.newArrayList(new Stock("600577"));
    private static final List<Stock> stocks5 = Lists.newArrayList(new Stock("000001,399001"));
    private static final List<Stock> stocks343 = Lists.newArrayList(new Stock("600577"));
    private static final List<Stock> stocks34 = Lists.newArrayList(new Stock("600036"));

    private static final List<Stock> stocks99 = Lists.newArrayList(new Stock("600577"), new Stock("600030"));


    private static final List<Stock> stocks3 = Lists.newArrayList(new Stock("000002"));


    @Autowired
    private StrategyService strategyService;
    @Autowired
    @Getter
    private PyramidStrategy pyramidStrategy;
    @Autowired
    private Dividends dividends;

    public List<TradingBook> autoTrade(TimeWindow time, double totalCapital) {
        return this.autoTrade(time,time.getPeriod(), totalCapital);
    }


    public List<TradingBook> autoTrade(TimeWindow timeWindow, TimePeriod period, double totalCapital) {
        List<TradingBook> tradingBooks = Lists.newArrayList();

        TradeManager manager = new TradeManager(null, timeWindow, period, totalCapital);
        manager.setStrategyService(strategyService);
        manager.setPyramidStrategy(pyramidStrategy);
        manager.setDividends(dividends);

        for ( Stock stock : stocks ) {
            manager.setStock(stock);
            TradingBook tradingBook = manager.trade();
            if ( tradingBook.hasTransaction() ) {
                tradingBooks.add(tradingBook);
            }
        }

        return tradingBooks;
    }

}
