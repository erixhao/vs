package com.vs.common.domain.enums;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * Created by erix-mac on 15/8/3.
 */
public enum MarketIndex {
    ShanghaiCompositeIndex("Shanghai Index","000001", Market.Shanghai,"SH"), ShenzhenComponentIndex("ShenzhenComponentIndex","399001",Market.Shenzhen,"SZ"),
    GrowthEnterpriseIndex("GrowthEnterpriseIndex","399006",Market.Shenzhen,"GR"), ShanghaiShenzhen300Index("ShanghaiShenzhen 300 Index","000300",Market.Shanghai,"HS300"),
    Shanghai50Index("Shanghai50 Index","000016", Market.Shanghai,"SH50");

    private static final List<String> INDEXS = Lists.newArrayList("000001","399001","399006","000016");
    private static final List<String> INDEXS_ABB = Lists.newArrayList("SH","SZ","GR","HS300","SH50");

    private String name;
    private String code;
    private Market market;
    private String abbreviation;

    private MarketIndex(String name, String code, Market market, String abbreviation){
        this.name = name;
        this.code = code;
        this.market = market;
        this.abbreviation = abbreviation;
    }


    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getSinaCode(){
        //000001s,399001s,000300s,
        return code + "s";
    }

    public String getCode(MarketProvider provider){
        return provider.getMarketCode(this.code);
    }

    public static boolean isIndex(String code){
        return INDEXS.contains(code);
    }

    public static boolean isIndexByAbbreviation(String abb){
        return INDEXS_ABB.contains(abb.toUpperCase());
    }

    public static MarketIndex parseByAbbreviation(String abb){
        switch ( abb ){
            case "SH":
                return ShanghaiCompositeIndex;
            case "SZ":
                return ShenzhenComponentIndex;
            case "GR":
                return GrowthEnterpriseIndex;
            case "HS300":
                return ShanghaiShenzhen300Index;
            case "SH50":
                return Shanghai50Index;
            default:
                return null;
        }
    }


    public static boolean isIndexBull(Map<MarketIndex,BullBear> index){
        int idx1 =  index.get(ShanghaiCompositeIndex).isBull() ? 1 : 0;
        int idx2 =  index.get(ShenzhenComponentIndex).isBull() ? 1 : 0;
        int idx3 =  index.get(GrowthEnterpriseIndex).isBull() ? 1 : 0;

        return (idx1 + idx2 + idx3) >= 2;
    }
}
