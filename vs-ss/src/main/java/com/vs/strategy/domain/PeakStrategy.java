package com.vs.strategy.domain;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by erix-mac on 15/11/8.
 */
public class PeakStrategy {

    public Double peak;
    public boolean traded;

    public PeakStrategy(Double peak, boolean traded){
        this.peak = peak;
        this.traded = traded;
    }

    public static List<PeakStrategy> newPeakStrategy(List<Double> peaks){

        List<PeakStrategy> peakStrategies = Lists.newArrayList();
        for ( Double p : peaks ){
            peakStrategies.add(new PeakStrategy(p,false));
        }

        return peakStrategies;
    }

    public static List<PeakStrategy> updateTops(List<PeakStrategy> peaks, List<Double> tops){

        if ( peaks.size() != tops.size() )
            throw new RuntimeException("Peak Top Size not same!");

        for ( int i=0;i<tops.size();i++ ){
            peaks.get(i).peak = tops.get(i);
        }

        return peaks;
    }

    public static List<Double> toPeaks(List<PeakStrategy> peakStrategies){
        List<Double> peaks = Lists.newArrayList();

        if ( peakStrategies == null || peakStrategies.size() == 0 )
            return peaks;

        for ( PeakStrategy p : peakStrategies ){
            peaks.add(p.peak);
        }

        return peaks;
    }


    @Override
    public String toString() {
        return "PeakStrategy{" +
                "peak=" + peak +
                ", traded=" + traded +
                '}';
    }
}
