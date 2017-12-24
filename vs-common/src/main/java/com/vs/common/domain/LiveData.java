package com.vs.common.domain;

import lombok.Data;

/**
 * Created by erix-mac on 15/8/2.
 */
@Data
public class LiveData extends AbstractMarketData {
    public static final String MARKET_DATE_FORMAT = "MM-dd-yyyy";


    private String currentTime;
    private double currentPrice;
    private double currentBuyPrice;
    private double currentSellPrice;

    private long buyVolume1;
    private long buyVolume2;
    private long buyVolume3;
    private long buyVolume4;
    private long buyVolume5;

    private double buyPrice1;
    private double buyPrice2;
    private double buyPrice3;
    private double buyPrice4;
    private double buyPrice5;

    private long sellVolume1;
    private long sellVolume2;
    private long sellVolume3;
    private long sellVolume4;
    private long sellVolume5;

    private double sellPrice1;
    private double sellPrice2;
    private double sellPrice3;
    private double sellPrice4;
    private double sellPrice5;

/*    protected double high52weeks;
    protected double low52weeks;*/


    @Override
    public double getPercentage(){
        if ( this.close == 0 )
            return 0;

        if ( this.close > 0 ){
            return super.getPercentage();
        }else{
            return (this.currentPrice - this.yesterdayClose) * 100/this.yesterdayClose;
        }
    }
}
