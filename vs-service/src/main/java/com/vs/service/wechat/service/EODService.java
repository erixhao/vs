package com.vs.service.wechat.service;

import com.google.common.collect.Lists;
import com.vs.common.utils.BeanContext;
import com.vs.market.MarketService;
import com.vs.repository.MarketDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by erix-mac on 2017/1/14.
 */
@Slf4j
@Service
public class EODService {

    private final static int FIXED_EXECUTOR = 10;

    @Autowired
    private MarketService marketService;
    @Autowired
    private WeChatService weChatService;

    public void process(){
        process(MarketDataRepository.getAllExistingCodes());
    }

    public void process(String stocks){
        process(Lists.newArrayList(stocks.split(",")));
    }

    public void process(List<String> stocks){
        final ExecutorService executorService = Executors.newFixedThreadPool(FIXED_EXECUTOR);

        for (final String code : stocks ){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    weChatService.processUserAction(code);
                }
            });

        }
        log.info("WeChat EOD Process Successful with : " + stocks.size());
    }


    public static void main(String[] args){
        EODService eodService = BeanContext.getBean(EODService.class);

        //PropertieUtils.getStockCodeList()
        eodService.process(Lists.newArrayList("000518"));
    }
}
