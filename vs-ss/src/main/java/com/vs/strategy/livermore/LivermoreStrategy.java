package com.vs.strategy.livermore;


import com.vs.common.domain.TradeAction;
import com.vs.strategy.AbstractStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.domain.MarketContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by erix-mac on 2017/9/9.
 */
@Slf4j
@Component
public class LivermoreStrategy extends AbstractStrategy implements Strategy {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<TradeAction> execute(MarketContext context) {
        return null;
    }
}
