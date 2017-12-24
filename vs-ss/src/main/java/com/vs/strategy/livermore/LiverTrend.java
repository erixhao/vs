package com.vs.strategy.livermore;

/**
 * Created by erix-mac on 2017/9/9.
 */
public enum LiverTrend {

    NA(0),UPWARD(1),NATURAL_RALLY(2),SECONDARY_RALLY(3),DOWNWARD(-1),NATURAL_REACTION(-2),SECONDARY_REACTION(-3);

    private int value;

    private LiverTrend(int value){
        this.value = value;
    }

    public boolean isUp(){
        return value > 0;
    }

    public boolean isDown(){
        return value < 0;
    }
}
