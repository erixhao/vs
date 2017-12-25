package com.vs.strategy.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * Created by erix-mac on 2016/12/3.
 */
@Data
@AllArgsConstructor
public class MarketBase {
    // to calc pure long position price gap, see if our strategy is good or not.
    private Date beginDate;
    private Date endDate;
    private double beginPrice;
    private double endPrice;

    public double getProfitPercentage(){
        return (beginPrice == 0 || Double.isNaN(endPrice)) ? 0 :  ((endPrice - beginPrice) / beginPrice) * 100;
    }

}
