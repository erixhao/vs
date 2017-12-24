package com.vs.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by erix-mac on 16/1/26.
 */
public final class PerformanceUtils {
    private final static SimpleDateFormat TICK_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static long beginTime(String methodName){
        long begin = System.currentTimeMillis();
        System.out.println(">>>>> Begin " + methodName + " Time: " + begin);

        return begin;
    }

    public static long endTime(String methodName, long beginTime){
        long end = System.currentTimeMillis();

        long timeCost = (end - beginTime) / 1000;
        System.out.println(">>>>> End " + methodName + " Total Time: " + timeCost + " s");

        return end;
    }

    public static void tick(String action){
        System.out.println(">>>>> " + action + " Tick@" + TICK_TIME_FORMAT.format(new Date()));
    }
}
