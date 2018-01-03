package com.vs.strategy.livermore;

import com.google.common.collect.Lists;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradeAction;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.strategy.AbstractStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.domain.MarketContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 16/5/7.
 */
@Slf4j
@Component
public class LMKeepProfit extends AbstractStrategy implements Strategy {

    private final static double PROFIT_100_DOUBLE = 100;


    @Override
    public String getName() {
        return Strategies.LivermoreKeepProfit.toString();
    }

    @Override
    public List<TradeAction> execute(MarketContext context) {
        List<TradeAction> result = Lists.newArrayList();

        Stock stock = context.getStock();
        LocalDate date = context.getAnalysisDate();
        TradingBook tradingBook = context.getTradingBook();

        if (tradingBook.getPositions() == 0)
            return result;

        double currentProfit = tradingBook.getPnL().getTotalProfitPercentage();
        boolean isDoubled = currentProfit >= PROFIT_100_DOUBLE;

        if ( isDoubled ) {

            TradeAction action = new TradeAction(Strategies.LivermoreKeepProfit, TradeDirection.SELL, stock, date, date);

            action.setTradeDate(date);
            action.setTradePrice(context.getMarketPrice());
            action.setNettingHandled(false);

            long positions = (long)((tradingBook.getPnL().getProfit() / 2.0) / context.getMarketPrice());
            //action.setPositions(positions);

            System.out.println("#####$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$  Livermore Keep Profit : " + positions);
        }

        return result;
    }
}
