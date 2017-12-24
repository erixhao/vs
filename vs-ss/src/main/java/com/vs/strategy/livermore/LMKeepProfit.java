package com.vs.strategy.livermore;

import com.google.common.collect.Lists;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.Order;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.domain.enums.TradeStrategy;
import com.vs.strategy.domain.TradeContext;
import com.vs.strategy.AbstractTradeStrategy;
import com.vs.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 16/5/7.
 */
@Slf4j
@Component
public class LMKeepProfit extends AbstractTradeStrategy implements Strategy {

    private final static double PROFIT_100_DOUBLE = 100;


    @Override
    public String getName() {
        return TradeStrategy.LivermoreKeepProfit.toString();
    }

    @Override
    public List<Order> analysis(TradeContext info) {
        List<Order> result = Lists.newArrayList();

        Stock stock = info.getStock();
        Date date = info.getAnalysisDate();
        TradingBook tradingBook = info.getTradingBook();

        if (tradingBook.getPositions() == 0)
            return result;

        double currentProfit = tradingBook.getPnL().getTotalProfitPercentage();
        boolean isDoubled = currentProfit >= PROFIT_100_DOUBLE;

        if ( isDoubled ) {

            Order action = new Order(TradeStrategy.LivermoreKeepProfit, TradeDirection.SELL, stock, date, date);

            action.setTradeDate(date);
            action.setTradePrice(info.getMarketPrice());
            action.setNettingHandled(false);

            long positions = (long)((tradingBook.getPnL().getProfit() / 2.0) / info.getMarketPrice());
            //action.setPositions(positions);

            System.out.println("#####$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$  Livermore Keep Profit : " + positions);
        }

        return result;
    }
}
