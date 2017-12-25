package com.vs.common.domain;


import com.vs.common.domain.enums.Strategies;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.utils.Constants;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by erix-mac on 15/8/14.
 */

public class Transaction implements Comparable<Transaction> {
	@Getter
	private Stock stock;
	@Getter
	@Setter
	private Date date;
	@Getter
	@Setter
	private TradeDirection direction;
	@Getter
	@Setter
	private double price;
	@Getter
	@Setter
	private long positions;
	@Getter
	@Setter
	private long netPositions;
	@Setter
	private boolean closed = false;
	@Getter
	@Setter
	private Strategies strategy;
	@Getter
	@Setter
	private boolean isDividendsSplit;
	@Getter
	@Setter
	private TradeAction openAction;
	@Getter
	@Setter
	private TradeAction closeAction;

	@Getter
	@Setter
	private PnL currentTradePnL;
	
	

	public Transaction() {
	}

	public Transaction(Date date, TradeDirection direction, Stock stock, double price, long positions) {
		this(date, direction, stock, Strategies.NONE, price, positions);
	}

	public Transaction(Date date, TradeDirection direction, Stock stock, Strategies strategy, double price,
					   long positions) {
		this.date = date;
		this.direction = direction;
		this.stock = stock;
		this.price = price;
		this.positions = positions;
		this.strategy = strategy;
	}
	
	public String getTransactionDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(this.date);
	}

	public boolean isClosed() {
		if (this.getDirection().equals(TradeDirection.SELL))
			return true;
		return this.closed;
	}

	// Transacton Amount
	public double getAmount() {
		return price * positions;
	}

	public double getNetAmount() {
		double netAmount = 0;
		if(TradeDirection.SELL.equals(direction)){
			netAmount = this.getAmount() - this.getCommission() - this.getStampTax();
		}

		if(TradeDirection.BUY.equals(direction)){
			netAmount = (this.getAmount() + this.getCommission() + this.getStampTax()) * -1;
		}

		return netAmount;
	}
	
	public long getNetPosition(){
		if(TradeDirection.SELL.equals(direction)){
			return -1 * this.getPositions();
		}
		
		if(TradeDirection.BUY.equals(direction)){
			return this.getPositions();
		}
		
		return 0;
	}

	// Transaction Commission
	public double getCommission() {
		return this.getAmount() * Constants.getTradeCommission();
	}

	// Transaction Stamp Tax
	public double getStampTax() {
		if (this.direction == TradeDirection.SELL) {
			return this.getAmount() * Constants.getTradeStampTax();
		}
		return 0;
	}

	// Current stock value compare with the transacton occurs value
	public double getProfit(double marketPrice) {
		if (direction.equals(TradeDirection.SELL))
			return 0;

		return getDelta(marketPrice) * this.positions;
	}

	public double getProfitPercentage(double marketPrice) {
		if (price == 0 || positions <= 0)
			return 0;

		return getDelta(marketPrice) * 100 / price;
	}

	private double getDelta(double marketPrice) {
		return marketPrice == 0 ? 0 : (marketPrice - this.price) * direction.getValue();
	}

	@Override
	public int compareTo(Transaction o) {

		if (this.getDate().compareTo(o.getDate()) == 0)
			return 0;

		return this.getDate().before(o.getDate()) ? 1 : -1;
	}

	@Override
	public String toString() {
		return "Transaction{" +
				"date=" + HistoricalData.toMarketDate(date) +
				", direction=" + (direction.equals(TradeDirection.BUY) ? direction + " " : direction) +
				", stock=" + stock +
				", price=" + price +
				", positions=" + positions +
				", netPositions=" + netPositions +
				", profit=" + this.getProfit(this.getPrice()) +
				", closed=" + closed +
				", isDividendsSplit=" + isDividendsSplit +
				", Strategies=" + strategy +
				", CloseAction = " + (closeAction == null ? "NULL" : closeAction.toString()) +
				'}';
	}

	public String toTradeReportString() {
		return "Transaction{" +
				"date=" + HistoricalData.toMarketDate(date) +
				", direction=" + (direction.equals(TradeDirection.BUY) ? direction + " " : direction) +
				", stock=" + stock +
				", price=" + price +
				", positions=" + positions +
				'}';
	}

	public String toWeChatString(){
		return "Trading Strategy:{" +
				"Stock:" + stock.getCode() +
				",Trading Date:" + HistoricalData.toMarketDate(date) +
				",Direction:" + direction +
				//",交易价格= 用户可根据当日开盘情况, 自行选择买入最佳时机。" +
				", Position:" + positions +
				'}';
	}
}
