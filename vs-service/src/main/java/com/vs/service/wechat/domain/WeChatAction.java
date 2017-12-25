package com.vs.service.wechat.domain;


import com.vs.common.domain.Stock;
import com.vs.common.domain.enums.MarketIndexs;

/**
 * Created by erix-mac on 2017/1/9.
 */
public enum  WeChatAction {
    STOCK, LIVE_STOCK, ABB_INDEX,INDEX,TREND_INDEX,TREND_STOCK,REGRESSION,RECOMMEND, INVALID;

    private String code;

    public String getCode(){
        return code;
    }

    public static WeChatAction parseUserAction(String input){
        WeChatAction action = WeChatAction.INVALID;

        if ( input == null )
            return action;

        action.code = "NA";
        switch ( input.length() ){
            case 1:
                if ( input.equalsIgnoreCase("6") ){
                    action = WeChatAction.RECOMMEND;
                }
            case 2:
                if (MarketIndexs.isIndexByAbbreviation(input) ){
                    action = WeChatAction.ABB_INDEX;
                    action.code = input;
                }
                break;
            case 5:
                if ( input.startsWith("TD:") ) {
                    action = WeChatAction.TREND_INDEX;
                    action.code = input.substring(3);
                }
                break;
            case 6:
                if ( Stock.isValidStockCode(input) ) {
                    action = WeChatAction.STOCK;
                    action.code = input;
                }
                break;
            case 7:
                if ( input.toUpperCase().endsWith("S") && MarketIndexs.isIndex(input.substring(0, input.length()-1) ) ) {
                    action = WeChatAction.INDEX;
                    action.code = input;
                }
                break;
            case 8:
                if ( input.toUpperCase().startsWith("L:") && MarketIndexs.isIndex(input.substring(0, input.length()-1) ) ) {
                    action = WeChatAction.INDEX;
                    action.code = input;
                }
                break;
            case 9:
                if ( input.startsWith("TD:") ){
                    String stock = input.substring(3);
                    if ( Stock.isValidStockCode(stock) ){
                        action = WeChatAction.TREND_STOCK;
                        action.code = stock;
                    }
                }
                break;
            default:
                action = WeChatAction.INVALID;
                action.code = "NA";
                break;
        }

        return action;
    }
}
