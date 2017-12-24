package com.vs.strategy.livermore;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * Created by erix-mac on 2017/9/9.
 */
@Data
@AllArgsConstructor
public class LivermoreFormula {

    private PivotalPoint live;


    public void udpateTrend(double price, double threshold, double confirmPoint){

        switch ( live.getCurrTrend() ){
            case UPWARD: // Rule 4.a; 6.a
                if ( isCriticalDown(price, live.getUpward().end, threshold) ){
                    if ( live.isLastTrendEquals(LiverTrend.NATURAL_REACTION) && isHigherThan(price, live.getLastTrendPointEnd()) ){
                        live.trendMove(LiverTrend.SECONDARY_REACTION,price);
                    }else{
                        live.trendMove(LiverTrend.NATURAL_REACTION,price);
                    }
                }else{
                    live.trendContinue(price);
                }
                break;
            case NATURAL_RALLY:// Rule 4.d
                if ( isCriticalDown(price, live.getNaturalRally().end, threshold) ){
                    // Rule 6.b
                    if ( live.getLastTrendPointTrend().isDown() && isCriticalDown(price, this.live.getLastTrendPoint().getKey(), confirmPoint)  ){
                        live.trendMove(LiverTrend.DOWNWARD,price);
                    }else if ( isLowerThan(price, live.getDownward().getKey()) ){
                        live.trendMove(LiverTrend.DOWNWARD,price);
                    } else if ( live.getLastTrendPointTrend().isDown() && isLowerThan(price, live.getLastTrendPointEnd()) ){
                        live.trendMove(LiverTrend.SECONDARY_REACTION,price);
                    } else if ( live.isLastTrendEquals(LiverTrend.NATURAL_REACTION)){
                        live.trendMove(LiverTrend.SECONDARY_REACTION,price);
                    }else {
                        live.trendMove(LiverTrend.NATURAL_REACTION,price);
                    }
                } else if ( live.getLastTrendPointTrend().isUp() && isCriticalUp(price, live.getNaturalRally().getKey(), confirmPoint)){
                    live.trendMove(LiverTrend.UPWARD,price);
                }else if ( live.isLastTrendEquals(LiverTrend.NATURAL_REACTION) && live.getLastTrendPoint().lastTrendPoint.equals(LiverTrend.UPWARD) && isHigherThan(price, live.getUpward().getKey()) ){
                    live.trendMove(LiverTrend.UPWARD,price);
                }
                else{
                    live.trendContinue(price);
                }
                break;
           case SECONDARY_RALLY:
                // Rule 6.g
                if ( isHigherThan(price, live.getNaturalRally().getKey()) ){
                    live.trendMove(LiverTrend.NATURAL_RALLY,price);
                }else {
                    live.trendContinue(price);
                }
                break;
            case DOWNWARD:// Rule 4.c && Rule 6.c
                if ( isCriticalUp(price, live.getDownward().getKey(), threshold) ){
                    if ( live.isLastTrendEquals(LiverTrend.NATURAL_RALLY) ){
                        live.trendMove(LiverTrend.SECONDARY_RALLY, price);
                    }else {
                        live.trendMove(LiverTrend.NATURAL_RALLY, price);
                    }
                    return;
                }else{
                    live.trendContinue(price);
                }
                break;
            case NATURAL_REACTION: // Rule 4.b && Rule 6.d
                if ( isCriticalUp(price, live.getNaturalReaction().end, threshold) ){
                    if ( live.isLastTrendEquals(LiverTrend.UPWARD) && isHigherThan(price, live.getLastTrendPointEnd())  ){
                        live.trendMove(LiverTrend.UPWARD, price);
                    }else if ( live.getLastTrendPointTrend().isUp() && isHigherThan(price, live.getNaturalRally().getKey()) ){
                        live.trendMove(LiverTrend.NATURAL_RALLY, price);
                    }else if ( isLowerThan(price, live.getNaturalRally().getKey()) ){
                        live.trendMove(LiverTrend.SECONDARY_RALLY, price);
                    }
                }else if ( isCriticalDown(price, live.getNaturalReaction().getKey(), confirmPoint) ){
                        live.trendMove(LiverTrend.DOWNWARD, price);
                }
                else if ( live.isLastTrendEquals(LiverTrend.NATURAL_RALLY) && live.getLastTrendPoint().lastTrendPoint.equals(LiverTrend.DOWNWARD) && isLowerThan(price, live.getDownward().getKey() )){
                    live.trendMove(LiverTrend.DOWNWARD, price);
                }
                else{
                    live.trendContinue(price);
                }

                break;
            case SECONDARY_REACTION:
                // Rule 6.h
                if ( isCriticalDown(price,live.getSecondaryReaction().begin, threshold) ){
                    if ( isLowerThan(price, live.getDownward().getKey()) ){
                        live.trendMove(LiverTrend.DOWNWARD, price);
                    }else{
                        live.trendMove(LiverTrend.NATURAL_REACTION, price);
                    }
                }else if ( isLowerThan(price, live.getDownward().getKey()) ){
                    live.trendMove(LiverTrend.DOWNWARD, price);
                }else if ( isCriticalDown(price,live.getNaturalReaction().getKey(), confirmPoint)){
                    live.trendMove(LiverTrend.DOWNWARD, price);
                }else if ( isLowerThan(price, live.getNaturalReaction().getKey()) ){
                    live.trendMove(LiverTrend.NATURAL_REACTION, price);
                }else if ( live.getLastTrendPointTrend().isUp() && isCriticalUp(price, live.getSecondaryReaction().end, threshold) ){
                    live.trendMove(LiverTrend.NATURAL_RALLY, price);
                }else {
                    live.trendContinue(price);
                }
                break;
            default:
                break;

        }
    }

    private boolean isCriticalUp(double price, double live, double critical){
        return (price >= live) && (Math.abs(price - live) >= critical);
    }

    private boolean isCriticalDown(double price, double live, double critical){
        return (price < live) && (Math.abs(live - price) >= critical);
    }

    private boolean isHigherThan(double price1, double price2){
        return price1 >= price2;
    }

    private boolean isLowerThan(double price1, double price2){
        return price1 < price2;
    }


    public static void main(String[] args) {

        List<Triple<String,Double,LiverTrend>> dataList = Lists.newArrayList(
                Triple.of("1938/03/23",47.0,  LiverTrend.DOWNWARD),
                Triple.of("1938/03/25",44.75, LiverTrend.DOWNWARD),
                Triple.of("1938/03/26",44.0,  LiverTrend.DOWNWARD),
                Triple.of("1938/03/28",44.625,LiverTrend.DOWNWARD),
                Triple.of("1938/03/29",39.625,LiverTrend.DOWNWARD),
                Triple.of("1938/03/30",39.0,  LiverTrend.DOWNWARD),
                Triple.of("1938/03/31",38.0,  LiverTrend.DOWNWARD),
                Triple.of("1938/04/02",43.5,  LiverTrend.NATURAL_RALLY),
                Triple.of("1938/04/09",46.5,  LiverTrend.NATURAL_RALLY),
                Triple.of("1938/04/13",47.25, LiverTrend.NATURAL_RALLY),
                Triple.of("1938/04/14",47.5,  LiverTrend.NATURAL_RALLY),
                Triple.of("1938/04/16",49.0,  LiverTrend.NATURAL_RALLY),
                Triple.of("1938/04/28",43.0,  LiverTrend.NATURAL_REACTION),
                Triple.of("1938/04/29",42.375,LiverTrend.NATURAL_REACTION),
                Triple.of("1938/05/02",41.5,  LiverTrend.NATURAL_REACTION),
                Triple.of("1938/05/25",41.375,LiverTrend.NATURAL_REACTION),
                Triple.of("1938/05/26",40.125,LiverTrend.NATURAL_REACTION),
                Triple.of("1938/05/27",39.875,LiverTrend.NATURAL_REACTION),
                Triple.of("1938/05/31",39.25, LiverTrend.NATURAL_REACTION),
                Triple.of("1938/06/20",45.375,LiverTrend.SECONDARY_RALLY),
                Triple.of("1938/06/21",46.5,  LiverTrend.SECONDARY_RALLY),
                Triple.of("1938/06/22",48.5,  LiverTrend.SECONDARY_RALLY),
                Triple.of("1938/06/23",51.0,  LiverTrend.NATURAL_RALLY),
                Triple.of("1938/06/24",53.75, LiverTrend.UPWARD),
                Triple.of("1938/06/25",54.875,LiverTrend.UPWARD),
                Triple.of("1938/06/29",56.875,LiverTrend.UPWARD),
                Triple.of("1938/06/30",58.375,LiverTrend.UPWARD),
                Triple.of("1938/07/01",59.0,  LiverTrend.UPWARD),
                Triple.of("1938/07/02",60.875,LiverTrend.UPWARD),
                Triple.of("1938/07/07",61.75, LiverTrend.UPWARD),
                Triple.of("1938/07/11",55.875,LiverTrend.NATURAL_REACTION),
                Triple.of("1938/07/12",55.5,  LiverTrend.NATURAL_REACTION),
                Triple.of("1938/07/19",62.375,LiverTrend.UPWARD),
                Triple.of("1938/07/25",63.25, LiverTrend.UPWARD),
                Triple.of("1938/08/12",56.625,LiverTrend.SECONDARY_REACTION),
                Triple.of("1938/08/13",56.5,  LiverTrend.SECONDARY_REACTION),
                Triple.of("1938/08/24",61.625,LiverTrend.NATURAL_RALLY),
                Triple.of("1938/08/26",61.875,LiverTrend.NATURAL_RALLY),
                Triple.of("1938/08/29",56.385,LiverTrend.SECONDARY_REACTION),
                Triple.of("1938/09/13",54.25, LiverTrend.NATURAL_REACTION),
                Triple.of("1938/09/14",52.00, LiverTrend.DOWNWARD),
                Triple.of("1938/09/20",57.625,LiverTrend.NATURAL_RALLY),
                Triple.of("1938/09/21",58.00 ,LiverTrend.NATURAL_RALLY),
                Triple.of("1938/09/24",51.875,LiverTrend.DOWNWARD),
                Triple.of("1938/09/28",50.875,LiverTrend.DOWNWARD),
                Triple.of("1938/09/29",57.125,LiverTrend.SECONDARY_RALLY),
                Triple.of("1938/09/30",59.25,LiverTrend.NATURAL_RALLY),
                Triple.of("1938/10/03",60.385,LiverTrend.NATURAL_RALLY),
                Triple.of("1938/10/05",62.000,LiverTrend.UPWARD),
                Triple.of("1938/11/12",71.250,LiverTrend.UPWARD),
                Triple.of("1938/11/18",65.125,LiverTrend.NATURAL_REACTION),
                Triple.of("1938/11/28",61.000,LiverTrend.NATURAL_REACTION),
                Triple.of("1938/12/15",67.125,LiverTrend.NATURAL_RALLY),
                Triple.of("1938/12/28",67.750,LiverTrend.NATURAL_RALLY),
                Triple.of("1939/01/04",70.000,LiverTrend.NATURAL_RALLY),
                Triple.of("1939/01/12",62.625,LiverTrend.SECONDARY_REACTION),
                Triple.of("1939/01/21",62.00,LiverTrend.SECONDARY_REACTION),
                Triple.of("1939/01/23",57.875,LiverTrend.DOWNWARD),
                Triple.of("1939/01/26",56.250,LiverTrend.DOWNWARD),
                Triple.of("1939/01/31",59.500,LiverTrend.NATURAL_RALLY),
                Triple.of("1939/01/31",59.500,LiverTrend.NATURAL_RALLY),
                Triple.of("1939/03/09",65.500,LiverTrend.NATURAL_RALLY),
                Triple.of("1939/03/16",59.500,LiverTrend.NATURAL_REACTION),
                Triple.of("1939/03/22",53.500,LiverTrend.NATURAL_REACTION),
                Triple.of("1939/03/30",52.125,LiverTrend.DOWNWARD)




                );


        //PivotalPoint live = PivotalPoint.of(62.125,62.125,0,48.25,0,0);
        PivotalPoint live = PivotalPoint.of(LiverTrend.DOWNWARD, 62.125,62.125,0,48.25,0,0);
        LivermoreFormula p = new LivermoreFormula(live);
        boolean isPass = true;
        for ( Triple<String,Double, LiverTrend> d : dataList ){
            if ( d.getLeft().equalsIgnoreCase("1939/03/22") ){
                System.out.println(">>>> DEBUG....");
            }

            p.udpateTrend(d.getMiddle(),5.12,3);

            if ( !p.live.getCurrTrend().equals(d.getRight()) ){
                isPass = false;
                System.out.println("Error : Date: " + d.getLeft() + " Expected : " + d.getRight() + " Actual: " + p.live.getCurrTrend() + "  Detail: " + p.live.getCurrTrend().toString() + "      >>>>>>>  [" + p.live.toString() + "]");
            }
        }

        if ( isPass ){
            System.out.println(">>>>> All Pass !");
        }


    }
}
