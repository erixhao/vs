package com.vs.market;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.utils.BeanContext;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.MarketDataUtils;
import com.vs.dao.utility.DataAccessService;
import com.vs.http.analyzer.SinaHistoryAnalyzer;
import com.vs.repository.MarketDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by erix-mac on 16/1/14.
 */
@Service
@Slf4j
public class MarketService {

    public boolean hasMarketData(String code) {
        return MarketDataRepository.getMarketCount(code) > 0;
    }

    public void updateMarketData(List<String> codes, LocalDate date) {
        for (String s : codes) {
            this.updateMarketData(s, date);
        }
    }

    public void updateMarketData(String code, LocalDate date) {
        DownloadTask.downloadHistoryDataTask(code, date);
    }
}
