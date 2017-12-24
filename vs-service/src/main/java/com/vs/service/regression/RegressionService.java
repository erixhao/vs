package com.vs.service.regression;

import com.google.common.collect.Lists;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.BeanContext;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.PerformanceUtils;
import com.vs.service.report.TradeReport;
import com.vs.service.report.Valuations;
import com.vs.service.trade.TraderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.vs.common.domain.HistoricalData.toMarketDate;


/**
 * Created by erix-mac on 16/7/19.
 */
@Service
public class RegressionService {
    private final static long capital = 100000;

    private final static String STRESS_BEGIN_DATE = "2006-01-01";
    private final static String STRESS_END_DATE   = "2016-01-01";
    private final static int STRESS_TIMES = 10;
    private final static int STRESS_TIME_WINDOW = -12; // 1 Year



    private final static TimeWindow full_2015 = new TimeWindow(toMarketDate("2014-01-01"), toMarketDate("2015-09-01"), TimePeriod.DAILY);
    private final static TimeWindow full_2016 = new TimeWindow(toMarketDate("2015-01-01"), toMarketDate("2016-01-26"), TimePeriod.DAILY);
    private final static TimeWindow bull_2014 = new TimeWindow(toMarketDate("2014-01-01"), toMarketDate("2015-05-01"), TimePeriod.DAILY);


    private final static TimeWindow full_2015_callapse = new TimeWindow(toMarketDate("2015-05-01"), toMarketDate("2015-09-01"), TimePeriod.DAILY);
    private final static TimeWindow bear_2015_callapse = new TimeWindow(toMarketDate("2015-06-01"), toMarketDate("2015-09-01"), TimePeriod.DAILY);

    private final static TimeWindow full_2007 = new TimeWindow(toMarketDate("2005-02-15"), toMarketDate("2008-12-20"), TimePeriod.DAILY);
    private final static TimeWindow bear_2008 = new TimeWindow(toMarketDate("2007-10-15"), toMarketDate("2008-12-20"), TimePeriod.DAILY);
    private final static TimeWindow bull_2007 = new TimeWindow(toMarketDate("2005-02-15"), toMarketDate("2007-10-20"), TimePeriod.DAILY);
    private final static TimeWindow full_10_years = new TimeWindow(toMarketDate("2005-02-15"), toMarketDate("2017-08-20"), TimePeriod.DAILY);

    private final static TimeWindow bull_2009 = new TimeWindow(toMarketDate("2009-01-15"), toMarketDate("2010-01-20"), TimePeriod.DAILY);
    private final static TimeWindow window_2016 = new TimeWindow(toMarketDate("2016-01-01"), toMarketDate("2016-12-31"), TimePeriod.DAILY);
    private final static TimeWindow window_20166 = new TimeWindow(toMarketDate("2016-07-01"), toMarketDate("2016-12-31"), TimePeriod.DAILY);
    private final static TimeWindow window_20163 = new TimeWindow(toMarketDate("2016-10-01"), toMarketDate("2016-12-31"), TimePeriod.DAILY);
    private final static TimeWindow window_20161 = new TimeWindow(toMarketDate("2016-12-01"), toMarketDate("2016-12-31"), TimePeriod.DAILY);


    private List<TimeWindow> timeWindows = Lists.newArrayList(full_2016, full_2015, full_2007, bear_2008, bull_2014, bull_2007);
    //private List<TimeWindow> timeWindows = Lists.newArrayList(bull_2007);

    private List<TimeWindow> callapse_timeWindows = Lists.newArrayList(full_2015_callapse, bear_2015_callapse, bear_2008);

    //private TimeWindow[] timeWindows = {full_2016,full_2015};

    @Autowired
    private TraderService traderService;

    public List<RegressionResult> regression() {
        return regression(timeWindows);
    }


    public List<RegressionResult> regression(List<TimeWindow> windows) {
        List<RegressionResult> trades = Lists.newArrayList();

        ExecutorService executorService = Executors.newFixedThreadPool(timeWindows.size());
        List<Future<RegressionResult>> futureList = Lists.newArrayList();
        final CountDownLatch countDownLatch = new CountDownLatch(timeWindows.size());

        Date beginDate = new Date();
        long begin = PerformanceUtils.beginTime("regression");
        try {
            for (TimeWindow t : windows) {
                RegressionThread regression = new RegressionThread(t, capital);
                regression.setTraderService(this.traderService);
                regression.setCountDownLatch(countDownLatch);

                Future<RegressionResult> trade = executorService.submit(regression);
                futureList.add(trade);
            }

            countDownLatch.await();

            for (Future<RegressionResult> t : futureList) {
                RegressionResult r = t.get();
                if (r != null && r.getTradingBooks().size() > 0) {
                    trades.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }

        this.printRegressionReport(trades);

        long cost = PerformanceUtils.endTime("regression", begin);
        System.out.println(">>>>>>>>>>>>>>>>  Time Begin: " + beginDate);
        System.out.println(">>>>>>>>>>>>>>>>  Time End: " + new Date());
        return trades;
    }


    public List<RegressionResult>  stress() {
        Date begin = DateUtils.toMarketDate(STRESS_BEGIN_DATE);
        Date end   = DateUtils.toMarketDate(STRESS_END_DATE);

        List<TimeWindow> windows = Lists.newArrayList();
        for ( int i = 0; i < STRESS_TIMES; i++ ){
            Date randomDate = DateUtils.randomDate(begin,end);
            windows.add(TimeWindow.getTimeWindow(TimePeriod.DAILY,randomDate,0,STRESS_TIME_WINDOW,0));
        }
        return regression(windows);
    }


    private void printRegressionReport(List<RegressionResult> trades) {

        for (RegressionResult r : trades) {
            TradeReport.printTradeResult(r.getTimeWindow(), r.getTradingBooks());
        }
    }


    public static void main(String[] args) {
        RegressionService regressionService = BeanContext.getBean(RegressionService.class);

        long begin = PerformanceUtils.beginTime(">>>>>>>>>>>>>>>>>>>>>>>>>>  Regression Begin");
        List<RegressionResult> res = regressionService.regression();
        //List<RegressionResult> res = regressionService.stress();


        Valuations.calcuate(res);
        PerformanceUtils.endTime(">>>>>>>>>>>>>>>>>>>>>>>>>>  Regression Begin", begin);

        System.exit(0);
    }
}
