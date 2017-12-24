package com.vs.strategy.livermore;


import com.vs.common.domain.Order;
import com.vs.strategy.domain.TradeContext;
import com.vs.strategy.AbstractTradeStrategy;
import com.vs.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by erix-mac on 2017/9/9.
 */
@Slf4j
@Component
public class LivermoreStrategy extends AbstractTradeStrategy implements Strategy {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<Order> analysis(TradeContext info) {
        return null;
    }
}
