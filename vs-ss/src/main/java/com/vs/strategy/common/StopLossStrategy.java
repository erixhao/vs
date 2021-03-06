package com.vs.strategy.common;

import com.google.common.collect.Lists;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradeAction;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.Transaction;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.utils.PropertieUtils;
import com.vs.strategy.AbstractStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.domain.MarketContext;
import com.vs.strategy.index.IndexTrendStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 15/8/22.
 */
@Slf4j
@Component
public class StopLossStrategy extends AbstractStrategy implements Strategy {

    final static String STOP_LOSS_PERCETAGE_KEY = "trade.transaction.stoploss";
    final double DEFAULT_STOPLOSS_PERCENTAGE = Double.parseDouble(PropertieUtils.getMarketProperty(STOP_LOSS_PERCETAGE_KEY));

    final static double BULL_RATIO = 2;

    @Autowired
    IndexTrendStrategy indexTrendStrategy;
    @Autowired
    private MarketTrendAnalyze marketTrendAnalyze;

    @Override
    public String getName() {
        return Strategies.StopLossStrategy.toString();
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
        BullBear stockTrend = marketTrendAnalyze.analysisTrend(stock.getCode(), date);

        for (Transaction tran : transactions) {

            if (tran.getDate().isAfter(date) || !tran.getDirection().equals(TradeDirection.BUY) || tran.isClosed()) {
                continue;
            }

            double stopLossPercentage = stockTrend.isBigBull() ? DEFAULT_STOPLOSS_PERCENTAGE * BULL_RATIO : DEFAULT_STOPLOSS_PERCENTAGE;
            double stopLossPrice = tran.getPrice() * (100 + stopLossPercentage) / 100;
            double currentProfit = tran.getProfitPercentage(context.getMarketPrice());

            //boolean isStopLoss = trend.isBull() ? trade.getProfit().getCurrProfitPercentage() <= stopLossPercentage : currentProfit <= stopLossPercentage;

            if (currentProfit <= stopLossPercentage) {
                //System.out.println(">>>>>>  Stock: " + stockTrend.toString());
                //System.out.println("STOP : currentProfit: " + currentProfit + " StopLoss: " + stopLossPercentage);
                TradeAction action = new TradeAction(Strategies.StopLossStrategy, TradeDirection.SELL, stock, date, date);

                action.setTradeDate(date);
                action.setTradePrice(stopLossPrice);
                action.setNettingHandled(true);

                tran.setClosed(true);
                tran.setCloseAction(action);

                result.add(action);
            }
        }

        return result;
    }
}
