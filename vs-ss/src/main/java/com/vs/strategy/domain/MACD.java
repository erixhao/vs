package com.vs.strategy.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by erix-mac on 2017/2/16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MACD {

    private double DIF;
    private double DEA;


    public double getMACD(){
        return (DIF - DEA) * 2;
    }

    @Override
    public String toString(){
        return "MACD" + "(MACD=" + this.getMACD() + ",DIF=" + getDIF() + ",DEA=" + getDEA();
    }
}
