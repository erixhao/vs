package com.vs.strategy;

import com.vs.common.domain.TradeAction;
import com.vs.strategy.domain.MarketContext;

import java.util.List;

/**
 * Created by erix-mac on 15/8/20.
 * Git-Hub Test
 */
public interface Strategy {
    String getName();
    List<TradeAction> execute(final MarketContext context);
}
