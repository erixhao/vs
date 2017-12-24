package com.vs.strategy.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by erix-mac on 15/9/1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Weight implements Comparable<Weight>{

    private long leftWeight;
    private long rightWeight;
    private float ratio = 1;

    public float totalWeight(){
        return (this.leftWeight * this.rightWeight) * this.ratio;
    }


    @Override
    public String toString() {
        return "Weight:" + this.totalWeight() + " {" +
                leftWeight + "," + rightWeight +
                "} ratio: " + ratio;
    }

    @Override
    public int compareTo(Weight o) {
        if ( this.totalWeight() - o.totalWeight() > 0 )
            return 1;
        else if ( this.totalWeight() - o.totalWeight() == 0 )
            return 0;
        else return -1;
    }
}
