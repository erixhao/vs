package com.vs.service.regression;

import com.vs.common.domain.TradingBook;
import com.vs.common.domain.vo.TimeWindow;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Created by erix-mac on 16/7/19.
 */
@Data
@AllArgsConstructor
public final class RegressionResult {
    private TimeWindow timeWindow;
    private List<TradingBook> tradingBooks;
}
