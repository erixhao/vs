package com.vs.service.trade;

import com.google.common.collect.Lists;
import com.vs.common.domain.Order;
import com.vs.strategy.domain.TradeContext;
import com.vs.strategy.Strategy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Created by erix-mac on 2017/4/4.
 */
@Slf4j
public class StrategyTaskThread implements Callable<List<Order>> {

    @Setter
    private Strategy strategy;
    @Setter
    private TradeContext tradeContext;
    @Setter
    private CountDownLatch countDownLatch;

    public StrategyTaskThread(){
    }

    public StrategyTaskThread(Strategy strategy, TradeContext info){
        this.strategy = strategy;
        this.tradeContext = info;
    }


    @Override
    public List<Order> call() throws Exception {
        List<Order> actions = Lists.newArrayList();
        try{
            //log.info(">>>>>>>>>>>>>>Thread StrategyTaskThread Begin : Thread ID: " + Thread.currentThread().getId() + " stock: " + tradeAnalysisInfo.getStock().getName() + " Date: " + tradeAnalysisInfo.getAnalysisDate().toString() + " Strategy : " +  strategy.getName() );
            actions = this.strategy.analysis(tradeContext);
            //System.out.println("####### -> action: " + actions.toString());
        }catch (Throwable e){
            log.error(">>>>>>>> ERROR: " + tradeContext.getStock().getCode() + ": " + e.toString());
            e.printStackTrace();
        }
        finally {
            this.countDownLatch.countDown();
            //log.info(">>>>>>>>>>>>>>Thread CountDownLatch : ThreadID: " + Thread.currentThread().getId() + " Count: " + this.countDownLatch.getCount() + " stock: " + tradeAnalysisInfo.getStock().getCode());
        }

        return actions;
    }
}
