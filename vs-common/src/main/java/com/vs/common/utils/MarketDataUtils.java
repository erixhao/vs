package com.vs.common.utils;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.enums.MarketIndex;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
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
    private final static List<String> HOLIDAYS = Lists.newArrayList("01-01", "01-02", "04-03", "04-04", "04-05", "04-06", "05-01", "05-02", "05-03", "10-01", "10-02", "10-03", "10-04", "10-05", "10-06", "10-07");


    public static List<HistoricalData> extractByDate(List<HistoricalData> datas, Date begin) {
        return extractByDate(datas, begin, null);
    }

    public static List<HistoricalData> extractByDate(List<HistoricalData> datas, Date begin, Date end) {

        List<HistoricalData> periodDatas = Lists.newArrayList();

        for (HistoricalData data : datas) {
            Date d = data.getDate();
            if (d.compareTo(begin) >= 0) {
                if (end == null || d.compareTo(end) <= 0) {
                    periodDatas.add(data);
                }
            }
        }

        Collections.sort(periodDatas);

        return periodDatas;
    }

    public static int indexOf1(List<HistoricalData> datas, Date current) {
        if (datas == null || datas.size() == 0)
            return -1;

        // if binary search doesn't fine, try the normal sequency search, may take more time
        for (int i = 0; i < datas.size(); i++) {
            if ( DateUtils.isSameDate(datas.get(i).getDate(), current) ) {
                return i;
            } else {
                Date previous = (i == 0 ? datas.get(i).getDate() : datas.get(i - 1).getDate());
                Date after = (i == datas.size() - 1 ? datas.get(i).getDate() : datas.get(i + 1).getDate());

                if (current.after(previous) && current.before(after)) {
                    return i;
                }

            }
        }

        return -1;
    }

    public static int indexOf(List<HistoricalData> datas, Date current) {
        if (datas == null || datas.size() == 0)
            return -1;

        HistoricalData key = new HistoricalData();
        key.setDate(current);

        return binarySearch(datas, current);
    }

    /**
     * binary search 1st for the speed, assume the list has already ordered.
     */
    public static int binarySearch(List<HistoricalData> datas, Date key){
        int low = 0;
        int high = datas.size() - 1;
        final int SIZE = datas.size() - 1;

        while ( low <= high ){
            int middle = (low + high) / 2;

            int diff = DateUtils.compareTo(datas.get(middle).getDate(), key);
            if ( diff == 0 ){
                return middle;
            }else if ( diff < 0 ){
                low = middle + 1;
            }else {
                high = middle - 1;
            }
        }

        int prevIndex  = indexBoundsCheck(Math.min(low,high),SIZE);
        int afterIndex = indexBoundsCheck(Math.max(low,high),SIZE);

        //System.out.println(">>>>>SIZE:" + SIZE + " preIndex: " + prevIndex + " afterIndex: " + afterIndex);

        if ( key.after(datas.get(prevIndex).getDate()) && key.before(datas.get(afterIndex).getDate()) ) {
            return prevIndex;
        }

        return -1;
    }

    public static int indexBoundsCheck(int index, int SIZE){
        return index < 0 ? 0 : (index >= SIZE ? SIZE-1 : index);
    }



    public static HistoricalData getMarketCurrent(List<HistoricalData> datas, Date current) {
        return getMarketT(datas, current, 0);
    }

    public static Date getFirstTradeDateOfWeek(Date current){
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        int day = cal.get(Calendar.DAY_OF_WEEK);

        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.add(Calendar.DATE,cal.getFirstDayOfWeek() - day);

        return getNextTradeDateIfCurrentNot(cal.getTime());
    }

    /*
    if current is a Trade date, return current;
    else return next Trade date.
     */
    public static Date getNextTradeDateIfCurrentNot(Date current){
        if ( isTradingDate(current) )
            return current;

        return getNextTradeDate(current);
    }

    public static Date getNextTradeDate(Date current) {
        Date next = nextCalendarDate(current);

        while (!isTradingDate(next)) {
            next = nextCalendarDate(next);
        }

        return next;
    }

    public static Date getPreTradeDate(){
        return getPreTradeDate(Calendar.getInstance().getTime());
    }

    public static Date getPreTradeDateIfCurrentNot(Date current){
        if ( isTradingDate(current) && DateUtils.isStartTradingTime() )
            return current;

        return getPreTradeDate(current);
    }

    public static Date getPreTradeDate(Date today){
        Calendar now = Calendar.getInstance();
        now.setTime(today);

        while (true) {
            now.roll(Calendar.DAY_OF_YEAR, -1);
            int a = now.get(Calendar.DAY_OF_WEEK) - 1;
            if (a != 6 && a != 0) {
                break;
            }
        }

        while ( !isTradingDate(now.getTime()) ){
            now.setTime(getPreTradeDate(now.getTime()));
        }

        return now.getTime();
    }

    public static Date nextCalendarDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);

        return calendar.getTime();
    }

    public static Date getNextTradeDate(List<HistoricalData> datas, Date current) {
        int currIndex = indexOf(datas, current);
        int index = currIndex + 1;

        if (index > datas.size()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(current);

            calendar.add(Calendar.DATE, 1);
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                calendar.add(Calendar.DATE, 2);
            } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                calendar.add(Calendar.DATE, 1);
            }

            return calendar.getTime();
        }

        HistoricalData next = getMarketT(datas, current, 1);

        return next == null ? null : next.getDate();
    }

    public static boolean isTradingDate(HistoricalData d) {

        if (d == null)
            return false;

        if (!MarketIndex.isIndex(d.getCode()) && (d.getVolume() == 0 && d.getOpen() == d.getClose() && d.getOpen() == d.getHigh()))
            return false;

        return isTradingDate(d.getDate());
    }

    public static boolean isTradingDate(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return false;
        }

        SimpleDateFormat format = new SimpleDateFormat(HOLIDAY_DATE_FORMAT);

        if (HOLIDAYS.contains(format.format(date))) {
            //System.out.println("HOLIDAY : " + HistoricalData.toMarketDate(date));
            return false;
        }

        return true;
    }

    public static HistoricalData getMarketT(List<HistoricalData> datas, Date current, int t) {

        int currIndex = indexOf(datas, current);

        if (currIndex == -1)
            return null;

        if (t == 0) {
            return datas.get(currIndex);
        }

        int index = currIndex + t;
        if (index < 0) {
            return datas.get(0);
        } else if (index >= datas.size()) {
            return null;
        } else {
            return datas.get(index);
        }
    }

    public static List<Double> extractMarketClose(List<HistoricalData> datas){
        List<Double> list = Lists.newArrayList();

        for ( HistoricalData d : datas ){
            list.add(d.getClose());
        }

        return list;
    }

    public static List<Pair<Date,Double>> extractMarketClose(List<HistoricalData> datas, Date current, int lastSize){
        List<Pair<Date,Double>> list = Lists.newArrayList();
        int currIndex = indexOf(datas, current);

        if (currIndex == -1 )
            return list;
        //System.out.println(">>>>>>>>>>>>> Date Current: " + current.toString() + " SIZE:" + datas.size() + " lastIndex: " + currIndex + lastSize );
        for ( int i=currIndex;i>=currIndex-lastSize && i >= 0;i-- ){
            //int index = indexBoundsCheck(i, datas.size());
            HistoricalData d = datas.get(i);
            list.add(Pair.of(d.getDate(),d.getClose()));
        }

        return list;
    }

    public static List<HistoricalData> extractMarkets(List<HistoricalData> datas, Date current, int lastSize){
        List<HistoricalData> list = Lists.newArrayList();
        int currIndex = indexOf(datas, current);

        if (currIndex == -1 )
            return list;
        //System.out.println(">>>>>>>>>>>>> Date Current: " + current.toString() + " SIZE:" + datas.size() + " lastIndex: " + currIndex + lastSize );
        for ( int i=currIndex;i>=currIndex-lastSize && i >= 0;i-- ){
            list.add(datas.get(i));
        }

        return list;
    }


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
