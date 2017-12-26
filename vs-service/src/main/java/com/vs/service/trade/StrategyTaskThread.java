package com.vs.service.trade;

import com.google.common.collect.Lists;
import com.vs.common.domain.TradeAction;
import com.vs.strategy.Strategy;
import com.vs.strategy.domain.MarketContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Created by erix-mac on 2017/4/4.
 */
@Slf4j
public class StrategyTaskThread implements Callable<List<TradeAction>> {

    @Setter
    private Strategy strategy;
    @Setter
    private MarketContext marketContext;
    @Setter
    private CountDownLatch countDownLatch;

    public StrategyTaskThread(){
    }

    public StrategyTaskThread(Strategy strategy, MarketContext info){
        this.strategy = strategy;
        this.marketContext = info;
    }


    @Override
    public List<TradeAction> call() throws Exception {
        List<TradeAction> actions = Lists.newArrayList();
        try{
            //log.info(">>>>>>>>>>>>>>Thread StrategyTaskThread Begin : Thread ID: " + Thread.currentThread().getId() + " stock: " + tradeAnalysisInfo.getStock().getName() + " Date: " + tradeAnalysisInfo.getAnalysisDate().toString() + " Strategy : " +  strategy.getName() );
            actions = this.strategy.execute(marketContext);
            //System.out.println("####### -> action: " + actions.toString());
        }catch (Throwable e){
            log.error(">>>>>>>> ERROR: " + marketContext.getStock().getCode() + ": " + e.toString());
            e.printStackTrace();
        }
        finally {
            this.countDownLatch.countDown();
            //log.info(">>>>>>>>>>>>>>Thread CountDownLatch : ThreadID: " + Thread.currentThread().getId() + " Count: " + this.countDownLatch.getCount() + " stock: " + tradeAnalysisInfo.getStock().getCode());
        }

        return actions;
    }
}
