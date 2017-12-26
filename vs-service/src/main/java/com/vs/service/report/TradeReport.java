package com.vs.service.report;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vs.common.domain.*;
import com.vs.common.domain.vo.TimeWindow;
import com.vs.common.utils.BeanContext;
import com.vs.common.utils.DataFormatUtil;
import com.vs.strategy.analysis.MarketMovementAnalyze;
import com.vs.strategy.domain.MarketBase;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
public class TradeReport {
	private static String SPLIT = "--------------------------------------------------";
	private static String HEADER_STR = "| ??????? : %10s     ??????? : %10s| ";
	private static String DETAIL_STR = "|%10s|%6s|%10s|%12s|%13s|%8s|%8s|%12s|%10s|%12s|%12s|";

	private static MarketMovementAnalyze nonStrategyProfitAnalyze = BeanContext.getBean(MarketMovementAnalyze.class);


	//test
	public static void printTradeDetail(List<TradingBook> tradingBooks) {
		HashMap<Stock, List<TradingBook>> stockTrades = TradeReport.groupByStock(tradingBooks);
		
		for(Entry e : stockTrades.entrySet()){
			Stock s = (Stock) e.getKey();
			List<TradingBook> t = (List<TradingBook>) e.getValue();
			printSplit(44);
			log.info(String.format(HEADER_STR, s.getCode(), s.getName()));
			printSplit(44);
			
			printSplit(125);
			log.info(String.format(DETAIL_STR, "TradeDate","Action","Price ","Position ","Amount ","Comm", "Tax","Captial ","Stocks","Stock Price", "profile"));
			
			for(TradingBook tradingBook : t){
				tradingBook.getMarkToMarket().setAvailCapital(tradingBook.getMarkToMarket().getTotalCapital());
				for(Transaction transaction : tradingBook.getTransactions()){
					//trade.makeTransaction(transaction);
					PnL p = transaction.getCurrentTradePnL();
					log.info(String.format(DETAIL_STR, 
							transaction.getTransactionDate(),
							transaction.getDirection(),
							DataFormatUtil.formatDouble(transaction.getPrice()),
							transaction.getPositions(),
							DataFormatUtil.formatDouble(transaction.getNetAmount()),
							DataFormatUtil.formatDouble(transaction.getCommission()),
							DataFormatUtil.formatDouble(transaction.getStampTax()),
							DataFormatUtil.formatDouble(tradingBook.getMarkToMarket().getAvailCapital()),
							p.getHoldingPositions(),
							DataFormatUtil.formatDouble(p.getMarketValue()),
							DataFormatUtil.formatPercentage(p.getCurrProfitPercentage()) + "%"
							));
				}
				printSplit(125);
			}
			
		}

	}


	public static void printTradeResult(TimeWindow window, List<TradingBook> tradingBooks) {
		Collections.sort(tradingBooks, Collections.<TradingBook>reverseOrder());
		System.out.println("\n >>>>>>>>>>>>>> >>>>>>>>>>>>>>   Trade " + window.toString() + ">>>>>>>>>>>>>> >>>>>>>>>>>>>>: ");
		if (tradingBooks == null || tradingBooks.size() == 0) {
			System.out.println(" >>>>>>>>>>>>>> NO Trade Action ! >>>>>>>>>>>>>>: ");
			return;
		}
		DecimalFormat f = new DecimalFormat("0.#");
		String DETAIL_STR = "|%7s|%8s|%10s|%7s|%5s|%6s|%10s|%10s|%8s}";
		System.out.println("---------------------------------------------------------------");
		System.out.println(String.format(DETAIL_STR, "Stock","Profit%","Profit ","Price ","Pos ","Trans","NonStr Date","N-Prices", "NonStr Profit%"));
		//System.out.println(String.format(DETAIL_STR, "??","??%","?? ","?? ","?? ","????"));
		System.out.println("---------------------------------------------------------------");
		Date today = Calendar.getInstance().getTime();

		for (TradingBook t : tradingBooks) {
			PnL p = t.getPnL();
			MarketBase np = nonStrategyProfitAnalyze.calcuate(t.getStock().getCode(),window);

			System.out.println(String.format(DETAIL_STR, t.getStock().getCode(), f.format(p.getTotalProfitPercentage()) + "%", f.format(p.getProfit()) , t.getMarkToMarket().getMarketPrice(), t.getPositions(),
					t.getTransactions().size(), HistoricalData.toMarketDate(np.getBeginDate()) + " - " + HistoricalData.toMarketDate(np.getEndDate()),
					f.format(np.getBeginPrice()) + "-" + f.format(np.getEndPrice()), f.format(np.getProfitPercentage()) + "%"));
		}
		System.out.println("---------------------------------------------------------------");
	}
	
	private static HashMap<Stock, List<TradingBook>> groupByStock(List<TradingBook> tradingBooks){
		HashMap<Stock, List<TradingBook>> tradeMap = Maps.newHashMap();
		
		for(TradingBook t : tradingBooks){
			if(tradeMap.get(t.getStock()) == null){
				List<TradingBook> stockTradingBook = Lists.newArrayList(t);
				tradeMap.put(t.getStock(), stockTradingBook);
			}else{
				tradeMap.get(t.getStock()).add(t);
			}
		}
		return tradeMap;
	}
	
	private static void printSplit(int size){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<size;i++){
			sb.append("-");
		}
		log.info(sb.toString());
	}

	public static List<Transaction> filterTransactions(List<TradingBook> tradingBooks, Date reportWindow){
		List<Transaction> filterTrans = Lists.newArrayList();
		System.out.println("\n\nTRADE REPORT  BEGIN");
		System.out.println("------------------------");
		for (TradingBook tradingBook : tradingBooks) {
			List<Transaction> trans = tradingBook.getTransactions();
			for (Transaction tran : trans) {
				if (tran.getDate().after(reportWindow)) {
					System.out.println("Report: " + tran.toString());
					filterTrans.add(tran);
				}
			}
		}
		System.out.println("TRADE REPORT  END\n\n");
		System.out.println("------------------------");



		return filterTrans;
	}
}
