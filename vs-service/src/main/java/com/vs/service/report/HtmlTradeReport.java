package com.vs.service.report;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vs.common.domain.Stock;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.PnL;
import com.vs.common.domain.Transaction;
import com.vs.common.utils.DataFormatUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by SJerold on 2016/8/8.
 */
public class HtmlTradeReport {

    private static HashMap<Stock, List<TradingBook>> groupByStock(List<TradingBook> tradingBooks) {
        HashMap<Stock, List<TradingBook>> tradeMap = Maps.newHashMap();

        for (TradingBook t : tradingBooks) {
            if (tradeMap.get(t.getStock()) == null) {
                List<TradingBook> stockTradingBook = Lists.newArrayList(t);
                tradeMap.put(t.getStock(), stockTradingBook);
            } else {
                tradeMap.get(t.getStock()).add(t);
            }
        }
        return tradeMap;
    }


    public static String generateTradeDetailHTMLReport(List<TradingBook> tradingBooks) {
        HashMap<Stock, List<TradingBook>> stockTrades = HtmlTradeReport.groupByStock(tradingBooks);

        StringBuffer sb = new StringBuffer();
        sb.append("<HTML>");
        sb.append("<head>");
        // sb.append("<link href=\"./style.css\" type=\"text/css\" rel=\"stylesheet\"/>");
        applyCSS(sb);
        sb.append("<meta http-equiv=\"Content Type\" content=\"text/html\" charset=\"utf-8\" />");

        sb.append("</head>");

        sb.append("<BODY bgcolor=\"#000000\">");

        for (Map.Entry e : stockTrades.entrySet()) {
            Stock s = (Stock) e.getKey();
            List<TradingBook> t = (List<TradingBook>) e.getValue();

            sb.append("<TABLE>");
            sb.append("<TR class=\"tableHeader\">");
            sb.append("<TD width=100>").append("股票代码").append("</TD>");
            sb.append("<TD width=100>").append(s.getCode()).append("</TD>");
            sb.append("</TR>");
            sb.append("<TR class=\"tableHeader\">");
            sb.append("<TD width=100>").append("股票名称").append("</TD>");
            sb.append("<TD width=100>").append(s.getName()).append("</TD>");
            sb.append("</TR>");
//			sb.append("</TABLE><BR>");

//			sb.append("<TABLE width=100%>");
            sb.append("<TR class=\"tableHeader\">");

            sb.append("<TD class=\"header\" width=100>").append("交易日期").append("</TD>");
//            sb.append("<TD class=\"header\" width=50>").append("操作").append("</TD>");
            sb.append("<TD class=\"header\" width=150>").append("交易").append("</TD>");
//            sb.append("<TD>").append("总交易金额").append("</TD>");
//            sb.append("<TD>").append("手续费").append("</TD>");
//            sb.append("<TD>").append("交易税").append("</TD>");
            sb.append("<TD class=\"header\" width=150>").append("当前状态").append("</TD>");
//            sb.append("<TD class=\"header\" width=100>").append("").append("</TD>");
//            sb.append("<TD>").append("股票价值").append("</TD>");
            sb.append("<TD class=\"header\" width=100>").append("当前收益").append("</TD>");
            sb.append("</TR>");
            for (TradingBook tradingBook : t) {
                tradingBook.getMarkToMarket().setAvailCapital(tradingBook.getMarkToMarket().getTotalCapital());
                int i = 0;
                for (Transaction transaction : tradingBook.getTransactions()) {
                    i++;
                    PnL p = transaction.getCurrentTadeProfit();
                    //trade.makeTransaction(transaction);
                    sb.append("<TR class=\"tableDetail\">");

                    sb.append("<TD>").append(transaction.getTransactionDate()).append("</TD>");
//                    sb.append("<TD>").append(transaction.getDirection().getChineseValue()).append("</TD>");
                    sb.append("<TD>").append(DataFormatUtil.formatDouble(transaction.getPrice()) + " / " + transaction.getDirection().getDesc()+ " " + transaction.getPositions() + " 股").append("</TD>");
//                    sb.append("<TD>").append(transaction.getPositions()).append("</TD>");
//                    sb.append("<TD>").append(DataFormatUtil.formatDouble(transaction.getNetAmount())).append("</TD>");
//                    sb.append("<TD>").append(DataFormatUtil.formatDouble(transaction.getCommission())).append("</TD>");
//                    sb.append("<TD>").append(DataFormatUtil.formatDouble(transaction.getStampTax())).append("</TD>");
                    sb.append("<TD>").append(DataFormatUtil.formatDouble(p.getMarketValue()) + " / 持有 " + p.getHoldingPositions() + " 股").append("</TD>");
//                    sb.append("<TD>").append(trade.getPosition()).append("</TD>");
//                    sb.append("<TD>").append(DataFormatUtil.formatDouble(trade.getHoldStockPrice())).append("</TD>");
                    sb.append("<TD>").append(DataFormatUtil.formatPercentage(p.getCurrProfitPercentage()) + "%").append("</TD>");
                    sb.append("</TR>");
                }
            }
            sb.append("</TABLE><BR>");

        }

        sb.append("</BODY>");
        sb.append("</HTML>");

        return sb.toString();
    }

    private static void applyCSS(StringBuffer sb) {
        sb.append("<style>");
        sb.append("tr.tableHeader{\n" +
                "\tfont:12px 微软雅黑;\n" +
                "\tcolor:#4f6228;\n" +
                "\ttext-align:center;\n" +
                "\tborder-left:1 solid  #000000;\n" +
                "\tborder-right:1 solid  #000000;\n" +
                "\tborder-top:1 solid  #000000;\n" +
                "\tborder-bottom:1 solid #000000;\n" +
                "\tbackground-color:#EAF1DD;\n" +
                "}\n" +
                "tr.tableDetail{\n" +
                "\tfont:12px 微软雅黑;\n" +
                "\tcolor:#4f6228;\n" +
                "\ttext-align:center;\n" +
                "\tborder-left:3 solid  #ffffff;\n" +
                "\tborder-right:3 solid  #ffffff;\n" +
                "\tborder-top:3 solid  #ffffff;\n" +
                "\tborder-bottom:3 solid #ffffff;\n" +
                "}\n" +
                "table.noborder{\n" +
                "\tborder-bottom:0 solid;\n" +
                "\tborder-left:0 solid;\n" +
                "\tborder-right:0 solid;\n" +
                "\tborder-top:0 solid;\n" +
                "\tborder-collapse:collapse;\n" +
                "}\n" +
                "tr{\n" +
                "\tfont:12px 微软雅黑;\n" +
                "\tcolor:#4f6228;\n" +
                "\ttext-align:center;\n" +
                "\tborder-bottom:0 solid;\n" +
                "\tborder-left:0 solid;\n" +
                "\tborder-right:0 solid;\n" +
                "\tborder-top:0 solid;\n" +
                "}\n" +
                "td{\n" +
                "\tfont:12px 微软雅黑;\n" +
                "\tcolor:#FFFFFF;\n" +
                "\ttext-align:center;\n" +
                "\tborder-bottom:0 solid;\n" +
                "\tborder-left:0 solid;\n" +
                "\tborder-right:0 solid;\n" +
                "\tborder-top:0 solid;\n" +
                "\tbackground-color:#002C41;\n" +
                "}\n" +
                "td.header{\n" +
                "\tfont:12px 微软雅黑;\n" +
                "\tcolor:#FFA500;\n" +
                "\ttext-align:center;\n" +
                "\tborder-bottom:0 solid;\n" +
                "\tborder-left:0 solid;\n" +
                "\tborder-right:0 solid;\n" +
                "\tborder-top:0 solid;\n" +
                "\tbackground-color:#002C41;\n" +
                "}\n" +
                "a{\n" +
                "\tfont:13px 微软雅黑;\n" +
                "\tcolor:#4f6228;\n" +
                "}");
        sb.append("</style>");
    }

    public static void generateHTML(List<TradingBook> tradingBooks) {
        String path = HtmlTradeReport.class.getResource("").getPath() + "report_" + System.currentTimeMillis() + ".html";
        System.out.println(path);
        File file = new File(path);
        FileWriter fw = null;
        try {
            file.createNewFile();
            fw = new FileWriter(file);
            fw.write(HtmlTradeReport.generateTradeDetailHTMLReport(tradingBooks));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }
}
