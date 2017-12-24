package com.vs.common.domain.enums;

/**
 * Created by erix-mac on 15/8/3.
 */
public enum Market {
    Shanghai, Shenzhen;

    private final static String SHANGHAI_PREFIX_6 = "6";
    private final static String SHANGHAI_INDEX = "000001";
    private final static String SHANGHAI_SHENZHEN_300_INDEX = "000300";
    private final static String SHANGHAI_50ETF_INDEX = "510050";

    public static Market getMarket( String stockCode ){
        if ( stockCode.startsWith(SHANGHAI_PREFIX_6)
                || SHANGHAI_INDEX.equals(stockCode)
                || stockCode.startsWith(SHANGHAI_INDEX)
                || SHANGHAI_SHENZHEN_300_INDEX.equals(stockCode)
                || SHANGHAI_50ETF_INDEX.equals(stockCode)
                ) {
            return Market.Shanghai;
        }else{
            return Market.Shenzhen;
        }
    }
}
