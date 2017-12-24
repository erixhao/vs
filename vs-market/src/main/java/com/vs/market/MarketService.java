package com.vs.market;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.utils.BeanContext;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.MarketDataUtils;
import com.vs.dao.MarketDataDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 16/1/14.
 */
@Service
@Slf4j
public class MarketService {

    @Autowired
    private MarketDataDAO marketDataDAO;
    @Autowired
    private SinaMarketDataService sinaMarketDataService;

    @Autowired
    private YahooMarketDataService yahooMarketDataService;
    @Autowired
    private MarketDataService marketDataService;


    public boolean hasMarketData(String code) {
        return this.marketDataDAO.getMarketCount(code) > 0;
    }

    public void updateMarketData(List<String> codes, int lastDays){
        for ( String s : codes ){
            this.updateMarketData(s, lastDays);
        }
    }

    public void updateMarketData(String code, int lastDays) {
        if ( !Stock.isValidStockCode(code) )
            return;

        List mkt = this.marketDataService.getMarketHistoricalData(code, TimePeriod.DAILY);
        boolean isNotInCache = mkt == null || mkt.size() == 0;

        if ( hasLatestMarketData(code) ) {
            if ( isNotInCache ){
                this.marketDataService.refreshMarketDataCache(code);
            }

            return;
        }

        boolean hasMarket = this.hasMarketData(code);
        int updateCount = 0;

        if (hasMarket) {
            log.info(">>>>>>>> updateMarketDataIncremental :  " + code);
            updateCount = this.updateMarketDataIncremental(code);
        } else {
            log.info(">>>>>>>> insertAllMarketData :  " + code);
            updateCount = this.insertAllMarketData(code, lastDays);
        }

        if ( updateCount > 0 ){
            this.marketDataService.refreshMarketDataCache(code);
        }

        if ( isNotInCache ){
            this.marketDataService.refreshMarketDataCache(code);
        }

        log.info("updateMarketData : " + code + " successfully with count: " +  updateCount + ".");
    }

    public List<String> getAllExistingCodes() {
        return this.marketDataDAO.getAllExistingCodes();
    }

    private boolean hasLatestMarketData(String code) {
        Date now = DateUtils.timeZoneDate(Calendar.getInstance().getTime());

        Date tradeDate = MarketDataUtils.isTradingDate(now) ? now : DateUtils.timeZoneDate(MarketDataUtils.getPreTradeDate());
        return this.marketDataDAO.getMarketCount(code, HistoricalData.toMarketDate(tradeDate)) > 0;


    }

    private int insertAllMarketData(String code, int lastDays) {
        List<HistoricalData> mktData;

        if ( lastDays != -1 ){
            mktData = this.sinaMarketDataService.getMarketDataIncremental(code, DateUtils.getLastDate(lastDays));
        }else {
            mktData = this.sinaMarketDataService.getMarketData(code);
        }

        this.marketDataDAO.insert(mktData);
        return mktData.size();
    }

    private int updateMarketDataIncremental(String code) {
        Date currMarketDate = this.marketDataDAO.getLatestMarketData(code).getDate();
        List<HistoricalData> mktData = this.sinaMarketDataService.getMarketDataIncremental(code, currMarketDate);
        List<HistoricalData> incremental = Lists.newArrayList();

        if ( mktData == null || mktData.size() == 0 )
            return 0;

        for (HistoricalData d : mktData) {
            if (d.getDate().after(currMarketDate)) {
                incremental.add(d);
            }
        }

        if ( incremental.size() == 0 ){
           // no need to insert to db
        }else{
            this.marketDataDAO.insert(incremental);
        }

        return incremental.size();
    }

    public static void main(String[] args) {
        MarketService marketService = BeanContext.getBean(MarketService.class);

        //marketService.updateMarketData("300372");
        System.out.println(marketService.hasLatestMarketData("000980"));


    }
}
