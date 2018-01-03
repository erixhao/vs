package com.vs.strategy;


import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.TradeAction;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.utils.MarketDataUtils;
import com.vs.repository.MarketDataRepository;
import com.vs.strategy.common.MarketTrendAnalyze;
import com.vs.strategy.domain.MarketContext;
import com.vs.strategy.index.IndexTrendStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 15/8/22.
 */
@Slf4j
public abstract class AbstractStrategy implements Strategy {

    @Autowired
    private MarketTrendAnalyze marketTrendAnalyze;

    @Autowired
    IndexTrendStrategy indexTrendStrategy;


    @Override
    public abstract List<TradeAction> execute(MarketContext context);

    protected BullBear analysisMarketTrend(String code, final LocalDate date) {
        return marketTrendAnalyze.analysisTrend(code, date);
    }

    protected BullBear analysisIndexTrend(LocalDate date) {
        return indexTrendStrategy.analysisTrend(date);
    }

    protected HistoricalData getCurrentMarket(String code, LocalDate date) {
        return getMarketDataT(code, date, 0);
    }

    protected HistoricalData getMarketDataT(MarketContext info) {
        return getMarketDataT(info, 0);
    }

    protected HistoricalData getMarketDataT(String code, LocalDate date, int T) {
        List<HistoricalData> datas = this.getMarketDataList(code);
        return MarketDataUtils.getMarketT(datas, date, T);
    }

    protected HistoricalData getMarketDataT(MarketContext info, int T) {
        return getMarketDataT(info.getStock().getCode(), info.getAnalysisDate(), T);
    }

    protected List<HistoricalData> getMarketDataList(String code) {
        return MarketDataRepository.getAllMarketDataBy(code);
    }

    protected HistoricalData getNextHistoricalDate(MarketContext info) {
        List<HistoricalData> datas = this.getMarketDataList(info.getStock().getCode());
        HistoricalData d0 = datas.get(0);
        //TODO need correct by erix.MarketDataUtils.getMarketCurrent(datas, info.getAnalysisDate());
        HistoricalData d1 = MarketDataUtils.getMarketT(datas, info.getAnalysisDate(), 1);

        if (d1 != null) {
            return d1;
        } else {
            if (d0 == null) {
                log.error(">>>> Exception, Please sync-up with Market Data: " + info.getAnalysisDate().toString());
                return null;
            }

            LocalDate next = (d1 == null ? MarketDataUtils.getNextTradeDate(d0.getDate(), 1) : d1.getDate());
            double price = (d1 == null ? d0.getClose() : d1.getOpen());

            return new HistoricalData(next, TimePeriod.DAILY, price, d0.getClose(), info.getStock().getCode());
        }
    }

    protected List<TradeAction> addTradeAction(List<TradeAction> result, TradeAction action, HistoricalData next, LocalDate date) {
        if (action.getTradeDirection().equals(TradeDirection.SELL) || action.getTradeDirection().equals(TradeDirection.BUY)) {
            LocalDate nextTradeDate = next.getDate() == null ? MarketDataUtils.getNextTradeDate(date) : next.getDate();
            action.setTradeDate(nextTradeDate);
            action.setTradePrice(next.getOpen());
            result.add(action);
        }

        return result;
    }

}
