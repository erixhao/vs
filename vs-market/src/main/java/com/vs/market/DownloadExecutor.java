package com.vs.market;


import com.google.common.collect.Lists;
import com.vs.common.domain.Stock;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.utils.BeanContext;
import com.vs.dao.utility.DataAccessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by erix-mac on 15/8/3.
 */
@Slf4j
@Service
public class DownloadExecutor {

    private final static int MAX_DOWNLOAD_THREAD_COUNT = 20;
    private final static int SLEEP_SECONDS = 3 * 1000;

    ExecutorService es = Executors.newFixedThreadPool(1);

    public static void loadAllMarketData(List<String> stockList) {
        for (String stock : stockList) {
            DownloadTask.downloadHistoryDataTask(stock);
        }
    }

    public static void loadAllStockData() {
        DownloadTask.downloadStockTask();
    }

    public static void downloadAll() {
//        DownloadTask.downloadStockTask();

        List<Stock> stockList = DataAccessService.findAll(Stock.class);

        for (Stock stock : stockList) {
            DownloadTask.downloadHistoryDataTask(stock.getCode());
        }
    }
}
