package com.vs.strategy.common;

import com.google.common.collect.Maps;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.BeanContext;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.MarketDataUtils;
import com.vs.market.MarketDataService;
import com.vs.strategy.domain.MarketIndicatorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static com.vs.common.utils.MarketDataUtils.indexBoundsCheck;


/**
 * Created by erix-mac on 2017/1/4.
 */
@Component
public class MarketTrendAnalyze {
    @Autowired
    protected MarketDataService marketDataService;
    private static final ConcurrentMap<String, BullBear> MARKET_TREND_MAP = Maps.newConcurrentMap();

    public MarketTrendAnalyze(){
    }

    public BullBear analysisTrend(String code, final Date date){
        List<HistoricalData> marketDatas = marketDataService.getMarketHistoricalData(code, TimePeriod.DAILY);
        return this.analysisAndPutCache(marketDatas,code, date);
    }

    public BullBear analysisTimeWindowTrend(String code, final Date date, TimeWindow window){
        List<HistoricalData> marketDatas = marketDataService.getMarketHistoricalData(code,TimePeriod.DAILY);

        if ( MARKET_TREND_MAP.size() == 0 ){
            initialization(code, window);
        }

        return this.analysisAndPutCache(marketDatas,code, date);
    }

    private void initialization(String code, TimeWindow timeWindow){
        if ( timeWindow == null )
            return;

        List<HistoricalData> marketDatas = marketDataService.getMarketHistoricalData(code,TimePeriod.DAILY);

        Date begin = timeWindow.getBegin();
        Date end = timeWindow.getEnd();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);

        while ( calendar.getTime().before(timeWindow.getEnd()) || calendar.getTime().equals(timeWindow.getEnd()) ) {
            analysisAndPutCache(marketDatas, code, calendar.getTime());
            calendar.add(Calendar.DATE, 1);
        }

    }

    private BullBear analysisAndPutCache(List<HistoricalData> marketDatas, String code, final Date date){
        String key = this.extractMarketTrendKey(code, date);
        BullBear trend = MARKET_TREND_MAP.get(key);

        if ( trend == null ){
            //System.out.println(" Date: " + date.toString());
            MarketIndicatorConfig ind = Stock.isIndex(code) ? MarketIndicatorConfig.getIndexIndicatorConfig() : MarketIndicatorConfig.getStockIndicatorConfig();
            trend = analysisTrend(this.selectMarketTrendDate(code,date), marketDatas, ind);

            MARKET_TREND_MAP.putIfAbsent(key, trend);
        }

        return trend;
    }


    public BullBear getCachedMarketTrend(String code, final Date date){
        final String key = this.extractMarketTrendKey(code, date);
        return MARKET_TREND_MAP.get(key);
    }

    public BullBear getCachedLastWeekMarketTrend(String code, final Date date){
        final String key = this.extractMarketTrendKey(code, DateUtils.nextDate(date,-7));
        return MARKET_TREND_MAP.get(key);
    }

    private Date selectMarketTrendDate(String code, final Date date){
        List<HistoricalData> marketDatas = marketDataService.getMarketHistoricalData(code,TimePeriod.DAILY);
        Date firstDayOfWeek = MarketDataUtils.getFirstTradeDateOfWeek(date);
        boolean isUseFirstDayOfWeek = this.isUseFirstDayOfWeekOrCurrentDate(marketDatas, firstDayOfWeek, date);

        Date anaysisDate = isUseFirstDayOfWeek ? firstDayOfWeek : date;

        return anaysisDate;
    }



    private String extractMarketTrendKey(String code, final Date date){
        return code + "_" + DateUtils.toMarketDate(selectMarketTrendDate(code,date));
    }

    private boolean isUseFirstDayOfWeekOrCurrentDate(List<HistoricalData> markets, Date firstDayOfWeek, Date currentDate){

        if ( firstDayOfWeek.equals(currentDate) )
            return true;

        HistoricalData firstDayOfWeekMarket = MarketDataUtils.getMarketCurrent(markets, firstDayOfWeek);
        HistoricalData currentMarket = MarketDataUtils.getMarketCurrent(markets, currentDate);

        if ( firstDayOfWeekMarket == null )
            return false;
        else if ( currentMarket == null )
            return true;
        else {
            double delt = (currentMarket.getClose() - firstDayOfWeekMarket.getClose()) / firstDayOfWeekMarket.getClose() * 100;
            return Math.abs(delt) <= 20;
        }
    }

    private BullBear analysisTrend(final Date date, List<HistoricalData> markets, MarketIndicatorConfig ind){
        BullBear trend = BullBear.NA;

        //Collections.sort(markets);
        Date anaysisDate = MarketDataUtils.getNextTradeDateIfCurrentNot(date);

        if ( DateUtils.isToday(date) || DateUtils.isFutureTradeDate(date) )
            anaysisDate = MarketDataUtils.getPreTradeDate(date);

        final HistoricalData mktData = MarketDataUtils.getMarketCurrent(markets,anaysisDate);

        int index = MarketDataUtils.indexOf(markets, anaysisDate);
        if ( index == -1 ){
            return BullBear.NA;
        }

        String DETAIL_STR = "|%10s|%7s|%15s|%7s|%8s|%7s|%8s|";
        Date beginDate = TimeWindow.getTimeWindow(TimePeriod.DAILY, anaysisDate,0,0,ind.getMktObsWindow()).getBegin();
        HistoricalData market = markets.get(indexBoundsCheck(--index,markets.size()));
        double delta = 0;
        BullBear lastWeekTrend = this.getCachedLastWeekMarketTrend(mktData.getCode(), date);

        // from given date back to last observation window start, to check.
        while ( market.getDate().after(beginDate) && index >=0 ){
            market = markets.get(index);
            delta = (mktData.getClose() - market.getClose()) / market.getClose() * 100;
            if ( delta >= ind.getBullMarketIndicator() ){
                if ( lastWeekTrend != null && lastWeekTrend.isBear() ){
                    trend = BullBear.VOLATILE;
                }else{
                    trend = this.analysisTrend(markets,mktData.getClose(),index,ind.getBigBullMarketIndicator(),beginDate, BullBear.BIGBULL) ? BullBear.BIGBULL : BullBear.BULL;
                }
                break;
            }else if ( delta <= ind.getBearMarketIndicator() ){
                if ( lastWeekTrend != null && lastWeekTrend.isBull() ){
                    trend = BullBear.VOLATILE;
                }else{
                    trend = this.analysisTrend(markets,mktData.getClose(),index,ind.getBigBearMarketIndicator(),beginDate, BullBear.BIGBEAR) ? BullBear.BIGBEAR : BullBear.BEAR;
                }

                break;
            }else{
                trend = BullBear.VOLATILE;
            }
            index --;
        }

        //System.out.println(String.format(DETAIL_STR,DateUtils.toMarketDate(anaysisDate),mktData.getCode(), trend.toString(),mktData.getClose(),DateUtils.toMarketDate(market.getDate()),market.getClose(),FORMAT.format(delta) + "%"));
        return trend;
    }

    private boolean analysisTrend(List<HistoricalData> markets, double todayClose, int index, double indicatorDelta, Date beginDate, BullBear trend){

        if ( markets == null || index == -1 || index == markets.size() )
            return false;

        HistoricalData market = markets.get(index);
        while ( market.getDate().after(beginDate) && index >=0 ){
            market = markets.get(index);
            double delta = (todayClose - market.getClose()) / market.getClose() * 100;

            if ( trend.isBull() ? delta >= indicatorDelta : delta <= indicatorDelta ){
                return true;
            }

            index --;
        }

        return false;
    }


    public String marketTrendRegression(String code, Date begin, Date end, TimePeriod period){

        StringBuilder sb = new StringBuilder();
        Date mktDate = begin;
        Calendar calendar = Calendar.getInstance();
        TimeWindow window = new TimeWindow(begin, end, period);

        String DETAIL_STR = "%10s|%7s|%3s";
        //System.out.println(String.format(DETAIL_STR,DateUtils.toMarketDate(anaysisDate),mktData.getCode(), trend.toString(),mktData.getClose(),DateUtils.toMarketDate(market.getDate()),market.getClose(),FORMAT.format(delta) + "%"));

        //System.out.println("Market Date|Code|Market");
        //sb.append("\n" + String.format(DETAIL_STR, "日期","代码","市场趋势"));
        while ( mktDate.before(end) || mktDate.equals(end) ){
            BullBear t = this.analysisTimeWindowTrend(code, mktDate, window);

            sb.append("\n" + String.format(DETAIL_STR,DateUtils.toMarketDate(mktDate),code, t.getChinese()));

            calendar.setTime(mktDate);
            calendar.add(period.getCalenda(), 1);
            mktDate = calendar.getTime();
        }

        return sb.toString();
    }

    public static void main(String[] args){
        MarketTrendAnalyze strategy = BeanContext.getBean(MarketTrendAnalyze.class);

        Date beginDate = DateUtils.toMarketDate("2015-05-01");
        Date endDate = DateUtils.toMarketDate("2017-01-10");

        strategy.marketTrendRegression("300431", beginDate, endDate, TimePeriod.WEEKLY);


    }
}
