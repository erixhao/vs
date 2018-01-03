package com.vs.common.utils;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.enums.MarketIndexs;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Created by erix-mac on 15/8/29.
 */
@Component
public final class MarketDataUtils {
    public static final String HOLIDAY_DATE_FORMAT = "MM-dd";
    private final static List<String> HOLIDAYS = Lists.newArrayList("0101", "0102", "0403", "0404", "0405", "0406", "0501", "0502", "0503", "1001", "1002", "1003", "1004", "1005", "1006", "1007");


    public static HistoricalData getMarketCurrent(List<HistoricalData> markets, LocalDate anaysisDate) {
        for (HistoricalData data : markets) {
            if (data.getDate().equals(anaysisDate)) {
                return data;
            }
        }

        return null;
    }

    public static int indexBoundsCheck(int index, int SIZE){
        return index < 0 ? 0 : (index >= SIZE ? SIZE-1 : index);
    }

    public static int indexOf(List<HistoricalData> markets, LocalDate anaysisDate) {
        for (int i = 0; i < markets.size(); i++) {
            if (markets.get(i).getDate().equals(anaysisDate)) {
                return i;
            }
        }

        return -1;
    }

    /*
    if current is a Trade date, return current;
    else return next Trade date.
     */
    public static LocalDate getNextTradeDateIfCurrentNot(LocalDate current) {
        if (isTradingDate(current))
            return current;

        return getNextTradeDate(current);
    }

    public static LocalDate getNextTradeDate(LocalDate current) {
        LocalDate next = current.plusDays(1);

        while (!isTradingDate(next)) {
            next = next.plusDays(1);
        }

        return next;
    }

    public static LocalDate getNextTradeDate(LocalDate current, int t) {
        LocalDate next = current;
        while (t > 0) {
            next = getNextTradeDate(next);
            t--;
        }

        return next;
    }

    public static LocalDate getPreTradeDate() {
        return getPreTradeDate(LocalDate.now());
    }

    public static LocalDate getPreTradeDateIfCurrentNot(LocalDate current) {
        if (isTradingDate(current) && DateUtils.isStartTradingTime())
            return current;

        return getPreTradeDate(current);
    }

    public static LocalDate getPreTradeDate(LocalDate cur) {
        cur = cur.minusDays(1);

        while (!isTradingDate(cur)) {
            cur = cur.minusDays(1);
        }

        return cur;
    }

    public static Date nextCalendarDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);

        return calendar.getTime();
    }

    public static boolean isTradingDate(LocalDate date) {
        if (date.getDayOfWeek().equals(DayOfWeek.SATURDAY) || date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            return false;
        }

        if (HOLIDAYS.contains(DateUtils.toString(date).substring(4, 8))) {
            return false;
        }

        return true;
    }

    public static HistoricalData getMarketT(List<HistoricalData> datas, LocalDate current, int t) {
        LocalDate nextTradeDate = current;
        while (t > 0) {
            nextTradeDate = getNextTradeDate(current);
            t--;
        }

        for (HistoricalData data : datas) {
            if (data.getDate().equals(nextTradeDate)) {
                return data;
            }
        }

        return null;
    }

    public static List<Double> extractMarketClose(List<HistoricalData> datas) {
        List<Double> list = Lists.newArrayList();

        for (HistoricalData d : datas) {
            list.add(d.getClose());
        }

        return list;
    }

    public static LocalDate getFirstTradeDateOfWeek(LocalDate cur) {
        while (!cur.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            cur = cur.minusDays(1);
        }

        return MarketDataUtils.getNextTradeDate(cur);
    }
//
//    public static List<Pair<Date,Double>> extractMarketClose(List<HistoricalData> datas, Date current, int lastSize){
//        List<Pair<Date,Double>> list = Lists.newArrayList();
//        int currIndex = indexOf(datas, current);
//
//        if (currIndex == -1 )
//            return list;
//        //System.out.println(">>>>>>>>>>>>> Date Current: " + current.toString() + " SIZE:" + datas.size() + " lastIndex: " + currIndex + lastSize );
//        for ( int i=currIndex;i>=currIndex-lastSize && i >= 0;i-- ){
//            //int index = indexBoundsCheck(i, datas.size());
//            HistoricalData d = datas.get(i);
//            list.add(Pair.of(d.getDate(),d.getClose()));
//        }
//
//        return list;
//    }
//
//    public static List<HistoricalData> extractMarkets(List<HistoricalData> datas, Date current, int lastSize){
//        List<HistoricalData> list = Lists.newArrayList();
//        int currIndex = indexOf(datas, current);
//
//        if (currIndex == -1 )
//            return list;
//        //System.out.println(">>>>>>>>>>>>> Date Current: " + current.toString() + " SIZE:" + datas.size() + " lastIndex: " + currIndex + lastSize );
//        for ( int i=currIndex;i>=currIndex-lastSize && i >= 0;i-- ){
//            list.add(datas.get(i));
//        }
//
//        return list;
//    }


    public static void main(String[] args) {
        //System.out.println(getPreTradeDate(Calendar.getInstance().getTime()));

        //System.out.println(nextCalendarDate(new Date()));

        //System.out.println(getFirstTradeDateOfWeek(DateUtils.toMarketDate("2017-01-06")));
        //System.out.println(getFirstTradeDateOfWeek(DateUtils.toMarketDate("2016-12-25")));
        //System.out.println(getFirstTradeDateOfWeek(DateUtils.toMarketDate("2016-10-1")));
        //System.out.println(getFirstTradeDateOfWeek(DateUtils.toMarketDate("2016-10-2")));
        //System.out.println(getFirstTradeDateOfWeek(DateUtils.toMarketDate("2016-10-5")));


    }

}
