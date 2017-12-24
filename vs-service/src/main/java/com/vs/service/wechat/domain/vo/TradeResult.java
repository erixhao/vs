package com.vs.service.wechat.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * Created by erix-mac on 2017/1/14.
 */
@Data
@AllArgsConstructor
public class TradeResult {

    boolean isTradeSingal;
    boolean isBuySingal;
    private Date tradeDate;
    private String response;

    private double profit;

}
