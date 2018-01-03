package com.vs.common.utils;

import com.vs.common.domain.vo.TimeWindow;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.vs.common.utils.Constants.TIME_ZONE_SHANGHAI;


/**
 * Created by erix-mac on 15/12/6.
 */
@Slf4j
public class DateUtils {
    public static final String MARKET_DATE_FORMAT = "yyyy-MM-dd";


    public static Date nextDate(Date curr, int next) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curr);
        calendar.add(Calendar.DATE, next);

        return calendar.getTime();
    }

    public static boolean isFutureTradeDate(final Date date) {

        Date today = timeZoneDate(Calendar.getInstance().getTime());
        Date checkDate = timeZoneDate(date);

        return checkDate.after(today);
    }

    public static LocalDate nextMonths(int next) {
        return LocalDate.now().plusMonths(next);
    }

    public static Date nextMonths(Date curr, int next) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curr);
        calendar.add(Calendar.MONTH, next);

        return calendar.getTime();
    }

    public static int daysBetween(Date begin, Date end) {

        if (begin == null || end == null)
            return -1;

        long beginTime = begin.getTime();
        long endTime = end.getTime();

        return (int) ((endTime - beginTime) / (1000 * 60 * 60 * 24) + 0.5);
    }

    public static LocalDate fromString(String dateString) {
        int year = Integer.parseInt(dateString.substring(0, 4));
        int month = Integer.parseInt(dateString.substring(5, 7));
        int day = Integer.parseInt(dateString.substring(8, 10));

        return LocalDate.of(year, month, day);
    }

    public static String toString(LocalDate date) {
        StringBuilder sb = new StringBuilder();
        sb.append(date.getYear());
        sb.append("-" + format(date.getMonthValue()));
        sb.append("-" + format(date.getDayOfMonth()));
        return sb.toString();
    }

    private static String format(int value) {
        String str = String.valueOf(value);
        if (str.length() < 2) {
            str = "0" + str;
        }

        return str;
    }


    public static Date toMarketDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat(MARKET_DATE_FORMAT);
        Date d = null;
        try {
            d = format.parse(date);
        } catch (ParseException e) {
            log.error("toMarket Date Error: " + e.toString());
        }

        return d;
    }

    public static String toMarketDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(MARKET_DATE_FORMAT);
        return format.format(date);
    }

    public static Date formatMarketData(final Date date) {
        return toMarketDate(toMarketDate(date));
    }

    public static Date randomDate(Date begin, Date end) {
        long date = begin.getTime() + (long) (Math.random() * (end.getTime() - begin.getTime()));
        return new Date(date);
    }

    public static String toMarketDate(int years) {
        return toMarketDate(getMarketDateByYears(years));
    }

    public static Date getMarketDateByYears(int years) {
        return getMarketDateByMonths(years * 12);
    }

    public static Date getMarketDateByMonths(int months) {
        Date today = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.MONTH, months);

        return calendar.getTime();
    }

    public static int extractYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return c.get(Calendar.YEAR);
    }

    public static int extractMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return c.get(Calendar.MONTH) + 1;
    }

    public static int extractQuarter(Date date) {
        return toQuarter(extractMonth(date));
    }

    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    public static int getCurrentQuarter() {
        return toQuarter(getCurrentMonth());
    }

//    public static TimeWindow getTimeWindow(int month) {
//        Date today = Calendar.getInstance().getTime();
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(today);
//        calendar.add(Calendar.DAY_OF_MONTH, month);
//
//        System.out.println(">>>>>>  Date : " + calendar.toString());
//
//        return TimeWindow.getTimeWindow(calendar.getTime(), today);
//    }


    private static int toQuarter(int month) {
        if (month < 1 || month > 12)
            return -1;

        int q = -1;
        if (month == 1 || month == 2 || month == 3) {
            q = 1;
        } else if (month == 4 || month == 5 || month == 6) {
            q = 2;
        } else if (month == 7 || month == 8 || month == 9) {
            q = 3;
        } else if (month == 10 || month == 11 || month == 12) {
            q = 4;
        }

        return q;

    }

    public static LocalDate getLastDate(int days) {
//        Date today = Calendar.getInstance().getTime();
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(today);
//        calendar.add(Calendar.DAY_OF_MONTH, days);

        return LocalDate.now().plusDays(days);
    }

    public static boolean isSameDate(Date d1, Date d2) {
        return toMarketDate(timeZoneDate(d1)).equals(toMarketDate(timeZoneDate(d2)));
    }

    public static int compareTo(Date d1, Date d2) {
        Date fd1 = formatMarketData(d1);
        Date fd2 = formatMarketData(d2);

        return fd1.compareTo(fd2);
    }

    public static boolean isToday(Date date) {
        Date today = Calendar.getInstance().getTime();
        return isSameDate(today, date);
    }

    public static boolean isTradingTime() {
        Date now = Calendar.getInstance().getTime();
        int hours = now.getHours();
        int minutes = now.getMinutes();
        System.out.println(hours + " " + minutes);
        return ((hours == 9 && minutes >= 30) || (hours >= 10 && hours < 15));
    }

    public static boolean isStartTradingTime() {
        Date now = Calendar.getInstance().getTime();
        int hours = now.getHours();
        int minutes = now.getMinutes();

        return ((hours == 9 && minutes >= 30) || (hours >= 10));
    }

    public static boolean isTradingPriceAvaiable() {
        Date now = Calendar.getInstance().getTime();
        int hours = now.getHours();
        int minutes = now.getMinutes();
        //System.out.println(hours + " " + minutes);
        return ((hours == 9 && minutes >= 30) || (hours >= 10)) && (hours <= 15);
    }


    public static String withTimeZoneFormat(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(MARKET_DATE_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_SHANGHAI));

        return df.format(date);
    }

    public static Date timeZoneDate(Date date) {
        return toMarketDate(withTimeZoneFormat(date));
    }

    public static void main(String[] args) {
        //System.out.println(DateUtils.getLastDate(-40).toString());
        System.out.println(DateUtils.nextMonths(3).toString());
        System.out.println(DateUtils.nextMonths(-6).toString());


    }
}
