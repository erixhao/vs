package com.vs.common.utils;


/**
 * Created by erix-mac on 15/8/6.
 */
public final class Constants {
    public final static String MARKET_STOCK_SPLIT = ",";
    public final static String MARKET_STOCK_LIST = "market.stock";
    public final static String WECHAT_STOCK_LIST = "wechat.stock";
    private final static String MARKET_STOCK_LOCATION = "market.location";
    private final static String TRADE_DAILY_REPORT_LOCATION = "trade.daily.report.location";
    private final static String ENV_MODE = "env.mode";
    public final static String TIME_ZONE_SHANGHAI = "Asia/Shanghai";


    public static final String MARKET_FILE_LOCATION = PropertieUtils.getMarketProperty(MARKET_STOCK_LOCATION);
    public static final String TRADE_FILE_DAILY_REPORT_LOCATION = PropertieUtils.getMarketProperty(TRADE_DAILY_REPORT_LOCATION);


    public static final double getTradeCommission(){
        return Double.parseDouble(PropertieUtils.getMarketProperty("trade.commission"));
    }

    public static final double getTradeStampTax(){
        return Double.parseDouble(PropertieUtils.getMarketProperty("trade.stamp.tax"));
    }

    public static boolean isDevelopmentMode(){
        return "DEV".equals(PropertieUtils.getMarketProperty(Constants.ENV_MODE));
    }

    public static boolean isProductionMode(){
        return "PROD".equals(PropertieUtils.getMarketProperty(Constants.ENV_MODE));
    }
}
