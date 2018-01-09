package com.vs.market;


import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.utils.MarketDataUtils;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by erix-mac on 15/8/1.
 */
@Component
@Getter
@Setter
@Slf4j
public class DownloadTask implements Runnable {
    private static final LocalDate START_TIME = LocalDate.of(1990, 1, 1);// Shanghai market starts @1990

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

    public static void downloadHistoryDataTask(String code) {
        downloadHistoryDataTask(code, START_TIME);
    }

    public static void downloadHistoryDataTask(String code, LocalDate tillDate) {
        LocalDate cur = LocalDate.now();
        while (true) {
            if (cur.isBefore(tillDate)) {
                break;
            }

            if (isDataNotExist(code, cur)) {
                List<HistoricalData> historicalDataList = SinaHistoryAnalyzer.getData(code, cur);
                if (historicalDataList.size() == 0) {
                    break;
                }
                DataAccessService.saveMkt(historicalDataList);
                cur = cur.minusMonths(3);
            } else {
                List<HistoricalData> historicalDataList = DataAccessService.findAllMktBy(code).stream().sorted().collect(Collectors.toList());
                cur = historicalDataList.get(0).getDate().minusDays(1);
            }
        }
    }

    private static boolean isDataNotExist(String code, LocalDate cur) {
        if (MarketDataUtils.isTradingDate(cur)) {
            return MarketDataRepository.getMarketCount(code, cur) == 0;
        } else {
            cur = MarketDataUtils.getPreTradeDate(cur);
            return MarketDataRepository.getMarketCount(code, cur) == 0;
        }
    }

    public static void downloadStockTask() {
        List<Stock> stockList = SinaStockAnalyzer.getData();
        DataAccessService.save(Stock.class, stockList);
    }

    public static void main(String[] args) {
        //downloadStockTask();
        downloadHistoryDataTask("600030");
    }

}
