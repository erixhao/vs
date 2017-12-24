package com.vs.strategy.index;

import com.google.common.collect.Maps;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.MarketIndex;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.BeanContext;
import com.vs.common.utils.DateUtils;
import com.vs.strategy.common.MarketTrendAnalyze;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Created by erix-mac on 16/2/29.
 */
@Component
public class IndexTrendStrategy {

    @Autowired
    @Getter
    private MarketTrendAnalyze marketTrendAnalyze;

    public IndexTrendStrategy(){
    }


    public BullBear analysisTrend(final Date date){
        return marketTrendAnalyze.analysisTrend(MarketIndex.ShanghaiCompositeIndex.getSinaCode(),date);
    }

    public BullBear analysisTimeWindowTrend(final Date date, TimeWindow window){
        return marketTrendAnalyze.analysisTimeWindowTrend(MarketIndex.ShanghaiCompositeIndex.getSinaCode(),date, window);
    }

    public Map<MarketIndex,BullBear> analysisAllIndex(final Date date){

        Map<MarketIndex,BullBear> map = Maps.newConcurrentMap();

        BullBear shIndex = marketTrendAnalyze.analysisTrend(MarketIndex.ShanghaiCompositeIndex.getSinaCode(),date);
        BullBear szIndex = marketTrendAnalyze.analysisTrend(MarketIndex.ShenzhenComponentIndex.getSinaCode(),date);
        BullBear geIndex = marketTrendAnalyze.analysisTrend(MarketIndex.GrowthEnterpriseIndex.getSinaCode(),date);

        map.put(MarketIndex.ShanghaiCompositeIndex,shIndex);
        map.put(MarketIndex.ShenzhenComponentIndex,szIndex);
        map.put(MarketIndex.GrowthEnterpriseIndex,geIndex);

        return map;
    }

    public String marketRegression(String code, Date begin, Date end, TimePeriod period){
        return this.marketTrendAnalyze.marketTrendRegression(code, begin, end, period);
    }



    public static void main(String[] args){
        IndexTrendStrategy strategy = BeanContext.getBean(IndexTrendStrategy.class);

        Date beginDate = DateUtils.toMarketDate("2017-05-01");
        Date endDate = DateUtils.toMarketDate("2017-06-08");

        //strategy.getMarketTrendAnalyze().marketTrendRegression(StockIndex.ShanghaiCompositeIndex.getSinaCode(), beginDate, endDate, TimePeriod.WEEKLY);
        //strategy.getMarketTrendAnalyze().marketTrendRegression(StockIndex.ShenzhenComponentIndex.getSinaCode(), beginDate, endDate, TimePeriod.WEEKLY);
        //strategy.getMarketTrendAnalyze().marketTrendRegression(StockIndex.ShanghaiShenzhen300Index.getSinaCode(), beginDate, endDate, TimePeriod.WEEKLY);
        //strategy.getMarketTrendAnalyze().marketTrendRegression(StockIndex.GrowthEnterpriseIndex.getSinaCode(), beginDate, endDate, TimePeriod.DAILY);
        strategy.getMarketTrendAnalyze().marketTrendRegression(MarketIndex.Shanghai50Index.getSinaCode(), beginDate, endDate, TimePeriod.DAILY);


    }

}
