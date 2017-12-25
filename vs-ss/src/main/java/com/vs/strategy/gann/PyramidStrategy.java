package com.vs.strategy.gann;


import com.vs.common.domain.TradeAction;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.domain.vo.PyramidPosition;
import com.vs.strategy.PositionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by erix-mac on 15/10/18.
 */
@Slf4j
@Component
public class PyramidStrategy implements PositionStrategy {

    public static double[] BUY_PYRAMID = {0.5, 0.3, 0.2};
    public static double[] SELL_PYRAMMID = {1};


    public static void updatePyramidArray(double[] buy, double[] sell){
        BUY_PYRAMID = buy;
        SELL_PYRAMMID = sell;
    }

    @Override
    public PyramidPosition execute(TradingBook tradingBook, TradeAction action) {

        PyramidPosition pyramid = (PyramidPosition)((PyramidPosition)tradingBook.getMarkToMarket().getPyramidPosition()).clone();
        int nextIndex = this.nextIndex(action.getTradeDirection(), pyramid);
        double ratio = this.nextPyramidValue(action.getTradeDirection(), nextIndex);
        long position = action.getTradeDirection().equals(TradeDirection.BUY) ? (long)((tradingBook.getMarkToMarket().getTotalCapital() * ratio) / action.getTradePrice()) : (long)(tradingBook.getPositions() * ratio);

        pyramid.setIndex(nextIndex);
        pyramid.setDirection(action.getTradeDirection());
        pyramid.setPositions(position);
        //log.info(">>>>>>> Pyramid : " + action.getTradeDirection().toString() + " positions: " + position + " Pos%: " +  new DecimalFormat("0.##").format(position * action.getTradePrice()/trade.getTotalCapital() *100) + "% of Total Capital: " + trade.getTotalCapital());
        return pyramid;
    }


    private double nextPyramidValue(TradeDirection tradeDirection, int index) {
        if ( index < 0 )
            return 0;

        return tradeDirection.equals(TradeDirection.BUY) ? BUY_PYRAMID[index] : SELL_PYRAMMID[index];
    }

    private int nextIndex(TradeDirection tradeDirection, PyramidPosition p) {
        if (tradeDirection.equals(p.getDirection())) {
            int next = p.getIndex() + 1;
            int length = tradeDirection.equals(TradeDirection.BUY) ? BUY_PYRAMID.length : SELL_PYRAMMID.length;

            return next <= length -1 ? next : -1;
        } else {
            return 0;
        }
    }

}
