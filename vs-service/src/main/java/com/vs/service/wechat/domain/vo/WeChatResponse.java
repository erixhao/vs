package com.vs.service.wechat.domain.vo;

import com.google.common.collect.Lists;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.TradingBook;
import com.vs.common.domain.PnL;
import com.vs.common.domain.Transaction;
import com.vs.common.domain.enums.BullBear;
import com.vs.common.domain.enums.MarketIndex;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.DateUtils;
import com.vs.common.utils.PropertieUtils;
import lombok.Data;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by erix-mac on 16/7/2.
 */
@Data
public class WeChatResponse {

    private final static String NO_TRADE_MSG = "[交易信号]暂无交易信号";
    private final static String NO_TRAN_MSG = "[交易历史]暂无交易记录";
    private final static String NO_TRADE_PL_MSG = "[交易损益]暂无损益报告";
    private final static int LATEST_TRADE_DAY = -30 * 3;


    public TradeResult toResponse(String code, List<Transaction> trans, List<TradingBook> tradingBooks, BullBear trend, Map<MarketIndex, BullBear> index,
                                  Map<String, TradeResult> random, Map<String, TradeResult> randomProfit) {
        StringBuilder sb = new StringBuilder();

        List<Transaction> latestTrans = extractLatestTradeTransaction(trans, DateUtils.getLastDate(LATEST_TRADE_DAY));


        String logo = generateLogo();
        String version = generateVersion();
        String title = generateTitle(code);
        String summary = generateSummary(code, trend, index);
        String singal = generateTradeSignal(latestTrans);
        String history = generateTradeHistory(trans);
        String pl = generateTradePL(tradingBooks);
        String suggestion = generateSmartSuggestion(random);
        String greatProfit = generateGreatProfit(randomProfit);

        boolean isSignal = isTradeSingnal(latestTrans);
        boolean isBuySignal = isBuySingnal(latestTrans);
        double profit = tradingBooks.size() > 0 ? tradingBooks.get(0).getPnL().getTotalProfitPercentage() : 0;


        sb.append(logo)
                .append(title).append("\n")
                .append(summary).append("\n")
                .append(singal).append("\n")
                .append(trans.size() > 0 ? history + "\n" : "")
                .append(tradingBooks.size() > 0 ? pl + "\n" : "")
                .append(random.size() > 0 ? suggestion + "\n" : "")
                .append(randomProfit.size() > 0 ? greatProfit + "\n" : "")
                .append(version);

        return new TradeResult(isSignal, isBuySignal, new Date(), sb.toString(), profit);
    }


    public static String generateInvalidInput() {
        return generateMenu();
    }

    public static String generateMenu() {
        return new StringBuilder()
                .append(generateLogo())
                .append("欢迎使用【VENUS】智能投顾\n")
                .append("------------------------ \n")
                .append("1. 查询个股请输入6位代码\n")
                .append("2. 查询指数(SH,SZ,GR)\n")
                .append("3. 查询大盘趋势(TD:SH)\n")
                .append("4. 查询个股趋势(TD:代码)\n")
                .append("5. 个股回归测试(敬请期待)\n")
                .append("6. 智能自动选股(敬请期待)\n")
                //.append("------------------------\n")
                //.append("7. 查询个股实时输入L:6位代码\n")
                //.append("(实时计算较慢,无返回需等1分钟重新查询）\n")
                .append("------------------------\n")
                .toString();
    }

    private static String generateLogo1() {
        return new StringBuilder()
                .append("------------------------ \n" +
                        "\\      / __         ___ \n" +
                        " \\    / |_ |\\ ||  ||__ \n" +
                        "  \\  /  |__| \\||__|___|\n" +
                        "   --                    \n" +
                        "------------------------ \n")
                .toString();
    }

    private static String generateVersion() {
        return new StringBuilder()
                .append("风险提示：")
                .append("\n智能投顾建议，风险自担!")
                .append("\n[VENUS]:" + PropertieUtils.getSystemVersion())
                .append("\n-------------------------")
                .toString();
    }

    private static String generateLogo() {
        return new StringBuilder()
                //.append("-------------------------- \n" +
                //"*      *     \n" +
                //" *    *           \n" +
                //"  *  *         \n" +
                //"   ** " +
                .append("【VENUS】 " + PropertieUtils.getSystemVersion() + " \n")
                .append("------------------------ \n")
                .toString();
    }

    private static String generateTitle(String code) {
        return new StringBuilder("证券[" + code + "]")
                .append("策略时间:" + DateUtils.toMarketDate(new Date()) + ",")
                .append("交易建议如下:")
                .append("\n------------------------")
                .toString();
    }

    private static String generateSummary(String code, BullBear trend, Map<MarketIndex, BullBear> index) {
        String SUMMARY = "根据Venus分析，目前大盘(上证)处于%2s,深圳指数处于%2s,创业指数处于%2s,个股[%6s]处于%2s,建议操作较为%2s, 请参考如下提示：";

        String action = (MarketIndex.isIndexBull(index) || trend.isBull()) ? "乐观" : "谨慎";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(SUMMARY, index.get(MarketIndex.ShanghaiCompositeIndex).getChiness(),
                index.get(MarketIndex.ShenzhenComponentIndex).getChiness(),
                index.get(MarketIndex.GrowthEnterpriseIndex).getChiness(),
                code, trend.getChiness(), action));

        return sb.toString();
    }

    private static List<Transaction> extractLatestTradeTransaction(List<Transaction> trans, Date latestDay) {
        List<Transaction> latestTrans = Lists.newArrayList();
        for (Transaction t : trans) {
            if (t.getDate().after(latestDay)) {
                latestTrans.add(t);
            }
        }

        return latestTrans;
    }

    private static String generateTradeSignal(final List<Transaction> latestTrans) {
        StringBuilder sb = new StringBuilder();

        if (latestTrans.size() == 0) {
            return sb.append("------------------------\n")
                    .append(NO_TRADE_MSG)
                    .append("\n------------------------")
                    .toString();

        }

        String code = latestTrans.get(0).getStock().getCode();
        String DETAIL_STR = "%2s|%2s|%2s|";
        sb.append("[Venus]" + code + "交易信号");
        sb.append("\n-------------------------");
        sb.append(String.format(DETAIL_STR, "\n日期", "操作", "价格"));
        sb.append("\n-------------------------");
        DecimalFormat format = new DecimalFormat("0.00");
        for (Transaction t : latestTrans) {
            sb.append("\n");
            sb.append(String.format(DETAIL_STR, HistoricalData.toMarketShortDate(t.getDate()), t.getDirection().getDesc(), format.format(t.getPrice())));
        }
        sb.append("\n-------------------------");

        return sb.toString();
    }

    private static String generateTradeHistory(List<Transaction> trans) {
        if (trans == null || trans.size() == 0)
            return NO_TRAN_MSG;

        String code = trans.get(0).getStock().getCode();

        StringBuilder sb = new StringBuilder();
        String LINE = "-------------------------";
        String DETAIL_STR = "%5s|%2s|%3s|%3s|%3s|";
        sb.append("[Venus]" + code + "交易记录");
        sb.append("\n").append(LINE);
        sb.append(String.format(DETAIL_STR, "\n日期", "操作", "价格", "利润%", "总%"));
        sb.append("\n").append(LINE);
        DecimalFormat format1 = new DecimalFormat("0.0");
        DecimalFormat format = new DecimalFormat("0");
        for (Transaction t : trans) {
            if (t.getPositions() <= 0)
                continue;
            sb.append("\n");
            sb.append(String.format(DETAIL_STR, HistoricalData.toMarketShortDate(t.getDate()), t.getDirection().getDesc(), format1.format(t.getPrice()), format.format(t.getCurrentTadeProfit().getCurrProfitPercentage()) + "%", format.format(t.getCurrentTadeProfit().getTotalProfitPercentage()) + "%"));
        }
        sb.append("\n").append(LINE);

        return sb.toString();
    }

    private static String generateTradePL(List<TradingBook> tradingBooks) {
        StringBuilder sb = new StringBuilder();

        String LINE = "------------------------";
        Collections.sort(tradingBooks, Collections.<TradingBook>reverseOrder());
        if (tradingBooks == null || tradingBooks.size() == 0) {
            sb.append(LINE).append("\n")
                    .append(NO_TRADE_PL_MSG)
                    .append("\n").append(LINE);
            return sb.toString();
        }

        String code = tradingBooks.get(0).getStock().getCode();

        DecimalFormat format = new DecimalFormat("0");
        String DETAIL_STR = "%4s|%4s|%2s|%2s|";
        sb.append("[Venus]" + code + "持仓报告");
        sb.append("\n").append(LINE);
        sb.append("\n").append(String.format(DETAIL_STR, "利润%", "总利润%", "交易", "持仓"));
        sb.append("\n").append(LINE);
        for (TradingBook t : tradingBooks) {
            if (t.isAllShortTransactions())
                continue;

            PnL p = t.getPnL();
            sb.append("\n");
            sb.append(String.format(DETAIL_STR, format.format(p.getCurrProfitPercentage()) + "%", format.format(p.getTotalProfitPercentage()) + "%", t.getTransactions().size(), t.getPositions()));
        }
        sb.append("\n").append(LINE);
        return sb.toString();
    }


    public static String generateSmartSuggestion(Map<String, TradeResult> random) {
        StringBuilder sb = new StringBuilder();

        if (random.size() == 0)
            return sb.toString();

        sb.append("【智能优选】:");
        for (String k : random.keySet()) {
            sb.append(k).append(",");
        }

        String suggestion = sb.toString();
        return suggestion.substring(0, suggestion.length() - 1);
    }

    private String generateGreatProfit(Map<String, TradeResult> profit) {

        StringBuilder sb = new StringBuilder();

        if (profit.size() == 0)
            return sb.toString();

        DecimalFormat format = new DecimalFormat("0");
        sb.append("【一年业绩】:" + TimeWindow.getLastYear(TimePeriod.DAILY).toWeChatString());
        for (String k : profit.keySet()) {
            sb.append("\n").append(k).append(":").append(format.format(profit.get(k).getProfit())).append("%");
        }

        return sb.toString();
    }


    private static boolean isBuySingnal(List<Transaction> trans) {
        if (trans == null || trans.size() == 0 || trans.get(0) == null)
            return false;

        return trans.get(0).getDirection().equals(TradeDirection.BUY);
    }

    private static boolean isTradeSingnal(List<Transaction> trans) {
        if (trans == null || trans.size() == 0)
            return false;

        for (Transaction t : trans) {
            if (t.getDirection().equals(TradeDirection.BUY))
                return true;
        }

        return false;
    }

}
