package com.vs.service.wechat.service;

import com.google.common.collect.Maps;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.MarketDataUtils;
import com.vs.service.wechat.domain.vo.CacheSignal;
import com.vs.service.wechat.domain.vo.TradeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * Created by erix-mac on 2017/1/14.
 */
@Slf4j
@Service
public class TradeCacheService{

    private final static double LAST_YEAR_TRDE_PROFIT = 20;

    private final Map<String,TradeResult> TRADE_CACHE = Maps.newConcurrentMap();

    public int size(){
        return TRADE_CACHE.size();
    }

    public Map<String,TradeResult> getTradeCache(){
        return TRADE_CACHE;
    }

    public TradeResult get(String code){
        return TRADE_CACHE.get(code);
    }

    public void put(String code, TradeResult result){
        final  LocalDate date = result.getTradeDate();

        if ( isCached(code,date) )
            return;

        LocalDate tradingDate = date;
        if ( !MarketDataUtils.isTradingDate(date) ){
            tradingDate = MarketDataUtils.getNextTradeDate(date);
        }
        TRADE_CACHE.put(code,new TradeResult(result.isTradeSingal(), result.isBuySingal(), tradingDate,result.getResponse(), result.getProfit()));
    }

    public boolean isCached(String code, LocalDate date){
        LocalDate tradingDate = date;

        if ( !MarketDataUtils.isTradingDate(tradingDate) ){
            tradingDate = MarketDataUtils.getNextTradeDate(tradingDate);
        }

        TradeResult r = this.get(code);
        return r != null && DateUtils.toString(r.getTradeDate()).equalsIgnoreCase(DateUtils.toString(tradingDate));
    }

    public Map<String,TradeResult> randomTradeCache(int num, CacheSignal cacheSignal){
        Map<String,TradeResult> randomMap = Maps.newConcurrentMap();

        int size = TRADE_CACHE.size() - 1;
        if ( size <= 0 )
            return randomMap;

        int count = 0;
        int randomIndex = new Random().nextInt(size);

        for ( String k : TRADE_CACHE.keySet() ){
            count++;

            // after randomIndex, to check
            if ( count >= randomIndex ){
                TradeResult result = TRADE_CACHE.get(k);

                switch ( cacheSignal ){
                    case BUY:
                        if ( result.isBuySingal() ){
                            randomMap.put(k, result);
                        }
                        break;
                    case TRADE:
                        if ( result.isTradeSingal() ){
                            randomMap.put(k, result);
                        }
                        break;
                    case GREAT_PROFIT:
                        if ( result.getProfit() >= LAST_YEAR_TRDE_PROFIT ){
                            randomMap.put(k, result);
                        }
                        break;
                    default:
                        break;
                }

                if ( randomMap.size() >= num )
                    break;
            }
        }

        return randomMap;
    }


}
