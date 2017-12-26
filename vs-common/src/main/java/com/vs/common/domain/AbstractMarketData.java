package com.vs.common.domain;

import com.vs.common.utils.DateUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public abstract class AbstractMarketData extends Stock implements Comparable<AbstractMarketData>, Serializable {
    protected long id;
    protected Date date;
    protected double yesterdayClose;
    protected double open;
    protected double high;
    protected double low;
    protected double close;
    protected double adjClose;
    protected long volume;
    protected double volumeAmount;
    protected Date updateDate;

    public double getPercentage(){
        if ( this.yesterdayClose == 0 )
            return 0;
        else
            return (this.close - this.yesterdayClose) * 100/this.yesterdayClose;
    }

    public boolean isCloseUp(){
        return close >= open;
    }

    public boolean isCloseDown(){
        return close < open;
    }

    public String timeZoneDate(){
        return DateUtils.withTimeZoneFormat(date);
    }



    protected void clear(){
        this.open = 0;
        this.close = 0;
        this.high = 0;
        this.low = 0;
        this.volume = 0;
        this.yesterdayClose = 0;
    }

    @Override
    public int compareTo(AbstractMarketData o) {
        return this.date.compareTo(o.getDate());
    }


}
