package com.vs.strategy;


import com.vs.common.domain.vo.PyramidPosition;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.Order;

/**
 * Created by erix-mac on 15/10/20.
 */
public interface PositionStrategy {

    public static final long FIXED_POSITIONS = 800;

    PyramidPosition analysis(TradingBook tradingBook, Order action);
}
