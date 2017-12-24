package com.vs.strategy;

import com.vs.common.domain.Order;
import com.vs.strategy.domain.TradeContext;

import java.util.List;

/**
 * Created by erix-mac on 15/8/20.
 * Git-Hub Test
 */
public interface Strategy {
    String getName();
    List<Order> analysis(final TradeContext info);
}
