package com.vs.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by erix-mac on 15/8/19.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PnL {

    private double totalBuy;
    private double totalSell;
    private double profit;
    private double commission;
    private double stampTax;
    private double netProfit;
    private double currProfitPercentage;
    private double totalProfitPercentage;

    private double marketPrice;
    private double holdingPositions;
    private double marketValue;
    private double remainCapital;

}
