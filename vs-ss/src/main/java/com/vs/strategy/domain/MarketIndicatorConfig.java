package com.vs.strategy.domain;

import com.vs.common.utils.PropertieUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by erix-mac on 2017/1/5.
 */
@AllArgsConstructor
@Data
public class MarketIndicatorConfig {

    private double bigBullMarketIndicator;
    private double bigBearMarketIndicator;

    private double bullMarketIndicator;
    private double bearMarketIndicator;
    private int mktObsWindow;

    public MarketIndicatorConfig(){}


    public static MarketIndicatorConfig getIndexIndicatorConfig(){
        MarketIndicatorConfig indicator = new MarketIndicatorConfig();

        indicator.setBigBullMarketIndicator(Integer.parseInt(PropertieUtils.getMarketProperty("index.market.bigbull.trend")));
        indicator.setBullMarketIndicator(Integer.parseInt(PropertieUtils.getMarketProperty("index.market.bull.trend")));
        indicator.setBigBearMarketIndicator(Integer.parseInt(PropertieUtils.getMarketProperty("index.market.bigbear.trend")));
        indicator.setBearMarketIndicator(Integer.parseInt(PropertieUtils.getMarketProperty("index.market.bear.trend")));
        indicator.setMktObsWindow(Integer.parseInt(PropertieUtils.getMarketProperty("index.market.obs.window")));

        return indicator;
    }

    public static MarketIndicatorConfig getStockIndicatorConfig(){
        MarketIndicatorConfig indicator = new MarketIndicatorConfig();

        indicator.setBigBullMarketIndicator(Integer.parseInt(PropertieUtils.getMarketProperty("stock.market.bigbull.trend")));
        indicator.setBullMarketIndicator(Integer.parseInt(PropertieUtils.getMarketProperty("stock.market.bull.trend")));
        indicator.setBigBearMarketIndicator(Integer.parseInt(PropertieUtils.getMarketProperty("stock.market.bigbear.trend")));
        indicator.setBearMarketIndicator(Integer.parseInt(PropertieUtils.getMarketProperty("stock.market.bear.trend")));
        indicator.setMktObsWindow(Integer.parseInt(PropertieUtils.getMarketProperty("stock.market.obs.window")));

        return indicator;
    }
}
