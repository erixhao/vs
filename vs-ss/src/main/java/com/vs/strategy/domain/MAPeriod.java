package com.vs.strategy.domain;

/**
 * Created by erix-mac on 2017/8/12.
 */
public enum MAPeriod {
    MA5(5),MA10(10),MA20(20),MA24(24),MA30(30),MA60(60),MA67(67),MA89(89),MA120(120);

    private int period;

    private MAPeriod(int days){
        this.period = days;
    }

    public int getPeriod(){
        return period;
    }

}
