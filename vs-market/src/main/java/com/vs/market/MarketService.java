package com.vs.market;

import com.vs.repository.MarketDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by erix-mac on 16/1/14.
 */
@Service
@Slf4j
public class MarketService {

    public boolean hasMarketData(String code) {
        return MarketDataRepository.getMarketCount(code) > 0;
    }

    public void updateMarketData(List<String> codes, LocalDate date) {
        for (String s : codes) {
            this.updateMarketData(s, date);
        }
    }

    public void updateMarketData(String code, LocalDate date) {
        DownloadTask.downloadHistoryDataTask(code, date);
    }

    public void updateMarketData(List<String> codes){
        updateMarketData(codes, LocalDate.now());
    }
}
