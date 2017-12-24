package com.vs.common.domain.vo;


import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.enums.TimePeriod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;


/**
 * Created by erix-mac on 15/9/13.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeWindow {

    private Date begin;
    private Date end;
    private TimePeriod period;

    @Override
    public String toString() {
        return "TimeWindow{" +
                "begin=" + HistoricalData.toMarketDate(begin) +
                ", end=" + HistoricalData.toMarketDate(end) +
                ", period=" + period +
                '}';
    }


    public String toKeyString(){
        return
                HistoricalData.toMarketDate(begin) +
                "_" + HistoricalData.toMarketDate(end)
               ;
    }

    public String toShortString() {
        return "{" +
                HistoricalData.toMarketDate(begin) +
                " to " + HistoricalData.toMarketDate(end) +
                '}';
    }

    public String toWeChatString() {
        return "{" +
                HistoricalData.toMarketYYYYMMDDDate(begin) +
                "-" + HistoricalData.toMarketYYYYMMDDDate(end) +
                '}';
    }

    public static TimeWindow getTimeWindow(Date begin, Date end){
        return new TimeWindow(begin,end,TimePeriod.DAILY);
    }

    public static TimeWindow getLastYear(TimePeriod period) {
        return getTimeWindow(period,Calendar.getInstance().getTime(),-1,0,0);
    }

    public static TimeWindow getLastMonth(TimePeriod period) {
        return getTimeWindow(period,Calendar.getInstance().getTime(),0,-1,0);
    }

    public static TimeWindow getLastMonths(TimePeriod period, int months) {
        return getTimeWindow(period,Calendar.getInstance().getTime(),0,months,0);
    }

    public static TimeWindow getTimeWindow(TimePeriod period, Date end, int year, int month, int day) {

        TimeWindow window = new TimeWindow();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(end);

        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DAY_OF_YEAR, day);


        return new TimeWindow(calendar.getTime(),end,period);
    }
}
