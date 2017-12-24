package com.vs.service.regression;

import com.google.common.collect.Lists;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.service.trade.TraderService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Created by erix-mac on 16/7/19.
 */
@Slf4j
public class RegressionThread implements Callable<RegressionResult> {
    @Setter
    private TraderService traderService;
    @Setter
    private CountDownLatch countDownLatch;

    private TimeWindow timeWindow;
    private double capital;

    public RegressionThread(TimeWindow time, double capital){
        this.timeWindow = time;
        this.capital = capital;
    }

    @Override
    public RegressionResult call() throws Exception {
        List<TradingBook> tradingBooks = Lists.newArrayList();

        try{
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> RegressionThread Timewindow BEGIN: " + timeWindow.toString());
            tradingBooks = traderService.autoTrade(timeWindow, capital);
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> RegressionThread Timewindow END: " + timeWindow.toString());

        }finally {
            this.countDownLatch.countDown();
        }

        return new RegressionResult(timeWindow, tradingBooks);
    }
}
