package com.vs.common.domain.enums;


import com.vs.common.utils.DateUtils;

import java.util.Date;

/**
 * Created by erix-mac on 15/11/7.
 */
public enum BullBear {
    BULL("牛市"), BEAR("熊市"), VOLATILE("波动市"), BIGBULL("大牛市"), BIGBEAR("大熊市"), NA("未知");

    private String chiness;

    BullBear(String comments){
        chiness = comments;
    }

    public String getChiness(){
        return this.chiness;
    }

    public boolean isBigBull(){ return this.equals(BIGBULL); }
    public boolean isBigBear(){ return this.equals(BIGBEAR); }

    public boolean isBull(){ return this.equals(BULL) || this.equals(BIGBULL); }
    public boolean isBear(){ return this.equals(BEAR) || this.equals(BIGBEAR); }

    public boolean isVolatile(){ return this.equals(VOLATILE); }

    public void printMarketTrend(BullBear t, Date date, String code){
        String DETAIL_STR = "|%10s|%7s|%4s|";
        System.out.println(String.format(DETAIL_STR, DateUtils.toMarketDate(date),code, t.toString()));

    }

    public static void main(String[] args){

    }
}
