package com.vs.market;


import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.utils.BeanContext;
import com.vs.common.utils.FileUtils;
import com.vs.common.utils.PropertieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.vs.common.utils.Constants.MARKET_FILE_LOCATION;

/**
 * Created by erix-mac on 15/8/3.
 */
@Slf4j
@Service
public class MarketDataDownloader {
    @Autowired
    private MarketService marketService;

    private final static int MAX_DOWNLOAD_THREAD_COUNT = 20;
    private final static int SLEEP_SECONDS = 3 * 1000;

    public void downloadAllDividends(){
        this.downloadMarketData(null, -1, true);
    }

    public void downloadAllMarketData(){
        this.downloadMarketData(TimePeriod.DAILY, -1, false);
    }

    public void downloadMarketData(TimePeriod timePeriod, int historyDays, boolean isDividends){
        downloadMarketData(PropertieUtils.getStockCodeList(), timePeriod, historyDays, isDividends);
    }


    public void downloadMarketData(List<String> stockList, TimePeriod timePeriod, int historyDays, boolean isDividends){
        int count = 0;
        for ( String stock : stockList ){
            MarketDataDownloadTask task = MarketDataDownloadTask.newTask(stock, stock, timePeriod, historyDays, FileUtils.getCSVFileName(MARKET_FILE_LOCATION, stock, timePeriod, isDividends), isDividends);
            task.setMarketService(this.marketService);
            new Thread(task).start();
            count ++;

            if ( count >= MAX_DOWNLOAD_THREAD_COUNT ){
                try {
                    System.out.println("**************************** WALL STREET NEVER SLEEP " + SLEEP_SECONDS / 1000 + "s *****************************");
                    Thread.sleep(SLEEP_SECONDS);
                } catch (InterruptedException e) {
                    log.error(e.toString());
                }finally {
                    count = 0;
                }
            }
        }
    }



    public void syncMarketData(){
        List<String> stockList = this.marketService.getAllExistingCodes();

        this.downloadMarketData(stockList, TimePeriod.DAILY, -1, false);
    }

    public static void main(String args[]) {
        MarketDataDownloader marketDataService = BeanContext.getBean(MarketDataDownloader.class);
        //marketDataService.downloadMarketData(TimePeriod.DAILY, -30 * 25, false);
        marketDataService.downloadMarketData(TimePeriod.DAILY, -1, false);

        //marketDataService.syncMarketData();
        System.out.println(">>>>>>>>>>>>>>>>>>  SYNC END >>>>>>>>>>>>>>>>>>>>>>>>.");
        //marketDataService.downloadMarketData(SHSZStock.getAllSHStock(),TimePeriod.DAILY,-200,false);
        //marketDataService.downloadMarketData(Lists.<String>newArrayList("600507"),TimePeriod.DAILY,-180,false);

    }

}
