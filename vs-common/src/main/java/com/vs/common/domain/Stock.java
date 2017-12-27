package com.vs.common.domain;

import com.google.common.collect.Lists;
import com.vs.common.domain.annotation.MapInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by erix-mac on 15/8/20.
 */
@Data
@NoArgsConstructor
public class Stock extends Entity {

    private static final List<String> PRE_SHANGHAI_SHENZHENG = Lists.newArrayList("600", "601", "603", "000", "002", "300");
    private static final int STOCK_CODE_MIN_LEN = 6;

    @MapInfo(name = "code", position = 0)
    private String code;
    @MapInfo(name = "name", position = 1)
    private String name;

    public Stock(String code) {
        this.code = code;
        this.name = code;
    }

    public Stock(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public boolean isIndex() {
        return isIndex(code);
    }

    public static boolean isIndex(String code) {
        return code.indexOf("s") > -1 && code.indexOf("s") == code.length() - 1;
    }

    public static boolean isValidStockCode(String code) {
        if (StringUtils.isEmpty(code) && code.length() < STOCK_CODE_MIN_LEN)
            return false;

        if (code.indexOf("s") > -1) {
            return isIndex(code);
        } else {
            return code.matches("[0-9]{6}") && PRE_SHANGHAI_SHENZHENG.contains(code.substring(0, 3));
        }
    }
}