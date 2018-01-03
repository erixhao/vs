package com.vs.market;


import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.utils.DateUtils;
import com.vs.dao.utility.DataAccessService;
import com.vs.http.analyzer.SinaHistoryAnalyzer;
import com.vs.http.analyzer.SinaStockAnalyzer;
import com.vs.repository.MarketDataRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by erix-mac on 15/8/1.
 */
@Component
@Getter
@Setter
@Slf4j
public class DownloadTask implements Runnable {
    private static final LocalDate START_TIME = LocalDate.of(1999, 1, 1);

    private String code;
    private LocalDate date;

    private DownloadTask() {
    }

    private DownloadTask(String code, LocalDate date) {
        this.code = code;
        this.date = date;
    }

    public static DownloadTask newTask(String code, LocalDate date) {
        return new DownloadTask(code, date);
    }

    @Override
    public void run() {
        downloadHistoryDataTask(this.code, this.date);
    }

    public static void downloadHistoryDataTask(String code, LocalDate date) {
        List<HistoricalData> historicalDataList = SinaHistoryAnalyzer.getData(code, date);
        DataAccessService.save(HistoricalData.class, historicalDataList);
    }

    public static void downloadHistoryDataTask(String code) {
        LocalDate cur = LocalDate.now();
        while (true) {
            if (cur.isBefore(START_TIME)) {
                break;
            }

            if (isDataExist(code, cur)) {
//                System.out.println(cur);
                List<HistoricalData> historicalDataList = SinaHistoryAnalyzer.getData(code, cur);
                DataAccessService.save(HistoricalData.class, historicalDataList);
                cur = cur.minusMonths(3);
            } else {
                cur = cur.minusMonths(3);
            }
        }
    }

    private static boolean isDataExist(String code, LocalDate cur) {
        return (MarketDataRepository.getMarketCount(code, cur) == 0 && cur == LocalDate.now()) ||
                (MarketDataRepository.getMarketCount(code, cur.minusDays(1)) == 0 &&
                        MarketDataRepository.getMarketCount(code, cur.minusDays(2)) == 0 &&
                        MarketDataRepository.getMarketCount(code, cur.minusDays(3)) == 0 &&
                        MarketDataRepository.getMarketCount(code, cur.minusDays(4)) == 0 &&
                        MarketDataRepository.getMarketCount(code, cur.minusDays(5)) == 0 &&
                        MarketDataRepository.getMarketCount(code, cur.minusDays(6)) == 0);
    }

    public static void downloadStockTask() {
        List<Stock> stockList = SinaStockAnalyzer.getData();
        DataAccessService.save(Stock.class, stockList);
    }


}