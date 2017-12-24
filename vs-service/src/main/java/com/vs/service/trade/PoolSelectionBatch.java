package com.vs.service.trade;


import com.vs.common.domain.Stock;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.utils.BeanContext;
import com.vs.common.utils.PropertieUtils;
import com.vs.strategy.common.MarketTrendAnalyze;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 2017/1/12.
 */
@Slf4j
@Service
public class PoolSelectionBatch {

    @Autowired
    private MarketTrendAnalyze marketTrendAnalyze;

    public void selectBatch(){

        Date today = Calendar.getInstance().getTime();
        List<Stock> stocksList = PropertieUtils.getStockList();

        for (Stock s : stocksList){
            BullBear t = this.marketTrendAnalyze.analysisTrend(s.getCode(), today);
            //t.printMarketTrend(t, today, s.getCode());
            if ( t.isBull() )
                System.out.print(s.getCode() + ",");
        }
    }

    public static void main(String[] args ){
        log.info("Daily PoolSelectionBatch Starting....\n");

        PoolSelectionBatch selectionBatch = BeanContext.getBean(PoolSelectionBatch.class);
        selectionBatch.selectBatch();

        log.info("\nDaily PoolSelectionBatch End.");
    }

}
