package com.vs.http.analyzer;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.http.URLReader;
import com.vs.http.analyzer.utility.TableUtility;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class SinaHistoryAnalyzer {
    private static String SINA_HOSTORY_URL = "http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/%s.phtml?year=%s&jidu=%s";

    private SinaHistoryAnalyzer() {
    }

    public static List<HistoricalData> getData(String code, LocalDate now) {
        int month = now.getMonthValue();
        int quarter = month / 3 + (month % 3 > 0 ? 1 : 0);
        String url = String.format(SINA_HOSTORY_URL, code, now.getYear(), quarter);
        System.out.println(url);
        String content = URLReader.getContext(url, "gb2312");
        List<HistoricalData> histories = analyzer(content, code);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return histories;
    }

    private static List<HistoricalData> analyzer(String content, String code) {
        int position = content.indexOf("id=\"FundHoldSharesTable\"");

        String tableContent = TableUtility.getTableContent(content, position);

        String data = removeTags(tableContent);

        List<HistoricalData> histories = convertToEntity(data, code);

        return histories;
    }

    private static String removeTags(String tableContent) {
        tableContent = tableContent.replaceAll("<thead>.*?</thead>", "");

        tableContent = tableContent.replaceAll("<table.*?>", "");
        tableContent = tableContent.replaceAll("</table>", "");

        tableContent = tableContent.replaceAll("<tr.*?>", "");
        tableContent = tableContent.replaceAll("</tr>", "#");

        tableContent = tableContent.replaceAll("<td.*?>", "");
        tableContent = tableContent.replaceAll("</td>", ",");

        tableContent = tableContent.replaceAll("<div.*?>", "");
        tableContent = tableContent.replaceAll("</div>", "");

        tableContent = tableContent.replaceAll("<strong.*?>", "");
        tableContent = tableContent.replaceAll("</strong>", "");

        tableContent = tableContent.replaceAll("<a.*?>", "");
        tableContent = tableContent.replaceAll("</a>", "");

        // System.out.println(tableContent);
        return tableContent;
    }

    private static List<HistoricalData> convertToEntity(String content, String code) {
        List<HistoricalData> histories = Lists.newArrayList();
        String[] rows = content.split("#");

        for (int i = 1; i < rows.length - 1; i++) {
            HistoricalData history = new HistoricalData();
            history.from(code + "," + rows[i]);
            histories.add(history);
        }

        return histories;
    }
}
