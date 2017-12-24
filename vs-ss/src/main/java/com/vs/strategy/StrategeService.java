package com.vs.strategy;

import com.google.common.collect.Lists;
import com.vs.strategy.common.MarketTrendStrategy;
import com.vs.strategy.common.StopLossStrategy;
import com.vs.strategy.gann.*;
import com.vs.strategy.indicators.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by erix-mac on 15/9/5.
 *
 */
@Service
@Scope("prototype")
public class StrategeService {

    @Getter @Setter
    private boolean andOption = false;

    @Autowired @Getter
    private HighLowMoveStrategy highLowMoveStrategy;
    @Autowired @Getter
    private StopLossStrategy stopLossStrategy;
    @Autowired @Getter
    private KeepTradeProfitStrategy keepTradeProfitStrategy;
    @Autowired @Getter
    private KeepTransactionProfitStrategy keepTransactionProfitStrategy;
    @Autowired @Getter
    private PercentageStrategy percentageStrategy;
    @Autowired @Getter
    private TopBottomStrategy topBottomStrategy;//
    @Autowired @Getter
    private MarketTrendStrategy marketTrendStrategy;
    @Autowired @Getter
    private MACDStrategy macdStrategy;
    @Autowired @Getter
    private MAStrategy maStrategy;
    @Autowired @Getter
    private HighLowMoveOptimisticStrategy highLowMoveOptimisticStrategy;


    @Setter @Getter
    private List<? extends Strategy> strategieList;
    @Setter @Getter
    private List<? extends Strategy> mustWinSrategieList;

    @PostConstruct
    private void init(){
        strategieList = Lists.newArrayList(highLowMoveStrategy, keepTradeProfitStrategy,stopLossStrategy);
        mustWinSrategieList = Lists.newArrayList(highLowMoveStrategy,maStrategy,stopLossStrategy);

    }

    public List<? extends Strategy> getTradingStrategys(){
        return andOption ? mustWinSrategieList : strategieList;
    }
}
