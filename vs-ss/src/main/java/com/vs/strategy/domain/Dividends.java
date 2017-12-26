package com.vs.strategy.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.Transaction;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.utils.MarketDataUtils;
import com.vs.common.utils.PropertieUtils;
import com.vs.market.MarketDataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by erix-mac on 15/9/14.
 */
@Data
@Component
@Slf4j
public class Dividends {
    private static MarketDataService marketDataService;
    private static Map<Stock, List<Date>> stockSplitsDate;


    @Autowired(required = true)
    public void setMarketDataService(MarketDataService marketService) {
        marketDataService = marketService;
    }

    private static Map<Stock, List<Date>> getStockSplitsDate(){
        if ( stockSplitsDate == null ){
            stockSplitsDate = extractStockSplitDates(null);
        }

        return stockSplitsDate;
    }

    private static synchronized Map<Stock, List<Date>> extractStockSplitDates(Stock stock) {
        Map<Stock, List<Date>> map = Maps.newConcurrentMap();
        List<Stock> stocks = Lists.newArrayList();

        if (stock == null) {
            stocks = PropertieUtils.getStockList();
        } else {
            stocks.add(stock);
        }

        for (Stock s : stocks) {
            List<Date> dates = findSplitDates(s);
            map.put(s, dates);
        }

        return map;
    }

    private static List<Date> getSplitDate(Stock stock) {
        return getStockSplitsDate().get(stock);
    }

    public static boolean isSplitDate(Stock stock, Date transDate) {
        return getSplitDate(stock) == null ? false : getSplitDate(stock).contains(transDate);

    }


    public static List<Double> adjustDividendsSplit(Stock stock, Date divideDate, HistoricalData market, List<Double> prices) {
        double raio = getSplitRaio(stock, market.getOpen(), divideDate);

        if ( raio == 0 )
            return prices;

        List<Double> adjPrices = Lists.newArrayList();

        for (Double price : prices){
            adjPrices.add(price/raio);
        }

        return adjPrices;
    }


    public void adjustTradeDividendsSplit(TradingBook tradingBook, Date divideDate, HistoricalData market) {
        if ( tradingBook.getPositions() <=0 )
            return;

        for (Transaction tran : tradingBook.getTransactions()) {
            //if (!tran.isClosed() && tran.getDirection().equals(TradeDirection.BUY) && tran.getDate().before(divideDate)) {
            if ( tran.getDate().before(divideDate)) {
            //log.info("++++++++++++++++++++++++ adjustTradeDividendsSplit, before tran: " + tran.toString());
                double raio = getSplitRaio(tradingBook.getStock(), market.getOpen(), divideDate);

                tran.setPositions((long) (tran.getPositions() * raio));
                tran.setNetPositions((long) (tran.getNetPositions() * raio));
                tran.setPrice(tran.getPrice() / raio);
                tran.setDividendsSplit(true);
                //log.info("++++++++++++++++++++++++ adjustTradeDividendsSplit, after tran: " + tran.toString() );

            }
        }
    }

    private Date getLatestSplitDate(Stock stock, Date transDate) {
        List<Date> splitDates = getSplitDate(stock);

        for (Date s : splitDates) {
            if (transDate.equals(s) || transDate.before(s))
                return s;
        }

        return null;
    }

    private static List<Date> findSplitDates(Stock stock) {
        List<Date> splitDates = Lists.newArrayList();

        List<HistoricalData> markets = marketDataService.getMarketHistoricalData(stock.getCode(), TimePeriod.DAILY);

        if (markets == null)
            return splitDates;

        Collections.sort(markets);

        int last = markets.size() - 1;
        for (int i = 1; i <= last -1; i++) {

            HistoricalData d0 = markets.get(i);
            HistoricalData p1 = markets.get(i - 1);

            if (d0 == null || p1 == null || !MarketDataUtils.isTradingDate(d0) || !MarketDataUtils.isTradingDate(p1))
                continue;

            double d0Open = d0.getOpen();
            double p1Close = p1.getClose();
            // (20 - 10)/20 = 0.5
            double delta = (d0Open - p1Close) / p1Close;
            if (Math.abs(delta) >= 0.20) {
                splitDates.add(d0.getDate());
            }

        }

        return splitDates;
    }

    private static double getSplitRaio(Stock stock, double openPrice, Date splitDate) {

        if (openPrice <= 0)
            return 1;

        List<HistoricalData> datas = marketDataService.getMarketHistoricalData(stock.getCode(), TimePeriod.DAILY);
        HistoricalData p1 = MarketDataUtils.getMarketT(datas, splitDate, -1);

        return p1.getClose() / openPrice;
    }
}
