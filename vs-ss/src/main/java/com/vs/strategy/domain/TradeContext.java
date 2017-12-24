package com.vs.strategy.domain;


import com.vs.common.domain.Stock;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.vo.TimeWindow;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * Created by erix-mac on 16/2/17.
 */
@AllArgsConstructor
@Data
public class TradeContext {

    private TradingBook tradingBook;
    private Date analysisDate;
    private TimeWindow timeWindow;
    private TimePeriod period = TimePeriod.NONE;
    private double marketPrice = 0;

    public Stock getStock(){
        return this.getTradingBook().getStock();
    }

    public boolean isTradeProfitable(){
        return tradingBook.getPnL().getTotalProfitPercentage() > 0;
    }
}
