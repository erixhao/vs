package com.vs.strategy.domain;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.enums.SortOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by erix-mac on 15/8/30.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketPeak implements Comparable<MarketPeak> {

    private Date begin;
    private Date end;

    private HistoricalData data;
    private Peak type;
    private int index;
    private double peak;

    private Weight weight;

    public void adjustRatio(float ratio) {
        this.weight.setRatio(ratio);
    }


    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat(HistoricalData.MARKET_DATE_FORMAT);
        String begin = format.format(this.begin);
        String end = format.format(this.end);

        return "stock: " + this.data.getCode() + " between (" + begin + " to " + end + "), " + this.data.period.toString() + " " + this.type.toString() + " date: " + format.format(this.data.getDate())
                + " " + (this.type.equals(Peak.TOP) ? "H" : "L") + " : " + new DecimalFormat("0.##").format(this.peak)
                + " weight: " + this.weight.toString();
    }

    public String toShortString() {
        SimpleDateFormat format = new SimpleDateFormat(HistoricalData.MARKET_DATE_FORMAT);
        String begin = format.format(this.begin);
        String end = format.format(this.end);

        return "{stock: " + this.data.getCode() + " date: " + format.format(this.data.getDate())
                + " " + (this.type.equals(Peak.TOP) ? "H" : "L") + " : " + new DecimalFormat("0.##").format(this.peak)
                + "}";
    }

    public static void printShortString(List<MarketPeak> peaks){
        for ( MarketPeak p : peaks ){
            System.out.print(p.toShortString() + ", ");
        }
    }

    @Override
    public int compareTo(MarketPeak o) {
        return this.data.getDate().compareTo(o.getData().getDate());
    }

    public final static Comparator<MarketPeak> weightComparator(final SortOrder order) {
        return new Comparator<MarketPeak>() {
            @Override
            public int compare(MarketPeak o1, MarketPeak o2) {
                return o1.getWeight().compareTo(o2.getWeight()) * order.getValue();


            }
        };
    }

    public final static Comparator<MarketPeak> peakComparator(final SortOrder order) {
        return new Comparator<MarketPeak>() {
            @Override
            public int compare(MarketPeak o1, MarketPeak o2) {
                return (int) (o1.getPeak() - o2.getPeak()) * order.getValue();


            }
        };
    }

    public final static List<MarketPeak> extractPeak(List<MarketPeak> marketPeaks) {
        return extractPeak(marketPeaks,0);
    }


    public final static List<MarketPeak> extractPeak(List<MarketPeak> marketPeaks, double weight) {
        List<MarketPeak> peaks = Lists.newArrayList();

        for (MarketPeak mp : marketPeaks) {

            if (mp.getWeight().totalWeight() >= weight) {
                peaks.add(mp);
            }
        }

        return peaks;
    }

    public final static List<Double> extractPeakNumber(List<MarketPeak> marketPeaks) {
        return extractPeakNumber(marketPeaks, 0);
    }


    public final static List<Double> extractPeakNumber(List<MarketPeak> marketPeaks, double weight) {
        List<Double> peaks = Lists.newArrayList();

        for (MarketPeak mp : marketPeaks) {

            if (mp.getWeight().totalWeight() >= weight) {
                peaks.add(mp.getPeak());
            }
        }

        return peaks;
    }
}


