package com.vs.common.domain.enums;

import java.util.Calendar;

/**
 * Created by erix-mac on 15/8/1.
 */
public enum TimePeriod {

    DAILY("daily","d", Calendar.DATE),WEEKLY("weekly","w",Calendar.WEEK_OF_YEAR),MONTHLY("monthly","m",Calendar.MONTH),LIVE("live","live",-1),NONE("none","none",-1);

    private String period;
    private String name;
    private int calenda;

    private TimePeriod(String name, String period, int calenda){
        this.name = name;
        this.period = period;
        this.calenda = calenda;
    }

    public String getName(){
        return this.name;
    }

    public String getPeriod(){
        return this.period;
    }

    public int getCalenda(){ return this.calenda; }
}
