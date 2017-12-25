package com.vs.strategy;


import com.vs.common.domain.TradeAction;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.vo.PyramidPosition;

/**
 * Created by erix-mac on 15/10/20.
 */
public interface PositionStrategy {
    PyramidPosition execute(final TradingBook tradingBook, TradeAction action);
}
