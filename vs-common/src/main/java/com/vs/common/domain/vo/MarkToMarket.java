package com.vs.common.domain.vo;

import com.vs.common.domain.Stock;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Date;

/**
 * Created by erix-mac on 2017/12/24.
 */
@Data
public class MarkToMarket {
    @Getter
    private Stock stock;

    private LocalDate marketDate;
    private double marketPrice;
    private final double totalCapital;
    private double availCapital;
    private double highestProfit;
    private Position pyramidPosition = new PyramidPosition();

    public MarkToMarket(Stock s, double marketPrice, double totalCapital){
        this.stock = s;
        this.marketPrice = marketPrice;
        this.totalCapital = totalCapital;
        this.availCapital = totalCapital;
    }
}
