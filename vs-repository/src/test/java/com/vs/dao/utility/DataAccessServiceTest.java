package com.vs.dao.utility;

import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DataAccessServiceTest {

    @Test
    public void findAll() throws Exception {
        DataAccessService.save(Stock.class, new Stock("a", "a"));

        List<Stock> stocks = DataAccessService.findAll(Stock.class);
        Assert.assertEquals(1, stocks.size());

        DataAccessService.remove(Stock.class, stocks);

        stocks = DataAccessService.findAll(Stock.class);
        Assert.assertEquals(0, stocks.size());
    }

    @Test
    public void testMem() throws InterruptedException {
        List<HistoricalData> historicalDataList = DataAccessService.findAll(HistoricalData.class);

        System.out.println(historicalDataList.size());

        Thread.sleep(100000);
    }
}