package com.vs.strategy.gann;

import com.google.common.collect.Lists;
import com.vs.common.domain.*;
import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.utils.MarketDataUtils;
import com.vs.repository.MarketDataRepository;
import com.vs.strategy.AbstractStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.domain.MarketContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 15/9/23.
 */
@Slf4j
@Component
public class KeepTransactionProfitStrategy extends AbstractStrategy implements Strategy {

    final static double MAX_PROFIT_RAIO = 8;
    final static double MAX_KEEP_PROFIT_RAIO = 4;

    final static double PROFIT_RAIO = 5;
    final static double KEEP_PROFIT_RAIO = 1.5;

    @Override
    public String getName() {
        return Strategies.KeepTransactionProfitStrategy.toString();
    }

    @Override
    public List<TradeAction> execute(MarketContext context) {
        List<TradeAction> result = Lists.newArrayList();

        Stock stock = context.getStock();
        LocalDate date = context.getAnalysisDate();
        TradingBook tradingBook = context.getTradingBook();

        if (tradingBook.getPositions() == 0)
            return result;


        List<Transaction> transactions = tradingBook.getTransactions();

        for (Transaction trans : transactions) {

            if (trans.getDate().isAfter(date) || !trans.getDirection().equals(TradeDirection.BUY) || trans.isClosed()) {
                continue;
            }


            if (triggerKeepProfit(stock, date, trans)) {
                //log.info("++++++++  triggerKeepProfit : " + trans.toString());
                TradeAction action = new TradeAction(Strategies.KeepTransactionProfitStrategy, TradeDirection.SELL, stock, date, date);

                action.setTradeDate(date);
                action.setTradePrice(context.getMarketPrice());
                action.setNettingHandled(true);
                trans.setClosed(true);
                trans.setCloseAction(action);

                result.add(action);
            }
        }

        return result;
    }

    private boolean triggerKeepProfit(Stock stock, LocalDate date, Transaction trans) {

        if (trans.isClosed() || trans.getDate().equals(date) || trans.getPositions() <= 0)
            return false;

        LocalDate cur = trans.getDate().plusDays(1);

        LocalDate end = date;
        boolean maxProfitFlag = false;
        boolean minProfitFlag = false;

        while (cur.isBefore(end) || cur.equals(end)) {
            LocalDate marketDate = cur;
            HistoricalData market = MarketDataRepository.getMarketDataBy(stock.getCode(), marketDate);

            if (!MarketDataUtils.isTradingDate(market.getDate())) {
                cur = cur.plusDays(1);
                continue;
            }

            double maxProfit = trans.getProfitPercentage(market.getHigh());

            if (maxProfit >= MAX_PROFIT_RAIO && !maxProfitFlag) {
                maxProfitFlag = true;
            } else if (maxProfit >= PROFIT_RAIO && !minProfitFlag) {
                minProfitFlag = true;
            }


            if (maxProfitFlag || minProfitFlag) {
                market = MarketDataRepository.getMarketDataBy(stock.getCode(), date);
                //this.marketService.getMarketHistoricalData(stock.getCode(), TimePeriod.DAILY, date);
                double minProfit = trans.getProfitPercentage(market.getLow());

                if (maxProfitFlag && (minProfit > 0 && minProfit <= MAX_KEEP_PROFIT_RAIO)) {
                    return true;
                } else if (minProfitFlag && (minProfit > 0 && minProfit <= KEEP_PROFIT_RAIO)) {
                    return true;
                } else {
                    return false;
                }
            }

            cur = cur.plusDays(1);
        }

        return false;
    }
}
