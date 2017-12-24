package com.vs.market;


import com.vs.common.domain.enums.TimePeriod;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by erix-mac on 15/8/1.
 */
@Component
@Getter @Setter
@Slf4j
public class MarketDataDownloadTask implements Runnable{

    private static int BUFFER_SIZE = 8096;

    private String stockName;
    private String code;
    private TimePeriod timePeriod;
    private int historyDays;
    private String fileName;
    private boolean isDividends;

    @Autowired
    private YahooMarketDataService marketDataService;

    private MarketService marketService;

    public MarketDataDownloadTask(){}

    public MarketDataDownloadTask(String stockName, String code, TimePeriod period, int historyDays, String fileName, boolean isDividends){
        this.stockName = stockName;
        this.code = code;
        this.timePeriod = period;
        this.historyDays = historyDays;
        this.fileName = fileName;
        this.isDividends = isDividends;
    }

    public static MarketDataDownloadTask newTask(String stockName,String code, TimePeriod period, int historyDays, String fileName, boolean isDividends){
        return new MarketDataDownloadTask(stockName,code, period,historyDays,fileName,isDividends);
    }

    @Override
    public void run() {
        this.downloadMarketDataToDB();
    }

    public void downloadMarketDataToDB(){
        this.marketService.updateMarketData(this.code, this.historyDays);
    }


    @SneakyThrows(IOException.class)
    public void downloadMarketDataToFile(String marketURL, String fileName) {

        HttpURLConnection httpUrl = null;
        byte[] buf = new byte[BUFFER_SIZE];
        int size = 0;

        try{
            URL url = new URL(marketURL);
            httpUrl = (HttpURLConnection) url.openConnection();
            log.info("connecting....." + marketURL);
            httpUrl.connect();
            log.info("connected....." + marketURL);

            @Cleanup
            BufferedInputStream bis = new BufferedInputStream(httpUrl.getInputStream());
            @Cleanup
            FileOutputStream fos = new FileOutputStream(fileName);
            while ( (size = bis.read(buf)) != -1 ) {
                fos.write(buf, 0, size);
            }

            log.info("downloading done....." + marketURL);

        } finally {
            httpUrl.disconnect();
        }
    }

}
