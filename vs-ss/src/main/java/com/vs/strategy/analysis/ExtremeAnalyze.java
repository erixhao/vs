package com.vs.strategy.analysis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.common.domain.enums.SortOrder;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.MarketDataUtils;
import com.vs.market.MarketDataService;
import com.vs.strategy.domain.Dividends;
import com.vs.strategy.domain.MarketPeak;
import com.vs.strategy.domain.Peak;
import com.vs.strategy.domain.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by erix-mac on 15/8/28.
 */
@Component
public class ExtremeAnalyze {

    private final static float MINOR_TIME_WINDOW_GAP = 0.1f;
    @Autowired
    private MarketDataService marketDataService;
    @Autowired
    private Dividends dividends;

    public Map<Peak, List<MarketPeak>> determinMarketPeaks(Stock stock, TimeWindow timeWindow, int rank) {
        return determinMarketPeaks(stock,timeWindow.getBegin(),timeWindow.getEnd(),timeWindow.getPeriod(),rank);
    }

    public Map<Peak, List<MarketPeak>> determinMarketPeaks(Stock stock, Date begin, Date end, TimePeriod timePeriod, int rank) {

        List<HistoricalData> historicalDatas = this.marketDataService.getMarketHistoricalData(stock.getCode(), timePeriod, begin, end);
        return determinMarketPeaks(stock, historicalDatas, rank);
    }

    public Map<Peak, List<MarketPeak>> determinMarketPeaks(Stock stock, List<HistoricalData> historicalDatas, Date begin, Date end, int rank) {
        return determinMarketPeaks(stock, MarketDataUtils.extractByDate(historicalDatas, begin, end), rank);
    }

    private Map<Peak, List<MarketPeak>> determinMarketPeaks(Stock stock, final List<HistoricalData> datas, int top) {

        Collections.sort(datas);

        List<MarketPeak> peaks = Lists.newArrayList();
        HistoricalData previous = null;
        double previousSlope = 0;
        Date begin = datas.get(0).getDate();
        Date end = datas.get(datas.size() - 1).getDate();

        for (int i = 0; i < datas.size(); i++) {

            HistoricalData d = datas.get(i);

            if (previous == null) {
                previous = d;
                continue;
            }

            if (dividends.isSplitDate(stock, d.getDate())) {
                continue;
            }

            double slopeH = d.getHigh() - previous.getHigh();
            double slopeL = d.getLow()  - previous.getLow();

            boolean trendChanged = (slopeH * previousSlope < 0) || (slopeL * previousSlope < 0);
            double slope = trendChanged ? (slopeH * previousSlope < 0 ? slopeH : slopeL ) : slopeH;

            if ( trendChanged ) {
                Peak peakType = (slope > 0 ? Peak.BOTTOM : Peak.TOP);
                double peak = (peakType.equals(Peak.TOP) ? previous.getHigh() : previous.getLow());
                Weight weight = this.calcuatePeakWeight(datas, d.getDate(), peakType, peak);


                peaks.add(new MarketPeak(begin, end, previous, peakType, i, peak, weight));
            }
            previousSlope = slope;
            previous = d;

            // last one
            if (i == datas.size() - 1) {
                MarketPeak lastHigh = this.getLastPeak(peaks, Peak.TOP);
                MarketPeak lastLow = this.getLastPeak(peaks, Peak.BOTTOM);

                if ( lastLow != null && d.getLow() < lastLow.getPeak()) {
                    Weight weight = this.calcuatePeakWeight(datas, d.getDate(), Peak.BOTTOM, d.getLow());
                    peaks.add(new MarketPeak(begin, end, previous, Peak.BOTTOM, i, d.getLow(), weight));
                } else if ( lastHigh != null && d.getHigh() > lastHigh.getPeak()) {
                    Weight weight = this.calcuatePeakWeight(datas, d.getDate(), Peak.TOP, d.getHigh());
                    peaks.add(new MarketPeak(begin, end, previous, Peak.TOP, i, d.getHigh(), weight));
                }

            }
        }


        Map<Peak, List<MarketPeak>> map = toPeakMap(peaks);
        adjustPeakWeight(map, datas.size());

        return sortPeakMap(map, top);
    }

    private Map<Peak, List<MarketPeak>> adjustPeakWeight(final Map<Peak, List<MarketPeak>> peakListMap, int timeWindow) {

        List<MarketPeak> tops = peakListMap.get(Peak.TOP);
        List<MarketPeak> boms = peakListMap.get(Peak.BOTTOM);

        for (MarketPeak peak : tops) {
            if (isMinorPeak(tops, peak, timeWindow)) {
                peak.adjustRatio(MINOR_TIME_WINDOW_GAP);
            }
        }

        for (MarketPeak peak : boms) {
            if (isMinorPeak(boms, peak, timeWindow)) {
                peak.adjustRatio(MINOR_TIME_WINDOW_GAP);
            }
        }

        if ( tops.size() != 0 ){
            getTopest(tops).adjustRatio(1 / MINOR_TIME_WINDOW_GAP);
        }

        if ( boms.size() != 0 ){
            getTopest(boms).adjustRatio(1 / MINOR_TIME_WINDOW_GAP);

        }

        return peakListMap;
    }

    private static MarketPeak getTopest(final List<MarketPeak> peaks) {

        Peak type = peaks.get(0).getType();

        MarketPeak topest = peaks.get(0);
        for (MarketPeak peak : peaks) {
            if (type.equals(Peak.TOP)) {
                if (peak.getPeak() > topest.getPeak()) {
                    topest = peak;
                }
            } else if (type.equals(Peak.BOTTOM)) {
                if (peak.getPeak() < topest.getPeak()) {
                    topest = peak;
                }
            }
        }

        return topest;
    }

    private static boolean isMinorPeak(final List<MarketPeak> peaks, final MarketPeak current, int timeWindow) {

        Collections.sort(peaks);

        int len = peaks.size();
        for (int i = 0; i < peaks.size(); i++) {
            if (peaks.get(i).equals(current)) {

                MarketPeak left = peaks.get(i - 1 >= 0 ? (i - 1) : 0);
                MarketPeak right = peaks.get(i + 1 >= len ? (len - 1) : (i + 1));

                boolean leftGap = (current.getIndex() - left.getIndex()) / timeWindow < MINOR_TIME_WINDOW_GAP;
                boolean rightGap = (right.getIndex() - current.getIndex()) / timeWindow < MINOR_TIME_WINDOW_GAP;


                if (current.getType().equals(Peak.TOP)) {

                    if ((left.getPeak() > current.getPeak() && leftGap) || (right.getPeak() > current.getPeak() && rightGap)) {
                        return true;
                    }
                } else if (current.getType().equals(Peak.BOTTOM)) {

                    if ((left.getPeak() < current.getPeak() && leftGap) || (right.getPeak() < current.getPeak() && rightGap)) {
                        return true;
                    }
                }

                break;
            }
        }

        return false;
    }

    private static MarketPeak getLastPeak(List<MarketPeak> peaks, Peak type) {
        Collections.sort(peaks);

        for (int i = peaks.size() - 1; i >= 0; i--) {
            if (peaks.get(i).getType().equals(type)) {
                return peaks.get(i);
            }
        }

        return null;
    }


    private static Weight calcuatePeakWeight(List<HistoricalData> datas, Date currentDate, Peak type, double peak) {
        Collections.sort(datas);

        int current = MarketDataUtils.indexOf(datas, currentDate);
        Weight w = new Weight();

        // check past
        for (int i = current;i >= 0; i--) {
            if ( !MarketDataUtils.isTradingDate(datas.get(i)) )
                continue;

            boolean sameTrend = (type.equals(Peak.TOP) ? datas.get(i).getHigh() <= peak : datas.get(i).getLow() >= peak);
            if (sameTrend) {
                w.setLeftWeight(w.getLeftWeight() + 1);
            } else {
                break;
            }
        }

        // current
        // check after
        for (int i = current; i < datas.size(); i++) {
            if ( !MarketDataUtils.isTradingDate(datas.get(i)) )
                continue;

            boolean sameTrend = (type.equals(Peak.TOP) ? datas.get(i).getHigh() <= peak : datas.get(i).getLow() >= peak);

            if (sameTrend) {
                w.setRightWeight(w.getRightWeight() + 1);
            } else {
                break;
            }
        }

        return w;
    }


    private final static Map<Peak, List<MarketPeak>> sortPeakMap(final Map<Peak, List<MarketPeak>> peaks, int top) {

        Map<Peak, List<MarketPeak>> map = Maps.newHashMap();
        List<MarketPeak> h = peaks.get(Peak.TOP);
        List<MarketPeak> l = peaks.get(Peak.BOTTOM);


        Collections.sort(h, MarketPeak.weightComparator(SortOrder.DESC));
        Collections.sort(l, MarketPeak.weightComparator(SortOrder.DESC));

        if (top != -1) {
            h = h.subList(0, (top >= h.size() ? h.size() : top));
            l = l.subList(0, (top >= l.size() ? l.size() : top));
        }

        map.put(Peak.TOP, h);
        map.put(Peak.BOTTOM, l);

        return map;
    }


    public static void print(Map<Peak, List<MarketPeak>> peaks) {

        List<MarketPeak> high = peaks.get(Peak.TOP);
        List<MarketPeak> low = peaks.get(Peak.BOTTOM);

        System.out.println("\n\n >>>>>>>>>>>>>> PEAKS: >>>>>>>>>>>>>>");
        System.out.println(" >>>>>>>>>>>>>> HIGH >>>>>>>>>>>>>>");

        for (MarketPeak p : high) {
            System.out.println(p.toString());
        }
        System.out.println("\n >>>>>>>>>>>>>> LOW >>>>>>>>>>>>>>");

        for (MarketPeak p : low) {
            System.out.println(p.toString());
        }

        System.out.println(" >>>>>>>>>>>>>> PEAKS END >>>>>>>>>>>>>>");
    }

    private final static Map<Peak, List<MarketPeak>> toPeakMap(List<MarketPeak> peaks) {

        Map<Peak, List<MarketPeak>> map = new HashMap<>();
        map.put(Peak.TOP, new ArrayList<MarketPeak>());
        map.put(Peak.BOTTOM, new ArrayList<MarketPeak>());

        for (MarketPeak p : peaks) {

            if (p.getType().equals(Peak.TOP)) {
                map.get(Peak.TOP).add(p);
            } else if (p.getType().equals(Peak.BOTTOM)) {
                map.get(Peak.BOTTOM).add(p);
            }
        }

        Collections.sort(map.get(Peak.TOP));
        Collections.sort(map.get(Peak.BOTTOM));

        return map;
    }


}
