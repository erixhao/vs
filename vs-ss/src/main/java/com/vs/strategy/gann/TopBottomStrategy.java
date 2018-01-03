package com.vs.strategy.gann;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradeAction;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.domain.enums.Trend;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.DateUtils;
import com.vs.repository.MarketDataRepository;
import com.vs.strategy.AbstractStrategy;
import com.vs.strategy.Strategy;
import com.vs.strategy.analysis.ExtremeAnalyze;
import com.vs.strategy.domain.MarketContext;
import com.vs.strategy.domain.MarketPeak;
import com.vs.strategy.domain.Peak;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by erix-mac on 15/10/25.
 */
@Slf4j
@Component
public class TopBottomStrategy extends AbstractStrategy implements Strategy {

    private final static int TIME_WINDOW = -7;

    private final static int TOP_RANK = 1;
    private final static int PEAK_DELTA_PERCENTAGE = 3;
    private final static int PEAK_CONFIRM_PERCENTAGE = 5;
    private final static int PEAK_CONFIRM_DATE = 7;
    private final static int PEAK_ANAYSIS_GAP = 7;
    @Autowired
    private ExtremeAnalyze extremeAnalyze;


    private final static int MARKET_UP_DOWN_COMPARE = -5;
    private final static int WEIGHT = 100;
    private int month = 0;

    private PeakMonitor peakMonitor;

    private Map<String, Map<TimeWindow, List<MarketPeak>>> topMap = Maps.newConcurrentMap();
    private Map<String, Map<TimeWindow, List<MarketPeak>>> bottomMap = Maps.newConcurrentMap();
    private LocalDate peakAnalyzeDate;

    private void cleanUp() {
        this.peakMonitor = null;
        this.peakAnalyzeDate = null;
        this.month = 0;
    }

    private List<MarketPeak> getPeaks(String code, TimeWindow window, Peak peak) {
        Map<TimeWindow, List<MarketPeak>> map = peak.equals(Peak.TOP) ? topMap.get(code) : bottomMap.get(code);
        return map == null ? null : map.get(window);
    }

    private void setPeaks(String code, TimeWindow window, Peak peak, LocalDate date, List<MarketPeak> peaks) {
        Map<TimeWindow, List<MarketPeak>> map = peak.equals(Peak.TOP) ? topMap.get(code) : bottomMap.get(code);

        if (map == null) {
            this.cleanUp();
            map = Maps.newConcurrentMap();
            map.put(window, peaks);
            if (peak.equals(Peak.TOP)) {
                topMap.put(code, map);
            } else {
                bottomMap.put(code, map);
            }
        } else {
            map.put(window, peaks);
        }

        this.peakAnalyzeDate = date;
    }


    private void initPeaksMap(Stock stock, TimeWindow window, LocalDate analysisDate) {

        List<MarketPeak> topPeaks = this.getPeaks(stock.getCode(), window, Peak.TOP);
        List<MarketPeak> bomPeaks = this.getPeaks(stock.getCode(), window, Peak.BOTTOM);
        long dateGap = Duration.between(this.peakAnalyzeDate, analysisDate).toDays();//DateUtils.daysBetween(this.peakAnalyzeDate, analysisDate);

        if ((topPeaks == null || topPeaks.size() == 0) || (bomPeaks == null || bomPeaks.size() == 0)) {
            extractUpdatePeaks(stock, window, analysisDate, this.month);
        }

        this.updateLatestTop(stock, window, analysisDate, this.getCurrentMarket(stock.getCode(), analysisDate));
    }

    private void extractUpdatePeaks(Stock stock, TimeWindow window, LocalDate analysisDate, int month) {
        Map<Peak, List<MarketPeak>> peaks = extremeAnalyze.determinMarketPeaks(stock, TimeWindow.getTimeWindow(TimePeriod.DAILY, analysisDate, TIME_WINDOW, month, 0), TOP_RANK);

        this.setPeaks(stock.getCode(), window, Peak.TOP, analysisDate, MarketPeak.extractPeak(peaks.get(Peak.TOP), WEIGHT));
        this.setPeaks(stock.getCode(), window, Peak.BOTTOM, analysisDate, MarketPeak.extractPeak(peaks.get(Peak.BOTTOM), WEIGHT));
    }


    @Override
    public String getName() {
        return Strategies.TopBottomStrategy.toString();
    }

    @Override
    public List<TradeAction> execute(MarketContext context) {
        List<TradeAction> result = Lists.newArrayList();

        Stock stock = context.getStock();
        LocalDate date = context.getAnalysisDate();
        TradingBook tradingBook = context.getTradingBook();
        TimeWindow window = context.getTimeWindow();

        HistoricalData market = this.getMarketDataT(context);
        LocalDate today = date;

        this.initPeaksMap(stock, window, date);

        double marketPrice = context.getMarketPrice();
        TradeAction action = new TradeAction(Strategies.TopBottomStrategy, TradeDirection.NONE, stock, today, today, marketPrice);
        if (isTriggerTrade(stock, window, action, date, market)) {
            System.out.println("----------------------------------------->>>>> TopBottomStrategy : Today: " + today + "  Action: " + action.getTradeDirection().toString() + " market: " + marketPrice);
            result.add(action);
        }

        return result;
    }


    private void updateLatestTop(Stock stock, TimeWindow window, LocalDate date, HistoricalData data) {
        List<MarketPeak> top = this.getPeaks(stock.getCode(), window, Peak.TOP);
        List<MarketPeak> bom = this.getPeaks(stock.getCode(), window, Peak.BOTTOM);
        double market = data.getClose();

        boolean max = true;
        for (MarketPeak p : top) {
            if (p.getPeak() > market) {
                max = false;
                break;
            }
        }

        if (max) {
            top.get(top.size() - 1).setPeak(market);
            top.get(top.size() - 1).setData(data);
            this.peakAnalyzeDate = date;
        }

        boolean min = true;
        for (MarketPeak p : bom) {
            if (p.getPeak() < market) {
                min = false;
                break;
            }
        }

        if (min) {
            bom.get(bom.size() - 1).setPeak(market);
            bom.get(bom.size() - 1).setData(data);
            this.peakAnalyzeDate = date;
        }
    }


    private boolean isTriggerTrade(Stock stock, TimeWindow window, TradeAction action, LocalDate analysisDate, HistoricalData market) {
        List<MarketPeak> top = this.getPeaks(stock.getCode(), window, Peak.TOP);
        List<MarketPeak> bom = this.getPeaks(stock.getCode(), window, Peak.BOTTOM);

        Trend trend = this.getMarketTrend(stock.getCode(), analysisDate, market.getClose());
        action.setTradeDirection(TradeDirection.NONE);
        PeakTradeResult result;
        //double marketPrice = (trend.equals(Trend.UP) ? market.getHigh() : market.getLow());
        double marketPrice = market.getClose();

        if (peakMonitor == null) {
            result = detectPeakTrade(market, (trend.equals(Trend.UP) ? top : bom), trend);
            if (result.isClosePeak) {
                peakMonitor = new PeakMonitor((trend.equals(Trend.UP) ? Peak.TOP : Peak.BOTTOM), analysisDate, result.peak, marketPrice, trend);
            }
        } else if (comfirmPeakTrade(peakMonitor, analysisDate, marketPrice)) {

            switch (this.peakMonitor.trend) {
                case UP:
                    action.setTradeDirection(TradeDirection.SELL);
                    break;
                case DOWN:
                    action.setTradeDirection(TradeDirection.BUY);
                    break;
            }
        }

        TradeDirection direction = action.getTradeDirection();
        boolean isTrigger = direction.equals(TradeDirection.BUY) || direction.equals(TradeDirection.SELL);

        if (isTrigger) {
            System.out.println("CONFIRMED --------");
            MarketPeak.printShortString(getPeaks(stock.getCode(), window, Peak.TOP));
            MarketPeak.printShortString(getPeaks(stock.getCode(), window, Peak.BOTTOM));
            System.out.println("\n*******************peakMonitor: " + this.peakMonitor.toString());
            peakMonitor = null;
        }

        return isTrigger;
    }

    private boolean comfirmPeakTrade(PeakMonitor peakMonitor, LocalDate analysisDate, double market) {
        long dayGap = Duration.between(peakMonitor.getMonitorDate(), analysisDate).toDays();
        if (dayGap > PEAK_CONFIRM_DATE) {
            this.peakMonitor = null;
            return false;
        }

        boolean isConfrimed = false;
        this.peakMonitor.updatePeakMonitorIfRequired(analysisDate, market);
        double delta = market - peakMonitor.getMarketPrice();
        double percentage = ((market - peakMonitor.getMarketPrice()) / peakMonitor.getMarketPrice()) * 100;
        switch (peakMonitor.getTrend()) {
            case UP:
                isConfrimed = delta < 0 && percentage <= PEAK_CONFIRM_PERCENTAGE * -1;
                break;
            case DOWN:
                isConfrimed = delta > 0 && percentage >= PEAK_CONFIRM_PERCENTAGE * 1;
                break;
        }

        return isConfrimed;
    }


    private Trend getMarketTrend(String code, LocalDate today, double market) {

        Trend trend = Trend.NONE;

        HistoricalData m = MarketDataRepository.getMarketDataBy(code, today.plusDays(MARKET_UP_DOWN_COMPARE));
        //this.marketService.getMarketHistoricalData(code, TimePeriod.DAILY, today.plusDays(MARKET_UP_DOWN_COMPARE));

        if (m != null) {
            trend = market > m.getClose() ? Trend.UP : Trend.DOWN;
        }

        return trend;
    }

    private PeakTradeResult detectPeakTrade(HistoricalData market, List<MarketPeak> peaks, Trend trend) {

        PeakTradeResult result = new PeakTradeResult(false, -1, trend);
        for (MarketPeak p : peaks) {
            result.setPeak(p.getPeak());
            //double marketPrice = trend.equals(Trend.UP) ? market.getHigh() : market.getLow();
            double marketPrice = market.getClose();
            double delta = ((marketPrice - p.getPeak()) / p.getPeak()) * 100;
            result.setClosePeak(Math.abs(delta) <= PEAK_DELTA_PERCENTAGE);

            if (result.isClosePeak) {
              /*  System.out.println(">>>>>>>>>>  Current Market " + market.getDate().toString() + " Pice: " + marketPrice + " Close To: " + p.getData().getDate().toString() + " " + p.getPeak() + " Delta: " + delta);
                MarketPeak.printShortString(peaks);
                System.out.println("\n>>>>>>>>>>  Current Market End " + marketPrice);*/

                break;
            }
        }

        return result;
    }

}

@Data
@AllArgsConstructor
class PeakTradeResult {
    public boolean isClosePeak;
    public double peak;
    public Trend trend;
}

@Data
@AllArgsConstructor
class PeakMonitor {
    public Peak peakType;
    public LocalDate monitorDate;
    public double peak;
    public double marketPrice;
    public Trend trend;

    private boolean isGreater(double market) {
        return peakType.equals(Peak.TOP) ? market > marketPrice : market < marketPrice;
    }

    public void updatePeakMonitorIfRequired(LocalDate date, double marketPrice) {
        if (isGreater(marketPrice)) {
            this.monitorDate = date;
            this.marketPrice = marketPrice;
        }
    }
}