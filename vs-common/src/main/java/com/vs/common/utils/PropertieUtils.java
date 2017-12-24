package com.vs.common.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vs.common.domain.Stock;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by erix-mac on 15/8/3.
 */
@Slf4j
public final class PropertieUtils {

    private final static String MARKET_PROP = "marketdata.properties";
    private final static String STOCK_PROP = "stockdata.properties";
    private final static String CHINA_MARKET_PROP = "market_list.properties";



    public static String getMarketProperty(String key){
        return getProperty(MARKET_PROP, key);
    }

    public static String getSystemVersion(){
        return getMarketProperty("venus.version");
    }

    public static String getSystemVersionDate(){
        return getMarketProperty("venus.version.date");
    }



    public static List<Stock> getStockList(){
        String stockList = getProperty(STOCK_PROP,Constants.MARKET_STOCK_LIST);
        String[] stocks = stockList.split(Constants.MARKET_STOCK_SPLIT);
        Set<String> unique = Sets.newHashSet(stocks);
        List<Stock> list = Lists.newArrayList();

        for ( String s : unique ){
            list.add(new Stock(s,s));
        }

        return list;
    }

    public static List<String> getStockCodeList(){
        return getPropertyStockList(STOCK_PROP, Constants.MARKET_STOCK_LIST);
    }

    public static List<String> getChinaMarketList(){
        return getPropertyStockList(CHINA_MARKET_PROP, Constants.MARKET_STOCK_LIST);
    }

    public static List<String> getWechatMarketList(){
        return getPropertyStockList(CHINA_MARKET_PROP, Constants.WECHAT_STOCK_LIST);
    }

    private static List<String> getPropertyStockList(String proptyFile, String key){
        String stockList = getProperty(proptyFile,key);
        String[] stocks = stockList.split(Constants.MARKET_STOCK_SPLIT);
        Set<String> unique = Sets.newHashSet(stocks);
        return Lists.newArrayList(unique);
    }

    private static String getProperty(String propName, String key) {
        Properties prop = new Properties();

        InputStream in = PropertieUtils.class.getClassLoader().getResourceAsStream(propName);
        try {
            prop.load(in);

            return prop.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if ( in != null )
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e.toString());
                }
        }

        return null;
    }


    public static void main(String[] args){
        System.out.println(PropertieUtils.getSystemVersion());
    }

}
