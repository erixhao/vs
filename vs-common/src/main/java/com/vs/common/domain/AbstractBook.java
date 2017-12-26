package com.vs.common.domain;

import com.google.common.collect.Lists;
import com.vs.common.domain.enums.TradeDirection;
import com.vs.common.domain.vo.MarkToMarket;
import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * Created by erix-mac on 2017/12/24.
 */
@Data
public abstract class AbstractBook {
    protected Stock stock;
    protected MarkToMarket markToMarket;
    @Getter
    protected List<Transaction> transactions = Lists.newArrayList();

    public long getPositions() {
        long positions = 0;
        for (Transaction trans : transactions) {
            if (trans.getDirection() == TradeDirection.BUY) {
                positions += trans.getPositions();
            } else if (trans.getDirection() == TradeDirection.SELL) {
                positions -= trans.getPositions();
            }
        }

        return positions;
    }


    protected double getMarketValue(double mktPrice) {
        return this.getPositions() * mktPrice;
    }

    public List<Transaction> getTradedTransactions(){
        List<Transaction> trans = Lists.newArrayList();

        for ( Transaction t : this.transactions ){
            if ( t.getPositions() > 0 )
                trans.add(t);
        }

        return trans;
    }

    public boolean hasTransaction() {
        return this.transactions != null && this.transactions.size() > 0;
    }


    public boolean isAllShortTransactions(){
        for ( Transaction t : this.transactions ){
            if ( !t.getDirection().equals(TradeDirection.SHORT) )
                return false;
        }

        return true;
    }

    protected void nettingPositionsUpdate(Transaction tran) {
        List<Transaction> trans = this.getTransactions();
        long positions = tran.getPositions();
        for (int i = trans.size() - 1; i >= 0; i--) {
            if (positions <= 0) {
                tran.setClosed(true);
                break;
            }

            Transaction t = trans.get(i);
            if (!t.getDirection().equals(TradeDirection.BUY) || t.isClosed() || t.getNetPositions() == 0)
                continue;

            long delta = t.getNetPositions() - positions;
            positions -= t.getNetPositions();

            t.setNetPositions(delta > 0 ? delta : 0);
            t.setClosed(delta <= 0);

            if (delta <= 0) {
                //log.info("####### Close Transaction" + tran.toString());
            }
        }
    }

    protected double calculateCapital(Transaction tran) {
        double ajdCapital = this.markToMarket.getAvailCapital();

        if (tran.getDirection().equals(TradeDirection.BUY)) {
            ajdCapital -= tran.getAmount();
        } else if (tran.getDirection().equals(TradeDirection.SELL)) {
            ajdCapital += tran.getAmount();
        }

        return ajdCapital;
    }

    protected PnL calculatePnL(double marketPrice) {
        double buy = 0, sell = 0, commission = 0, stampTax = 0;

        for (Transaction trans : this.transactions) {
            if (trans.getDirection() == TradeDirection.BUY) {
                buy += (trans.getAmount());
            } else if (trans.getDirection() == TradeDirection.SELL) {
                sell += (trans.getAmount());
            }

            commission += trans.getCommission();
            stampTax += trans.getStampTax();
        }

        double profit = (this.getMarketValue(marketPrice) + sell) - buy;
        double netProfit = profit - (commission + stampTax);
        double totalPercentage = (this.markToMarket.getTotalCapital() == 0 ? 0 : netProfit / this.markToMarket.getTotalCapital()) * 100;
        double currPercentage = ( buy > 0 && buy < this.markToMarket.getTotalCapital() ) ?  (netProfit / buy) * 100 : totalPercentage;

        return new PnL(buy, sell, profit, commission, stampTax, netProfit, currPercentage, totalPercentage, this.markToMarket.getMarketPrice(), this.getPositions(), this.getMarketValue(markToMarket.getMarketPrice()), this.markToMarket.getAvailCapital());
    }


    protected void book(Transaction t, boolean nettingHandled) {
        this.transactions.add(t);
        this.markToMarket.setAvailCapital(this.calculateCapital(t));
        t.setCurrentTradePnL(this.calculatePnL(t.getPrice()));


        if (!nettingHandled && t.getDirection().equals(TradeDirection.SELL)) {
            this.nettingPositionsUpdate(t);

            if ( this.getPositions() <= 0 && this.markToMarket.getHighestProfit() > 0 ){
                this.markToMarket.setHighestProfit(0);
            }
        }
    }

    protected boolean validate(Transaction trans) {
        boolean result = true;
        switch (trans.getDirection()) {
            case BUY:
                double adjCapital = this.calculateCapital(trans);
                if (adjCapital < 0) {
                    //log.info("REJECT! Long positions > current totalCapital! current capital: " + this.getCapital() + " long: " + trans.getAmount());
                    result = false;
                }
                break;
            case SELL:
                if (trans.getPositions() > this.getPositions()) {
                    //log.info("REJECT! Short positions > current holding positions! current: " + this.getPositions() + " short: " + trans.getPositions());
                    result = false;
                }
                break;
            case SHORT:
                result = true;
                break;
            default:
                result = true;
                break;
        }

        return result;
    }

    protected boolean isInPosition(long position){
        return position >= this.getPositions();
    }
}
