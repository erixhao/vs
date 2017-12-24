package com.vs.market;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.LiveData;
import com.vs.common.domain.enums.MarketIndex;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.utils.*;
import com.vs.dao.MarketDataDAO;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.vs.common.utils.Constants.MARKET_FILE_LOCATION;


/**
 * Created by erix-mac on 15/8/6.
 */
@Service
@Slf4j
public class MarketDataService {

    private final static MarketDataDAO marketDataDAO;
    public final static int LAST_MONTHS_WINDOW = -25;
    //public final static int LAST_MONTHS_WINDOW = -160;
    private final static int LAST_6_MONTHS_WINDOW = -160;

    private final static Map<String, List<HistoricalData>> dailyMarketDatas;
    private final static Map<String, List<HistoricalData>> weeklyMarketDatas = Maps.newConcurrentMap();
    private final static Map<String, List<HistoricalData>> monthlyMarketDatas = Maps.newConcurrentMap();
    //private final static Map<String, List<Dividends>> dividends = Maps.newConcurrentMap();

    @Autowired
    private SinaMarketDataService liveDataService;

    @Autowired
    private MarketService marketService;

    public MarketDataService() {
    }

    static {
        marketDataDAO = BeanContext.getBean(MarketDataDAO.class);

        if ( "true".equalsIgnoreCase(PropertieUtils.getMarketProperty("env.default.load.market")) ){
            long begin = PerformanceUtils.beginTime("MarketDataService.getAllMarketDataMapFromDB");
            dailyMarketDatas = getAllMarketDataMapFromDB();
            PerformanceUtils.endTime("MarketDataService", begin);
        }else {
            dailyMarketDatas = Maps.newConcurrentMap();
            log.info("MarketDataService Do NOT load default Market Data");
        }

        ensureIndexsData();
        log.info("MarketDataService Map Size: " + dailyMarketDatas.size());
    }

    private static void ensureIndexsData(){
        String years = DateUtils.toMarketDate(DateUtils.getMarketDateByMonths(LAST_MONTHS_WINDOW));

        if ( dailyMarketDatas.get(MarketIndex.ShanghaiCompositeIndex.getSinaCode()) == null ){
            List<HistoricalData> sh = marketDataDAO.getAllMarketDataByYears(MarketIndex.ShanghaiCompositeIndex.getSinaCode(), years);
            dailyMarketDatas.put(MarketIndex.ShanghaiCompositeIndex.getSinaCode(), sh);
        }

        if ( dailyMarketDatas.get(MarketIndex.ShenzhenComponentIndex.getSinaCode()) == null ){
            List<HistoricalData> sz = marketDataDAO.getAllMarketDataByYears(MarketIndex.ShenzhenComponentIndex.getSinaCode(), years);
            dailyMarketDatas.put(MarketIndex.ShenzhenComponentIndex.getSinaCode(), sz);
        }

        if ( dailyMarketDatas.get(MarketIndex.GrowthEnterpriseIndex.getSinaCode()) == null ){
            List<HistoricalData> gw = marketDataDAO.getAllMarketDataByYears(MarketIndex.GrowthEnterpriseIndex.getSinaCode(), years);
            dailyMarketDatas.put(MarketIndex.GrowthEnterpriseIndex.getSinaCode(), gw);
        }
    }


    static Map<String, List<HistoricalData>> getAllMarketDataMapFromDB() {
        Map<String, List<HistoricalData>> maps = Maps.newConcurrentMap();

        String years = DateUtils.toMarketDate(DateUtils.getMarketDateByMonths(LAST_MONTHS_WINDOW));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>  Years: " + years);

        List<HistoricalData> datas;
        if ( Constants.isDevelopmentMode() ){
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>  ENV : DEV");
            List<String> stocks = PropertieUtils.getStockCodeList();
           /* if ( !stocks.contains(SHANGHAI_INDEX_CODE) ){
                stocks.add(SHANGHAI_INDEX_CODE);
            }*/

            datas = marketDataDAO.getAllMarketDataByYears(stocks,years);
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>  Count: " + datas.size());

        }else {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>  ENV : PRODUCTION");
            datas = marketDataDAO.getAllMarketDataByYears(years);
        }


        for (HistoricalData d : datas) {
            List<HistoricalData> stockData = maps.get(d.getCode());

            if (stockData == null) {
                maps.put(d.getCode(), new ArrayList<HistoricalData>());
            } else {
                stockData.add(d);
            }
        }

        return maps;
    }


    @SneakyThrows(IOException.class)
    static Map<String, List<HistoricalData>> getMarketHistoricalData(TimePeriod timePeriod) {
        Map<String, List<HistoricalData>> map = Maps.newHashMap();

        File marketRoot = new File(MARKET_FILE_LOCATION);
        File[] marketData = FileUtils.getMarketDataCSVFilePath(marketRoot, timePeriod, false);

        for (File f : marketData) {
            List<HistoricalData> list = Lists.newArrayList();

            String stockCode = FileUtils.getStockCode(f.getName());
            //log.info("Get History Data for " + stockCode);

            @Cleanup
            Reader in = new FileReader(f);

            @Cleanup
            final CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader());
            for (final CSVRecord record : parser) {
                list.add(extractMarketData(stockCode, record, timePeriod));
            }

            map.put(stockCode, list);
        }

        return map;
    }


    public List<HistoricalData> getShanghaiIndexHistoricalData() {
        return dailyMarketDatas.get(MarketIndex.ShanghaiCompositeIndex);
    }

    public List<HistoricalData> getMarketIndexHistoricalData(MarketIndex index) {
        return dailyMarketDatas.get(index.getSinaCode());
    }


    public List<HistoricalData> getMarketHistoricalData(String stockCode, TimePeriod timePeriod) {

        switch (timePeriod) {
            case DAILY:
                return dailyMarketDatas.get(stockCode);
            case WEEKLY:
                return weeklyMarketDatas.get(stockCode);
            case MONTHLY:
                return monthlyMarketDatas.get(stockCode);
        }

        return null;
    }

    public int size(TimePeriod timePeriod){
        switch (timePeriod) {
            case DAILY:
                return dailyMarketDatas.size();
            case WEEKLY:
                return weeklyMarketDatas.size();
            case MONTHLY:
                return monthlyMarketDatas.size();
        }

        return -1;
    }

    public long totalSize(){
        long total = 0;
        for ( String key : dailyMarketDatas.keySet() ){
            total += dailyMarketDatas.get(key).size();
        }

        return total;
    }

    /*public List<Dividends> getMarketDividends(String stockCode) {
        return dividends.get(stockCode);
    }*/


    public LiveData getMarketLiveData(MarketIndex index) {
        return this.liveDataService.getMarketData(index);
    }


    public Map<String, LiveData> getMarketLiveData(MarketIndex[] indexs) {
        return this.liveDataService.getMarketLiveData(indexs);
    }

    public LiveData getMarketLiveData(String stockName) {
        return this.liveDataService.getMarketLiveData(stockName).get(stockName);
    }

    public Map<String, LiveData> getMarketLiveData(String[] stockNames) {
        return this.liveDataService.getMarketLiveData(stockNames);
    }

    public List<HistoricalData> getMarketHistoricalData(String stockCode, TimePeriod timePeriod, Date begin, Date end) {
        List<HistoricalData> datas = this.getMarketHistoricalData(stockCode, timePeriod);
        return MarketDataUtils.extractByDate(datas, begin, end);
    }

    public HistoricalData getMarketHistoricalData(String stockCode, TimePeriod timePeriod, Date date) {

        if ( !MarketDataUtils.isTradingDate(date) )
            return null;

        List<HistoricalData> datas = this.getMarketHistoricalData(stockCode, timePeriod);
        HistoricalData market = MarketDataUtils.getMarketCurrent(datas, date);
        return market;
    }

    public void refreshMarketDataCache(String stock){
        String years = DateUtils.toMarketDate(DateUtils.getMarketDateByMonths(LAST_MONTHS_WINDOW));
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>> Code: " + stock + " Years: " + years);
        List<HistoricalData> datas = marketDataDAO.getAllMarketDataByYears(stock, years);

        if ( dailyMarketDatas.get(stock) != null ){
            dailyMarketDatas.remove(stock);
        }

        dailyMarketDatas.put(stock, datas);
    }

    public void refreshAllMarketDataFromDB(){

        log.info("refreshAllMarketDataFromDB begin....");

        Map<String, List<HistoricalData>> all = getAllMarketDataMapFromDB();

        for ( String stock : all.keySet() ){

            if ( dailyMarketDatas.get(stock) != null ){
                dailyMarketDatas.remove(stock);
            }

            dailyMarketDatas.put(stock, all.get(stock));
        }

        log.info("refreshAllMarketDataFromDB end.... Total: " + all.size());

    }

    @SneakyThrows(ParseException.class)
    private static HistoricalData extractMarketData(String code, CSVRecord record, TimePeriod timePeriod) {

        if (code == null || record == null)
            return null;

        HistoricalData m = new HistoricalData();

        m.setCode(code);
        m.setName(code);
        m.setPeriod(timePeriod);

        SimpleDateFormat format = new SimpleDateFormat(HistoricalData.MARKET_DATE_FORMAT);
        m.setDate(format.parse(record.get(0)));
        m.setOpen(Double.parseDouble(record.get(1)));
        m.setHigh(Double.parseDouble(record.get(2)));
        m.setLow(Double.parseDouble(record.get(3)));
        m.setClose(Double.parseDouble(record.get(4)));
        m.setVolume(Long.parseLong(record.get(5)));
        m.setAdjClose(Double.parseDouble(record.get(6)));

        return m;
    }

    /*private Dividends extractDividendsData(String code, CSVRecord record) {

        if (code == null || record == null)
            return null;

        Dividends m = new Dividends();
        SimpleDateFormat format = new SimpleDateFormat(HistoricalData.MARKET_DATE_FORMAT);
        return m;
    }*/

    public static void main(String args[]) throws Exception {
        System.out.println(">>>>> HistoricalDataService:");
        MarketDataService historicalDataService = BeanContext.getBean(MarketDataService.class);

        LiveData data = historicalDataService.getMarketLiveData("600399");
        System.out.println(data.toString());

       /* System.out.println(">>>>> HistoricalDataService:");
        List<HistoricalData> datas = historicalDataService.getMarketHistoricalData(StockIndex.ShanghaiCompositeIndex.getCode(), TimePeriod.DAILY);
        System.out.println("Daily " + StockIndex.ShanghaiCompositeIndex.getCode() + ": " + datas.size());*/
    }

}
