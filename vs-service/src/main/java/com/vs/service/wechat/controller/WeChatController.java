package com.vs.service.wechat.controller;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.PropertieUtils;
import com.vs.market.DownloadExecutor;
import com.vs.market.DownloadTask;
import com.vs.repository.MarketDataRepository;
import com.vs.service.wechat.domain.WeChats;
import com.vs.service.wechat.domain.vo.Authentication;
import com.vs.service.wechat.domain.vo.TradeResult;
import com.vs.service.wechat.service.EODService;
import com.vs.service.wechat.service.TradeCacheService;
import com.vs.service.wechat.service.WeChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by erix-mac on 16/6/16.
 */
@Slf4j
@RestController
public class WeChatController {

    private final static String WECHAT_MESSAGE = "Hello WeChat from Venus!";
    private final static String WECHAT_TOKEN = "venus";
    private final static String DEFAULT_TEST_STOCK = "600104";

    @Autowired
    private WeChats weChats;
    @Autowired
    private WeChatService wechatService;
    @Autowired
    private EODService eodService;
    @Autowired
    private TradeCacheService tradeCacheService;


    @RequestMapping(value = "/wechat", method = {RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public void doAuthenticate(Authentication weChat, PrintWriter out) {
        log.info("Venus doAuthenticate for WeChat echo: " + weChat.getEchostr());

        out.print(weChat.getEchostr());
        out.flush();
        out.close();
    }

    @RequestMapping(value = "/wechat", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    public void doMessagePost(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("Venus doMessagePost from WeChat ");
        weChats.processWeChatMessage(request, response);
    }


    @RequestMapping("/testStock")
    public String checkStock() {
        return this.wechatService.processUserAction(DEFAULT_TEST_STOCK);
    }

    @RequestMapping("/checkStock/{code}")
    public String checkStock(@PathVariable String code) {
        return this.wechatService.processUserAction(code);
    }

    @RequestMapping("/checkStocks/{codes}")
    public String checkStocks(@PathVariable String codes) {
        String[] stocks = codes.split(",");
        for (String s : stocks) {
            this.wechatService.processUserAction(s);
        }

        return "successful";
    }

    @RequestMapping("/time")
    public String time() {
        StringBuilder sb = new StringBuilder();
        sb.append("Date: " + new Date().toString());
        Calendar cal = Calendar.getInstance();

        TimeZone timeZone = cal.getTimeZone();

        sb.append("TimeZone ID: " + timeZone.getID()).append(timeZone.getDisplayName());

        log.info(sb.toString());
        return sb.toString();
    }

    @RequestMapping("/refreshStock")
    public String refreshMarket() {
        DownloadExecutor.downloadAll();

        return "successful";
    }

    @RequestMapping("/refreshStock/{code}")
    public String refreshMarket(@PathVariable String code) {
        DownloadTask.downloadHistoryDataTask(code, LocalDate.now());
        return "successful";
    }

    @RequestMapping("/refreshStocks/{codes}")
    public String refreshMarkets(@PathVariable String codes) {
        DownloadExecutor.loadAllMarketData(Lists.newArrayList(codes.split(",")));
        return "successful";
    }

    @RequestMapping("/refresChinaMarket")
    public String refreshSpecifiedMarket() {
        // sync wechat user's input
        DownloadExecutor.loadAllMarketData(PropertieUtils.getWechatMarketList());
//        marketDataDownloader.downloadMarketData(PropertieUtils.getWechatMarketList(), TimePeriod.DAILY, WeChatService.MKT_DAYS, false);
        // sync stock pool
        DownloadExecutor.loadAllMarketData(PropertieUtils.getChinaMarketList());
//        marketDataDownloader.downloadMarketData(PropertieUtils.getChinaMarketList(), TimePeriod.DAILY, WeChatService.MKT_DAYS, false);
        return "successful";
    }

    @RequestMapping("/checkMarketData/{code}")
    public String checkMarketData(@PathVariable String code) {
        List<HistoricalData> dataList = MarketDataRepository.getAllMarketDataBy(code);
        return dataList == null ? "NULL" : dataList.size() + ":" + DateUtils.toString(dataList.get(dataList.size() - 1).getDate());
    }

    @RequestMapping("/refreshMarketFromDB")
    public String refreshAllMarketDataFromDB() {
//        this.marketDataService.refreshAllMarketDataFromDB();
        return "successful";
    }

    @RequestMapping("/checkMarketCount")
    public String checkMarketCacheCount() {
        int size = 0;//this.marketDataService.size(TimePeriod.DAILY);
        long total = 0;//this.marketDataService.totalSize();

        log.info("checkMarketCacheCount Stock : " + size + " Total Data :" + total);

        return "Total Market Data in Cache Stock : " + size + " Total Data: " + total;
    }

    @RequestMapping("/checkTradeCount")
    public String checkTradCacheCount() {
        int size = this.tradeCacheService.size();
        log.info("checkTradCacheCount : " + size);

        return "Total Trades Data in Cache: " + size;
    }

    @RequestMapping("/checkTradeCount/{code}")
    public String checkTradCacheCount(@PathVariable String code) {
        TradeResult result = this.tradeCacheService.get(code);
        log.info("check Trade in Cache  : " + code + " Result: " + (result == null ? "NULL" : result.toString()));

        return "Trade in Cache  : " + code + " Result: " + (result == null ? "NULL" : result.toString());
    }

    @RequestMapping("/checkTradeCache")
    public String checkTradCache() {
        Map<String, TradeResult> cache = this.tradeCacheService.getTradeCache();
        StringBuilder sb = new StringBuilder("Trade Cache: ");
        for (String key : cache.keySet()) {
            TradeResult r = cache.get(key);
            sb.append("\r\nKey: " + key + " Cache: " + r.getTradeDate());
        }

        return sb.toString();
    }

    @RequestMapping("/eod")
    public String triggerEOD() {
        this.eodService.process();
        return "successful";
    }

    @RequestMapping("/eod/{code}")
    public String triggerEOD(@PathVariable String code) {
        this.eodService.process(code);
        return "successful";
    }

    public static void main(String[] args) {


    }

}
