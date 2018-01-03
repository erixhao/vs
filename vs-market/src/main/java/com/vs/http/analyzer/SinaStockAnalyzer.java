package com.vs.http.analyzer;

import com.google.common.collect.Lists;
import com.vs.common.domain.Stock;
import com.vs.http.URLReader;

import java.io.IOException;
import java.util.List;

public class SinaStockAnalyzer {
    private static String SINA_ACTIVE_STOCK_URL = "http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeDataSimple?"
            + "page=%s&num=80&sort=symbol&asc=1&node=zhishu_000001&_s_r_a=init";
    // page range from 1 to 17

    private SinaStockAnalyzer() {
    }

    public static List<Stock> getData() {
        List<Stock> stocks = Lists.newArrayList();
        // TODO 17 here
        for (int i = 1; i <= 17; i++) {
            String url = String.format(SINA_ACTIVE_STOCK_URL, i);
            System.out.println(url);
            String content = URLReader.getContext(url, "gb2312");
//			System.out.println(content);
            stocks.addAll(analyzer(content));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return stocks;
    }

    private static List<Stock> analyzer(String content) {
        List<Stock> stocks = convertToEntity(content);
        return stocks;
    }

    private static List<Stock> convertToEntity(String content) {
        List<Stock> stocks = Lists.newArrayList();
        content = content.toString().replaceAll("\\{", "");
        String[] rows = content.split("\\},");

        for (String row : rows) {
            String[] columns = row.split(",");
            String code = columns[0].split(":")[1].replaceAll("\"", "");
            String name = columns[1].split(":")[1].replaceAll("\"", "");
            Stock stock = new Stock();
            stock.setCode(code.substring(2));
            stock.setName(name);

            stocks.add(stock);
        }
        return stocks;
    }
}
