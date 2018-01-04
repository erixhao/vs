package com.vs.market;

import com.google.common.collect.Iterables;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.dao.utility.DataAccessService;
import com.vs.repository.MarketDataRepository;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

public class DownloadExecutorTest {
//    @Test
//    public void loadAll(){
//        DownloadExecutor.downloadAll();
//    }

//    @Test
//    public void loadAllMarketData() {
//        List<Stock> stockList = DataAccessService.findAll(Stock.class);
//
//        String stockCode = Iterables.getFirst(stockList, null).getCode();
//        DownloadTask.downloadHistoryDataTask(stockCode);
//
//        List<HistoricalData> allMarketData = MarketDataRepository.getAllMarketDataBy(stockCode);
//        Assert.assertNotEquals(0, allMarketData.size());
//    }

//    @Test
//    public void loadAllStockData() {
//        DownloadExecutor.loadAllStockData();
//
//        List<Stock> stockList = DataAccessService.findAll(Stock.class);
//        Assert.assertNotEquals(0, stockList.size());
//    }
}