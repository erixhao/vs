package com.vs.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by erix-mac on 15/12/25.
 */
public class HtmlUtils {

    public static String removeStringTabReturn(String str){

        if ( str != null && !"".equals(str) ){
            Pattern p = Pattern.compile("\t|\r|\n");
            Matcher m = p.matcher(str);
            String strNoBlank = m.replaceAll("");

            return strNoBlank;
        }

        return str;
    }
}
