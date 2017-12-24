package com.vs.service.trade;

import com.vs.common.domain.TradingBook;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Created by erix-mac on 15/10/1.
 */
@Slf4j
public class TradeTaskThread implements Callable<TradingBook> {

    @Setter
    private TradeManager tradeManager;
    @Setter
    private CountDownLatch countDownLatch;

    public TradeTaskThread(TradeManager manager){
        this.tradeManager = manager;
    }


    @Override
    public TradingBook call() throws Exception {
        TradingBook tradingBook = new TradingBook(tradeManager.getStock(), -1, tradeManager.getTotalCapital());
        try{
            log.info(">>>>>>>>>>>>>>Thread TradeTask Begin : Thread ID: " + Thread.currentThread().getId() + " stock: " + tradeManager.getStock().getCode());
            tradingBook = this.tradeManager.trade();
        }catch (Throwable e){
            log.error(">>>>>>>> ERROR: " + tradeManager.getStock().getCode() + ": " + e.toString());
            e.printStackTrace();
        }
        finally {
            this.countDownLatch.countDown();
            log.info(">>>>>>>>>>>>>>Thread CountDownLatch : ThreadID: " + Thread.currentThread().getId() + " Count: " + this.countDownLatch.getCount() + " stock: " + tradeManager.getStock().getCode());
        }

        return tradingBook;
    }
}
