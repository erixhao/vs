package com.vs.common.utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by erix-mac on 2016/12/7.
 */
@Data
public final class SHSZStockUtils {

    public static List<String> getAllSHStock(){
        List<String> stocks = new ArrayList<>(3000);

        int begin601Code = 600000;
        final int count601 = 1999;

        for ( int i=0;i<=count601;i++ ){
            stocks.add(String.valueOf(begin601Code++));
        }

        int begin603Code = 603000;
        final int count603 = 999;
        for ( int i=0;i<=count603;i++ ){
            stocks.add(String.valueOf(begin603Code++));
        }

        return stocks;
    }

    public static List<String> getAllSZStock(){
        List<String> stocks = new ArrayList<>(3000);

        for ( int i=0;i<=166;i++ ){
            stocks.add(padLeft(String.valueOf(i),6,'0'));
        }

        stocks.add("000301");
        stocks.add("000333");
        stocks.add("000338");

        for ( int i=400;i<=2800;i++ ){
            stocks.add(padLeft(String.valueOf(i),6,'0'));
        }

        return stocks;
    }

    public static List<String> getAllGrowsStock(){
        List<String> stocks = new ArrayList<>(600);

        int code = 300001;
        final int count = 580;

        for ( int i=0;i<=count;i++ ){
            stocks.add(String.valueOf(code++));
        }

        return stocks;
    }

    private static String padLeft(String curr, int len, char c){
        int currLen = curr.length();

        if ( currLen >= len )
            return curr;

        for (int i=0;i<len-currLen;i++){
            curr = c + curr;
        }

        return curr;
    }

    public static void main(String[] args){
    }
}
