package com.vs.market;


import com.vs.common.domain.AbstractMarketData;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.enums.MarketIndexs;
import com.vs.common.domain.enums.MarketProvider;
import com.vs.common.domain.enums.TimePeriod;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by erix-mac on 15/8/1.
 */
@Service
@Slf4j
public class YahooMarketDataProvider {

    public static final String YAHOO_FINANCE_URL = "http://table.finance.yahoo.com/table.csv?";
    public static final String YAHOO_FINANCE_URL_TODAY = "http://download.finance.yahoo.com/d/quotes.csv?";
    public static final String RESPONSE_SPLIT = ",";
    private final static String MARKET_STOCK_LOCATION = "market.location";



    public List<HistoricalData> getMarketData(MarketIndexs index, TimePeriod period, int historyDays ){
        return getMarketData(index.getCode(MarketProvider.YAHOO), period, historyDays);
    }


    public List<HistoricalData> getAllMarketData(String stockName){
        return this.getMarketData(stockName, TimePeriod.DAILY, -1);
    }

    public Map<String, List<HistoricalData>> getMarketData(String[] stockName, TimePeriod period, int historyDays){
        Map<String, List<HistoricalData>> map = new HashMap<>();

        for ( String stock : stockName ){
            map.put(stock,this.getMarketData(stock,period,historyDays));
        }

        return map;
    }


    /*@SneakyThrows(IOException.class)
    public List<Dividends> getDividends(String stockName, int historyDays){

        List<Dividends> datas = new ArrayList<>();

        String url = extractURL(stockName, null, historyDays, true);

        URL yahooURL = new URL(url);
        URLConnection con = yahooURL.openConnection();
        @Cleanup
        InputStreamReader ins = new InputStreamReader(con.getInputStream(), "UTF-8");
        @Cleanup
        BufferedReader in = new BufferedReader(ins);
        String newLine = in.readLine();
        while ((newLine = in.readLine()) != null) {
            datas.add(this.extractDividends(stockName, newLine));
        }

        return datas;
    }*/


    @SneakyThrows(IOException.class)
    public List<HistoricalData> getMarketData(String stockName, TimePeriod period, int historyDays){

        List<HistoricalData> datas = new ArrayList<>();

        String url = extractURL(stockName, period, historyDays, false);

        System.out.println("URL: " + url);

        URL yahooURL = new URL(url);
        URLConnection con = yahooURL.openConnection();
        @Cleanup
        InputStreamReader ins = new InputStreamReader(con.getInputStream(), "UTF-8");
        @Cleanup
        BufferedReader in = new BufferedReader(ins);
        String newLine = in.readLine();
        // 1st row is header
        while ((newLine = in.readLine()) != null) {
            datas.add(this.extractHistroricalMarketData(stockName, newLine, false, period));
        }

        return datas;
    }

    @SneakyThrows(IOException.class)
    public AbstractMarketData getTodayMarketData(String stockName) throws Exception {

        String response = "";
        String url = extractTodayURL(stockName);
        //http://download.finance.yahoo.com/d/quotes.csv?s=000002.SZ&f=spl1d1t1c1ohg

        URL yahooURL = new URL(url);
        URLConnection con = yahooURL.openConnection();
        @Cleanup
        InputStreamReader ins = new InputStreamReader(con.getInputStream(), "UTF-8");
        @Cleanup
        BufferedReader in = new BufferedReader(ins);
        response = in.readLine();

        if (response != null) {
            response = response.replace("\"", "");
        }

        return this.extractHistroricalMarketData(stockName, response, true, TimePeriod.LIVE);
    }

    @SneakyThrows(ParseException.class)
    private HistoricalData extractHistroricalMarketData(String code, String response, boolean isLive, TimePeriod timePeriod){

        if ( response == null || response.length() == 0 )
            return null;

        HistoricalData m = new HistoricalData();
        String[] data = response.split(RESPONSE_SPLIT);

        m.setCode(code);
        m.setName(code);
        m.setPeriod(timePeriod);

        SimpleDateFormat format = new SimpleDateFormat(isLive ? HistoricalData.LIVE_DATE_FORMAT : HistoricalData.MARKET_DATE_FORMAT);
        m.setDate(format.parse(data[0]));
        m.setOpen(Double.parseDouble(data[1]));
        m.setHigh(Double.parseDouble(data[2]));
        m.setLow(Double.parseDouble(data[3]));
        m.setClose(Double.parseDouble(data[4]));
        m.setVolume(Long.parseLong(data[5]));
        m.setAdjClose(Double.parseDouble(data[6]));


      /* if ( isLive ){
            m.setHigh52weeks(Double.parseDouble(data[7]));
            m.setLow52weeks(Double.parseDouble(data[8]));
        }*/

        return m;
    }

    //@SneakyThrows(ParseException.class)
    /*private Dividends extractDividends(String code, String response){

        if ( response == null || response.length() == 0 )
            return null;

        Dividends m = new Dividends();
        String[] data = response.split(RESPONSE_SPLIT);

        SimpleDateFormat format = new SimpleDateFormat(HistoricalData.MARKET_DATE_FORMAT);
        //m.setDate(format.parse(data[0]));
       // m.setDividends(Double.parseDouble(data[1]));

        return m;
    }*/

    public static String extractURL(String stockName, TimePeriod timePeriod, int historyDays, boolean isDividends){
        String url = "";
        String period = "";

        if ( historyDays > 0 ) {
            Calendar cal = Calendar.getInstance(Locale.CHINESE);
            int endMonth = cal.get(Calendar.MONTH);
            int endDay = cal.get(Calendar.DAY_OF_MONTH);
            int endYear = cal.get(Calendar.YEAR);

            cal.add(Calendar.DAY_OF_MONTH, 0 - historyDays);

            int beginMonth = cal.get(Calendar.MONTH);
            int beginDay = cal.get(Calendar.DAY_OF_MONTH);
            int beginYear = cal.get(Calendar.YEAR);
            period = "&a=" + beginMonth + "&b=" + beginDay + "&c=" + beginYear + "&d=" + endMonth + "&e=" + endDay + "&f=" + endYear;
        } else {
            // <=0 get all data
            period = "";
        }

        url = YAHOO_FINANCE_URL + "s=" + stockName + period + "&g=" + (isDividends ? "v" : timePeriod.getPeriod());

        return url;
    }



    public static String extractTodayURL(String stockName) {
       return YAHOO_FINANCE_URL_TODAY + "s=" + stockName + "&f=d1ohgl1vl1kj";

    }

    public static void main(String args[]) throws Exception {
        System.out.println(">>>>> Yahoo:");

        //MarketData data = new YahooMarketDataProvider().getTodayMarketData("000002.SZ");
        //System.out.println("Today: " + data);

        // 沪市后缀名.ss 例子： 沪深300 000300.ss ,深市后缀名 .sz 例子： 399106.sz
        //List<MarketData> market = new YahooMarketDataProvider().getMarketData("600577.ss", TimePeriod.DAILY, 10);
        //List<HistoricalData> market = new YahooMarketDataProvider().getMarketData("000001.ss", TimePeriod.WEEKLY, 60);
        //List<HistoricalData> market = new YahooMarketDataProvider().getMarketData("300168.sz", TimePeriod.DAILY, 10);

        List<HistoricalData> market = new YahooMarketDataProvider().getMarketData("600577.ss", TimePeriod.DAILY, 10);


        for ( AbstractMarketData m : market ){
            System.out.println(m.toString());
        }

    }

}
