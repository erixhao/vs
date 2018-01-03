package com.vs.strategy.analysis;

import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.MarketDataUtils;
import com.vs.repository.MarketDataRepository;
import com.vs.strategy.domain.MarketBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by erix-mac on 2016/12/3.
 */
@Slf4j
@Component
public class MarketMovementAnalyze {

    public MarketBase calcuate(String stockCode, TimeWindow window) {
//        HistoricalData begin = marketService.getMarketHistoricalData(stockCode,TimePeriod.DAILY,MarketDataUtils.getPreTradeDateIfCurrentNot(window.getBegin()));
//        HistoricalData end   = marketService.getMarketHistoricalData(stockCode,TimePeriod.DAILY,MarketDataUtils.getPreTradeDateIfCurrentNot(window.getEnd()));
        HistoricalData begin = MarketDataRepository.getMarketDataBy(stockCode, MarketDataUtils.getPreTradeDateIfCurrentNot(window.getBegin()));
        HistoricalData end = MarketDataRepository.getMarketDataBy(stockCode, MarketDataUtils.getPreTradeDateIfCurrentNot(window.getEnd()));

        double beginPrice = begin == null ? 0 : begin.getClose();
        double endPrice = end == null ? 0 : end.getClose();

        return new MarketBase(window.getBegin(), window.getEnd(), beginPrice, endPrice);
    }


}
