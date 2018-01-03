package com.vs.common.domain;

import com.vs.common.domain.enums.TimePeriod;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by erix-mac on 15/8/21.
 */
@Slf4j
public class HistoricalData extends MarketData {
//    public static final String MARKET_DATE_FORMAT = "yyyy-MM-dd";
//    public static final String MARKET_SHORT_DATE_FORMAT = "MM/dd";
//    public static final String YYYYMMDD_FORMAT = "yyyyMMdd";
//
//    public static final String LIVE_DATE_FORMAT = "M/dd/yyyy";


    public TimePeriod period;

    public HistoricalData() {
    }

    public HistoricalData(LocalDate date, double high) {
        this.setDate(date);
        this.setHigh(high);
    }

    public HistoricalData(LocalDate date, TimePeriod period, double open, double yesterdayClose, String stockCode) {
        this.setDate(date);
//        this.setPeriod(period);
        this.setOpen(open);
        this.setYesterdayClose(yesterdayClose);
        this.setStockCode(stockCode);
    }

    @Override
    public String toString() {
        return super.toString() + (period == null ? "" : " TimePeriod: " + period.toString());
    }
}
