package com.vs.strategy.analysis;

import com.google.common.collect.Maps;
import com.vs.common.domain.Stock;
import com.vs.strategy.domain.MarketPeak;
import com.vs.strategy.domain.Peak;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Created by erix-mac on 15/11/1.
 */
@Service
public class MarketPeaksService {

    private static Map<Stock, Map<Peak, List<MarketPeak>>> stockPeaks_1Year = Maps.newConcurrentMap();
    private static Map<Stock, Map<Peak, List<MarketPeak>>> stockPeaks_3Year = Maps.newConcurrentMap();


    @PostConstruct
    private void init() {
    }

}
