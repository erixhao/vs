package com.vs.service.trade;

import com.google.common.collect.Lists;
import com.vs.common.domain.PnL;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.Transaction;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.BeanContext;
import com.vs.common.utils.Constants;
import com.vs.common.utils.PerformanceUtils;
import com.vs.common.utils.PropertieUtils;
import com.vs.market.MarketService;
import com.vs.strategy.analysis.MarketMovementAnalyze;
import com.vs.strategy.common.MarketTrendAnalyze;
import com.vs.strategy.domain.MarketBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.vs.common.domain.HistoricalData.toMarketDate;


/**
 * Created by erix-mac on 15/10/27.
 */

@Slf4j
@Service
public class DailyBatch {

    public final static long capital = 50000;
    public final static int MKT_DAYS = -300 * 3;

    private final static String FILE_TRANSACTION_LOCATOIN = Constants.TRADE_FILE_DAILY_REPORT_LOCATION;

    @Autowired
    TraderService traderService;
    @Autowired
    MarketMovementAnalyze marketMoveAnalyze;
    @Autowired
    private MarketTrendAnalyze marketTrendAnalyze;
    @Autowired
    private MarketService marketService;


    public static void main(String[] args) {
        log.info("Daily Report Starting....");

        DailyBatch dailyBatch = BeanContext.getBean(DailyBatch.class);

        long begin = PerformanceUtils.beginTime("Daily Batch");
        dailyBatch.runDailyTradeAnalysis();
        PerformanceUtils.endTime("Daily Batch", begin);

        log.info("Daily Report End.");

    }

    public void runDailyTradeAnalysis() {
        Date today = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, -30*1);
        // report all T-7 to T trade trans
        Date reportWindow = calendar.getTime();

        //TimeWindow tradWindow = new TimeWindow(toMarketDate("2016-04-26"),today,TimePeriod.DAILY);
        //TimeWindow tradWindow = new TimeWindow(toMarketDate("2017-01-01"),today,TimePeriod.DAILY);

        //TimeWindow tradWindow = new TimeWindow(toMarketDate("2016-03-01"),toMarketDate("2017-08-25"),TimePeriod.DAILY);

        TimeWindow tradWindow = TimeWindow.getLastMonths(TimePeriod.DAILY,-3);

        List<Transaction> todayTrans = Lists.newArrayList();
        List<String> codes = PropertieUtils.getStockCodeList();
        this.marketService.updateMarketData(codes,MKT_DAYS);

        long begin = PerformanceUtils.beginTime("autoTrade");
        List<TradingBook> result = this.traderService.autoTrade(tradWindow, capital,false);
        PerformanceUtils.endTime(">>>>>>>>> END autoTrade",begin);


        System.out.println("\n\n\n TRADE REPORT  BEGIN");
        for (TradingBook tradingBook : result) {
            List<Transaction> trans = tradingBook.getTradedTransactions();
            for (Transaction tran : trans) {
                if (tran.getDate().after(reportWindow)) {
                    todayTrans.add(tran);
                }
            }
        }
        Collections.sort(todayTrans);
        printTradingTransationReport(todayTrans);
        System.out.println("TRADE REPORT  END");
        printTradeResult(tradWindow, result);

        //HtmlTradeReport.generateHTML(result);
        //HtmlTradeReport.generateTradeDetailHTMLReport(result);

        System.exit(0);
    }

    private void printTradingTransationReport(List<Transaction> trans) {
        String LINE = "----------------------------------------------------------------------";
        String DETAIL_STR = "|%10s|%8s|%8s|%8s|%8s|%12s|%8s|%4s";
        System.out.println(LINE);
        System.out.println(String.format(DETAIL_STR, "Trade Date","Stock","Action","Price ","Pos  ","Curr Profit%","Profit%","Trend"));
        System.out.println(LINE);

        DecimalFormat format = new DecimalFormat("0.##");

        for (Transaction t : trans) {
            BullBear trend = this.marketTrendAnalyze.analysisTrend(t.getStock().getCode(), t.getDate());

            System.out.println(String.format(DETAIL_STR, toMarketDate(t.getDate()), t.getStock().getCode(), t.getDirection(), format.format(t.getPrice()),
                    t.getPositions(),format.format(t.getCurrentTradePnL().getCurrProfitPercentage()) + "%",
                    format.format(t.getCurrentTradePnL().getTotalProfitPercentage()) + "%",
                    trend.toString()
            ));
        }
        System.out.println(LINE);

    }

     private void printTradeResult(TimeWindow window, List<TradingBook> tradingBooks) {
        Collections.sort(tradingBooks, Collections.<TradingBook>reverseOrder());

        System.out.println("\n >>>>>>>>>>>>>> >>>>>>>>>>>>>>   Trade " + window.toString() + ">>>>>>>>>>>>>> >>>>>>>>>>>>>>: ");
        if (tradingBooks == null || tradingBooks.size() == 0) {
            System.out.println(" >>>>>>>>>>>>>> NO Trade Action ! >>>>>>>>>>>>>>: ");
            return;
        }
        DecimalFormat f = new DecimalFormat("0.##");
        String DETAIL_STR = "|%6s|%10s|%10s|%8s|%8s|%8s|%8s|%10s|%10s|%8s|";
        System.out.println("---------------------------------------------------------------");
        System.out.println(String.format(DETAIL_STR, "Stock","Profit%","Curr/Profit%","Profit ","Price ","Pos ","Trans","NonStr Date","Non Price","NonStr Profit%"));
        //System.out.println(String.format(DETAIL_STR, "股票","利润%","总利润%","利润 ","市价 ","头寸 ","交易"));
        System.out.println("---------------------------------------------------------------");
        Date today = Calendar.getInstance().getTime();

        for (TradingBook t : tradingBooks) {
            /*if ( t.isAllShortTransactions() )
                continue;*/

            PnL p = t.getPnL();
            MarketBase np = this.marketMoveAnalyze.calcuate(t.getStock().getCode(),window);
            System.out.println(String.format(DETAIL_STR, t.getStock().getCode(), f.format(p.getTotalProfitPercentage()) + "%",f.format(p.getCurrProfitPercentage()) + "%",  f.format(p.getProfit()) , t.getMarkToMarket().getMarketPrice(), t.getPositions(), t.getTransactions().size(), "(" + toMarketDate(np.getBeginDate()) + "-" + toMarketDate(np.getEndDate()) + ")", "[" + np.getBeginPrice() + " - " + np.getEndPrice() + "]", f.format(np.getProfitPercentage()) + "%"));
        }
        System.out.println("---------------------------------------------------------------");
        System.out.println(" >>>>>>>>>>>>>> >>>>>>>>>>>>>>   Trade >>>>>>>>>>>>>> >>>>>>>>>>>>>>");
    }
}
