package com.vs.common.utils;

import java.time.LocalDate;

/**
 * Created by erix-mac on 16/7/29.
 */
public final class DebugUtils {


    public static void debugAt(LocalDate date, String mkDate){
        //DebugUtils.debugAt(date,"2016-09-23");
        // DEBUG DATE
        if ( DateUtils.toMarketDate(date).equals(mkDate)) {
            System.out.println(">>>>>>>>>>>>DebugUtils MrkDate@  " + DateUtils.toMarketDate(date));
        }
    }
}
