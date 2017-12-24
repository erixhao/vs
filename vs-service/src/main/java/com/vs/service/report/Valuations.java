package com.vs.service.report;


import com.vs.common.domain.TradingBook;
import com.vs.service.regression.RegressionResult;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by erix-mac on 16/7/20.
 */
public final class Valuations {


    public static double calcuate(List<RegressionResult> trades){
        double sum = 0;
        int count = 0;
        DecimalFormat format = new DecimalFormat("0.##");

        System.out.println("\n\n---------------------------------------------------------------");
        for ( RegressionResult r : trades){
            double timeWindowSum = 0;
            List<TradingBook> tradingBook = r.getTradingBooks();

            if ( tradingBook.size() == 0 )
                continue;

            for ( TradingBook t : tradingBook){
                sum += t.getPnL().getTotalProfitPercentage();
                timeWindowSum += t.getPnL().getTotalProfitPercentage();
                count ++;
            }
            System.out.println(r.getTimeWindow().toString() +  " Total: " + format.format(timeWindowSum)  + " Avg: " +  format.format(timeWindowSum/ tradingBook.size()) +  "--------");
        }

        double avg = count == 0 ? 1 : (sum / count);
        System.out.println("\n---------------------------------------------------------------");
        System.out.println("|Total: " + format.format(sum) + " Avg: "      + format.format(avg) + "--------");
        System.out.println("---------------------------------------------------------------");

        return sum;
    }
}
