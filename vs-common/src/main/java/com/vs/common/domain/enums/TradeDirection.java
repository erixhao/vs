package com.vs.common.domain.enums;

/**
 * Created by erix-mac on 15/8/16.
 */
public enum TradeDirection {
    SHORT(-2,"做空"),SELL(-1,"卖出"), NONE(0,"无操作"), BUY(1,"买入");

    private int value;
    private String desc;

    TradeDirection(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public int getValue(){
        return value;
    }

    public String getDesc(){
        return desc;
    }

    public boolean isTradeable(){
        return value != 0;
    }

    public boolean isBuy(){ return value == 1; }
    public boolean isSell(){ return value == -1 || value == -2; }

    public boolean isHigherPriorityThan(TradeDirection d){

        if ( this.getValue() == d.getValue() ){
            return true;
        }else if ( this.isBuy() && d.isSell() ){
            return false; // Sell with higher priority
        }else if ( this.isSell() && d.isBuy() ){
            return true;  // Sell with higher priority
        }

        return true;
    }



}
