package com.vs.common.domain.enums;

/**
 * Created by erix-mac on 15/9/13.
 */
public enum Strategies {

    NONE(-1),StopLossStrategy(0), HighLowMoveStrategy(1), KeepTransactionProfitStrategy(2),KeepTradeProfitStrategy(3), PercentageStrategy(4), TopBottomStrategy(5),
    OptimisticHLMoveStrategy(6), MarketTrendStrategy(7), MACDStrategy(50),MAStrategy(51),
    LivermoreKeepProfit(100);

    private int value;

    private Strategies(int value){
        this.value = value;
    }
}
