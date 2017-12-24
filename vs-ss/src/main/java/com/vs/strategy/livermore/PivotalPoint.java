package com.vs.strategy.livermore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Created by erix-mac on 2017/9/9.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class PivotalPoint {
    // UPWARD(1),NATURAL_RALLY(2),SECONDARY_RALLY(3),DOWNWARD(-1),NATURAL_REACTION(-2),SECONDARY_REACTION(-3);
    private LiverTrend currTrend;

    private TrendPoint upward;
    private TrendPoint naturalRally;
    private TrendPoint secondaryRally;
    private TrendPoint downward;
    private TrendPoint naturalReaction;
    private TrendPoint secondaryReaction;

    public static PivotalPoint of(LiverTrend trend, double upward, double naturalRally, double secondaryRally, double downward , double naturalReaction, double secondaryReaction){
        PivotalPoint p = new PivotalPoint();

        p.currTrend = trend;
        p.upward = new TrendPoint(LiverTrend.UPWARD, upward,upward);
        p.naturalRally = new TrendPoint(LiverTrend.NATURAL_RALLY, naturalRally,naturalRally);
        p.secondaryRally = new TrendPoint(LiverTrend.SECONDARY_RALLY, secondaryRally,secondaryRally);
        p.downward = new TrendPoint(LiverTrend.DOWNWARD, downward,downward);
        p.naturalReaction = new TrendPoint(LiverTrend.NATURAL_REACTION, naturalReaction,naturalReaction);
        p.secondaryReaction = new TrendPoint(LiverTrend.SECONDARY_REACTION, secondaryReaction,secondaryReaction);

        return p;
    }

    public void trendContinue(double price){
        TrendPoint p = this.getTrendPoint(currTrend);

        switch ( currTrend ){
            case UPWARD:
            case NATURAL_RALLY:
            case SECONDARY_RALLY:
                if ( price > p.end ){
                    p.end = price;
                }
                break;
            case DOWNWARD:
            case NATURAL_REACTION:
            case SECONDARY_REACTION:
                if ( price < p.end ){
                    p.end = price;
                }
                break;
            default:
                break;
        }
    }

    private void trendEnd(LiverTrend curr, LiverTrend to, TrendPoint p){

        switch ( curr ){
            case UPWARD:
                if ( to.equals(LiverTrend.NATURAL_REACTION) ){
                    p.setKey(p.end);
                    //System.out.println("Curr Trend: " + curr.toString() + " Key: " + p.key);

                }
                break;
            case NATURAL_RALLY:
                if ( to.equals(LiverTrend.NATURAL_REACTION) || to.equals(LiverTrend.DOWNWARD) ){
                    p.setKey(p.end);
                    //System.out.println("Curr Trend: " + curr.toString() + " Key: " + p.key);
                }
                break;
            case SECONDARY_RALLY:
                break;
            case DOWNWARD:
                if ( to.equals(LiverTrend.NATURAL_RALLY) ){
                    p.setKey(p.end);
                    //System.out.println("Curr Trend: " + curr.toString() + " Key: " + p.key);
                }
                break;
            case NATURAL_REACTION:
                if ( to.equals(LiverTrend.NATURAL_RALLY) || to.equals(LiverTrend.UPWARD) ){
                    p.setKey(p.end);
                    //System.out.println("Curr Trend: " + curr.toString() + " Key: " + p.key);
                }
                break;
            case SECONDARY_REACTION:
                break;
            default:
        }

        p.lastEnd = p.end;
    }

    private void trendBegin(LiverTrend toTrend, double price){
        TrendPoint p = this.getTrendPoint(toTrend);

        p.trend = toTrend;
        p.begin = price;
        p.end = price;
        p.lastTrendPoint = ObjectUtils.cloneIfPossible(this.getTrendPoint(this.currTrend));
        this.currTrend = toTrend;
    }

    public void trendMove(LiverTrend to, double price){
        TrendPoint p = this.getCurrentTrendPoint();// current
        this.trendEnd(this.currTrend, to, p); // end current trend first then start new trend
        this.trendBegin(to,price);
    }

    public boolean isLastTrendEquals(LiverTrend trend){
        TrendPoint p = this.getCurrentTrendPoint();

        if ( p == null || p.lastTrendPoint == null ){
            return false;
        }else{
            return p.lastTrendPoint.trend.equals(trend);
        }
    }

    public TrendPoint getCurrentTrendPoint(){
        return this.getTrendPoint(this.currTrend);
    }

    public TrendPoint getLastTrendPoint(){
        return this.getCurrentTrendPoint().lastTrendPoint;
    }

    public double getLastTrendPointEnd(){
        TrendPoint t = this.getLastTrendPoint();
        return  t == null ? 0.0 : t.end;
    }

    public LiverTrend getLastTrendPointTrend(){
        TrendPoint t = this.getLastTrendPoint();
        return  t == null ? LiverTrend.NA : t.trend;
    }

    private TrendPoint getTrendPoint(LiverTrend trend){
        switch ( trend ){
            case UPWARD:
                return upward;
            case NATURAL_RALLY:
                return naturalRally;
            case SECONDARY_RALLY:
                return secondaryRally;
            case DOWNWARD:
                return downward;
            case NATURAL_REACTION:
                return naturalReaction;
            case SECONDARY_REACTION:
                return secondaryReaction;
            default:
                return null;
        }
    }

}

@AllArgsConstructor
 class TrendPoint{
    public LiverTrend trend;
    public double begin;
    public double end;
    private double key;
    public double lastEnd;
    public TrendPoint lastTrendPoint = null;

    public TrendPoint(LiverTrend trend, double begin, double end){
        this.trend = trend;
        this.begin = begin;
        this.end = end;
    }

    public double getKey(){
        return key == 0 ? end : key;
    }

    public void setKey(double key){
        this.key = key;
    }

    @Override
    public String toString(){
        StringBuilder s =  new StringBuilder();

        s.append("begin=").append(begin)
                .append(" end=").append(end)
                .append(" lastEnd=").append(lastEnd)
                .append(" trend=").append(trend.toString())
                .append(" lastTrendPoint=");
                //.append(lastTrendPoint == null ? "null" : lastTrendPoint.toString());

        return s.toString();
    }
}