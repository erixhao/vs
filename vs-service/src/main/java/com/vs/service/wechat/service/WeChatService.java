package com.vs.service.wechat.service;

import com.google.common.collect.Lists;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.Transaction;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.MarketIndex;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.BeanContext;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.PerformanceUtils;
import com.vs.market.MarketService;
import com.vs.service.report.TradeReport;
import com.vs.service.trade.TraderService;
import com.vs.service.wechat.domain.WeChatAction;
import com.vs.service.wechat.domain.vo.CacheSignal;
import com.vs.service.wechat.domain.vo.TradeResult;
import com.vs.service.wechat.domain.vo.WeChatResponse;
import com.vs.strategy.common.MarketTrendAnalyze;
import com.vs.strategy.index.IndexTrendStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by erix-mac on 16/7/1.
 */
@Slf4j
@Service
public class WeChatService {
    final static long capital = 50000;
    //public final static int MKT_DAYS = -300 * 3;
    public final static int MKT_DAYS = -750;
    final static int TRADING_MONTHS = -12;

    @Autowired
    TraderService traderService;
    @Autowired
    private MarketService marketService;
    @Autowired
    private MarketTrendAnalyze marketTrendAnalyze;
    @Autowired
    private TradeCacheService tradeCacheService;
    @Autowired
    private IndexTrendStrategy indexTrendStrategy;

    public String processUserAction(String action) {
        WeChatAction weChatAction = WeChatAction.parseUserAction(action.trim().toUpperCase());
        final Date date = new Date();
        TradeResult result = new TradeResult(false, false, date, weChatAction.toString() + " N/A",-1);
        String key = action;

        if (tradeCacheService.isCached(key, new Date())) {
            return tradeCacheService.get(key).getResponse();
        }

        switch (weChatAction) {
            case STOCK:
                result = this.processStockAction(action);
                break;
            case LIVE_STOCK:
                STOCK:
                result = this.processStockAction(action);
                break;
            case INDEX:
                result = this.processStockAction(action);
                break;
            case ABB_INDEX:
                result = this.processStockAction(MarketIndex.parseByAbbreviation(action).getSinaCode());
                break;
            case TREND_INDEX:
                result = this.processTrendAction(MarketIndex.parseByAbbreviation(weChatAction.getCode()).getSinaCode());
                break;
            case TREND_STOCK:
                result = this.processTrendAction(weChatAction.getCode());
                break;
            case RECOMMEND:
                result = this.processRecommend();
            case REGRESSION:
                break;
            case INVALID:
                result = new TradeResult(false, false, date, WeChatResponse.generateInvalidInput(),-1);
                break;
        }

        tradeCacheService.put(key, result);
        //System.out.println(result);
        return result.getResponse();
    }

    private TradeResult processRecommend() {
        Map<String, TradeResult> random = this.tradeCacheService.randomTradeCache(5, CacheSignal.BUY);
        String suggestion = WeChatResponse.generateSmartSuggestion(random);

        return new TradeResult(true,false,new Date(),suggestion, -1);
    }


    private TradeResult processStockAction(String code) {
        Date reportWindow = DateUtils.nextMonths(TRADING_MONTHS);
        TimeWindow tradWindow = TimeWindow.getLastMonths(TimePeriod.DAILY, TRADING_MONTHS);
        System.out.println(tradWindow.toShortString());

        long p1 = PerformanceUtils.beginTime("updateData");
        this.marketService.updateMarketData(code, MKT_DAYS);

        long p2 = PerformanceUtils.endTime("updateData", p1);

        List<TradingBook> result = this.traderService.autoTrade(Lists.newArrayList(new Stock(code)), tradWindow, TimePeriod.DAILY, capital);
        List<Transaction> todayTrans = TradeReport.filterTransactions(result, reportWindow);
        Collections.sort(todayTrans);

        long p3 = PerformanceUtils.endTime("autoTrade", p2);

        BullBear trend = this.marketTrendAnalyze.analysisTrend(code, new Date());
        Map<MarketIndex, BullBear> idxTrend = this.indexTrendStrategy.analysisAllIndex(new Date());
        Map<String, TradeResult> randomTrade = this.tradeCacheService.randomTradeCache(3, CacheSignal.TRADE);
        Map<String, TradeResult> randomProfit = this.tradeCacheService.randomTradeCache(5, CacheSignal.GREAT_PROFIT);

        long p4 = PerformanceUtils.endTime("trendAnaysis", p3);

        return new WeChatResponse().toResponse(code, todayTrans, result, trend, idxTrend, randomTrade, randomProfit);
    }

    private TradeResult processTrendAction(String code) {
        TimeWindow window = TimeWindow.getLastMonths(TimePeriod.DAILY, TRADING_MONTHS);
        this.marketService.updateMarketData(code, MKT_DAYS);
        return new TradeResult(true, false, new Date(),
                this.marketTrendAnalyze.marketTrendRegression(code, window.getBegin(), window.getEnd(), TimePeriod.MONTHLY),-1
        );
    }

    public static void main(String[] args) {
        WeChatService weChatService = BeanContext.getBean(WeChatService.class);

       /* IndexTrendStrategy indexTrendStrategy = BeanContext.getBean(IndexTrendStrategy.class);

        Map<StockIndex, BullBear> idxTrend = indexTrendStrategy.analysisAllIndex(new Date());

        for ( BullBear t : idxTrend.values() ){
            System.out.println(t.toString());
        }
*/

        //String response = weChatService.processUserAction("6");
        //System.out.println("Response : " + response);

        long begin = PerformanceUtils.beginTime("weChatService.processUserAction");

        //weChatService.processUserAction("000518");
        //weChatService.processUserAction("600137");
        String response = weChatService.processUserAction("600030");
        //weChatService.processUserAction("SH");
        //weChatService.processUserAction("TD:GR");
        //weChatService.processUserAction("TD:600137");
        //weChatService.processUserAction("000d002");

        //MarketDataDownloader marketDataDownloader = BeanContext.getBean(MarketDataDownloader.class);
        // sync wechat user's input
        //marketDataDownloader.downloadMarketData(PropertieUtils.getWechatMarketList(), TimePeriod.DAILY, WeChatService.MKT_DAYS, false);
        // sync stock pool
        //marketDataDownloader.downloadMarketData(PropertieUtils.getChinaMarketList(), TimePeriod.DAILY, WeChatService.MKT_DAYS, false);
        System.out.println("Response : " + response);

        PerformanceUtils.endTime("weChatService.processUserAction", begin);
    }
}
