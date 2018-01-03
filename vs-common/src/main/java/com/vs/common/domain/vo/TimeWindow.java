package com.vs.common.domain.vo;


import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;


/**
 * Created by erix-mac on 15/9/13.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeWindow {

    private LocalDate begin;
    private LocalDate end;
    private TimePeriod period;

    @Override
    public String toString() {
        return "TimeWindow{" +
                "begin=" + begin +
                ", end=" + end +
                ", period=" + period +
                '}';
    }


    public String toKeyString() {
        return
                DateUtils.toString(begin) +
                        "_" + DateUtils.toString(end)
                ;
    }

    public String toShortString() {
        return "{" +
                DateUtils.toString(begin) +
                " to " + DateUtils.toString(end) +
                '}';
    }

    public String toWeChatString() {
        return "{" +
                DateUtils.toString(begin) +
                "-" + DateUtils.toString(end) +
                '}';
    }

    public static TimeWindow getTimeWindow(LocalDate begin, LocalDate end) {
        return new TimeWindow(begin, end, TimePeriod.DAILY);
    }

    public static TimeWindow getLastYear(TimePeriod period) {
        return getTimeWindow(period, LocalDate.now(), -1, 0, 0);
    }

    public static TimeWindow getLastMonth(TimePeriod period) {
        return getTimeWindow(period, LocalDate.now(), 0, -1, 0);
    }

    public static TimeWindow getLastMonths(TimePeriod period, int months) {
        return getTimeWindow(period, LocalDate.now(), 0, months, 0);
    }

    public static TimeWindow getTimeWindow(TimePeriod period, LocalDate end, int year, int month, int day) {
        return new TimeWindow(LocalDate.of(year, month, day), end, period);
    }
}
