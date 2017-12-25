package com.vs.common.domain.enums;

/**
 * Created by erix-mac on 15/8/3.
 */
public enum MarketProvider {
    SINA,YAHOO;

    private static final String MARKET_CODE_SINA_SH = "sh";
    private static final String MARKET_CODE_SINA_SZ = "sz";
    private static final String MARKET_CODE_YAHOO_SH = ".ss";
    private static final String MARKET_CODE_YAHOO_SZ = ".sz";


    public String getMarketCode(String code){
        if ( isConvertMarketProviderCode(code) )
            return code;

        return (this.ordinal() == 0 ? toSINACode(code) : toYAHOOCode(code));
    }

    public static String[] toSINACode( String[] stocks ){
        StringBuilder sb = new StringBuilder();
        for (String s: stocks){
            String code = s.indexOf("s") > 0 ? s.replaceAll("s","") : s;
            sb.append(toSINACode(code)).append(",");
        }

        String stockUrl = sb.toString();

        return stockUrl.substring(0,stockUrl.length() - 1).split(",");
    }

    public static String toSINACode( String stock ){
        if ( isConvertMarketProviderCode(stock) )
            return stock;

        Market market = Market.toMarket(stock);

        return (market.equals(Market.Shanghai) ? MARKET_CODE_SINA_SH + stock : MARKET_CODE_SINA_SZ + stock);
    }

    public static String toYAHOOCode( String[] stocks ){
        StringBuilder sb = new StringBuilder();
        for (String s: stocks){
            sb.append(toYAHOOCode(s)).append(",");
        }

        String stockUrl = sb.toString();

        return stockUrl.substring(0,stockUrl.length() - 1);
    }

    public static String toYAHOOCode( String stock ){
        if ( isConvertMarketProviderCode(stock) )
            return stock;

        Market market = Market.toMarket(stock);

        return (market.equals(Market.Shanghai) ? stock + MARKET_CODE_YAHOO_SH : stock + MARKET_CODE_YAHOO_SZ);
    }

    public static boolean isConvertMarketProviderCode(String stock){
        return
                (  stock.indexOf(MARKET_CODE_SINA_SH)  >= 0
                || stock.indexOf(MARKET_CODE_SINA_SZ)  >= 0
                || stock.indexOf(MARKET_CODE_YAHOO_SH) >= 0
                || stock.indexOf(MARKET_CODE_YAHOO_SZ) >= 0
                );
    }

}
