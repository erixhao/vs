package com.vs.service.wechat.controller;

import com.google.common.collect.Lists;
import com.vs.common.utils.PropertieUtils;
import com.vs.market.DownloadExecutor;
import com.vs.service.wechat.service.EODService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Created by erix-mac on 2016/12/23.
 */
@Slf4j
@RestController
public class MarketScheduler {
    @Autowired
    private EODService eodService;

    @Scheduled(cron="0 10 15 ? * MON-FRI", zone= "Asia/Shanghai")
    public void schedule(){
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> schedule trigger: refresh: " + new Date().toString());
        DownloadExecutor.downloadAll();
    }

    @Scheduled(cron="0 05 15 ? * MON-FRI", zone= "Asia/Shanghai")
    public void scheduleMarketIndex(){
        String index = PropertieUtils.getMarketProperty("market.index");
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> schedule trigger: refresh: market Index : " + index + " on :" + new Date().toString());
        if ( !StringUtils.isEmpty(index) ){
            DownloadExecutor.loadAllMarketData(Lists.newArrayList(index.split(",")));
//            marketDataDownloader.downloadMarketData(Lists.newArrayList(index.split(",")), TimePeriod.DAILY, -1, false);
        }
    }

    @Scheduled(cron="0 30 15 ? * MON-FRI", zone= "Asia/Shanghai")
    public void scheduleCalcEODProcess(){
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> schedule trigger: scheduleCalcEODProcess: " + new Date().toString());
        this.eodService.process();
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> schedule end: scheduleCalcEODProcess: " + new Date().toString());
    }
}
