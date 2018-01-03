package com.vs.common.domain;

import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.domain.vo.MarkToMarket;
import com.vs.common.domain.vo.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;

/**
 * Created by erix-mac on 15/8/16.
 */
@Data
@Builder
@AllArgsConstructor
@Slf4j
public class TradingBook extends AbstractBook implements Comparable<TradingBook> {

    public TradingBook(String code, double marketPrice) {
        this(new Stock(code), marketPrice, 0);
    }

    public TradingBook(Stock stock, double marketPrice, double capital) {
        this.stock = stock;
        this.markToMarket = new MarkToMarket(stock, marketPrice, capital);
    }

    public PnL getPnL(){
        return this.calculatePnL(this.markToMarket.getMarketPrice());
    }

    public void bookTrade(TradeAction action, Position p ) {
        long positions = p.getPosition();
        Transaction tran = new Transaction(action.getTradeDate(), action.getTradeDirection(), stock, action.getStrategy(), action.getTradePrice(), positions);
        DecimalFormat f = new DecimalFormat("0.##");

        if ( action.getTradeDirection().equals(TradeDirection.SELL) && (positions > this.getPositions()) ){
            tran.setPositions(this.getPositions());
        }

        String DETAIL_STR = "|%7s|%10s|Strategy:%23s|%5s|Positions:%6s|Cost:%10s|Price:%7s|Capital:%10s|";
        if ( positions > 0 && validate(tran) ) {
            log.info("---- Make Transaction :" + String.format(DETAIL_STR,action.getStock().getCode(), action.getTradeDate(),action.getStrategy(),action.getTradeDirection().toString(),positions,f.format(action.getTradePrice() * positions),f.format(action.getTradePrice()),f.format(this.markToMarket.getAvailCapital())));

            this.markToMarket.setPyramidPosition(p);
            this.book(tran, false);
        }
    }

    @Override
    public int compareTo(TradingBook o) {
        if ( o == null )
            return -1;

        return (this.getPnL().getTotalProfitPercentage() - o.getPnL().getTotalProfitPercentage()) > 0 ? 1 : -1;

    }
}
