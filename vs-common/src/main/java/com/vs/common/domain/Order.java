package com.vs.common.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vs.common.domain.enums.TradeStrategy;
import com.vs.common.domain.enums.TradeDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by erix-mac on 15/9/8.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Comparable<Order> {

    private TradeStrategy strategy;
    private TradeDirection tradeDirection;
    private Stock stock;
    private Date analysisDate;
    private Date tradeDate;
    private double tradePrice;
    private boolean nettingHandled = false;

    public Order(TradeStrategy strategy, TradeDirection direction, Stock stock, Date analysisDate, Date tradeDate) {
        this(strategy,direction,stock,analysisDate,tradeDate,-1);
    }


    public Order(TradeStrategy strategy, TradeDirection direction, Stock stock, Date analysisDate, Date tradeDate, double tradePrice) {
        this.strategy = strategy;
        this.tradeDirection = direction;
        this.stock = stock;
        this.analysisDate = analysisDate;
        this.tradeDate = tradeDate;
        this.tradePrice = tradePrice;
        this.nettingHandled = false;
    }

    @Override
    public int compareTo(Order o) {
        return this.tradeDate.compareTo(o.getTradeDate());
    }


    public static List<Order> merge(final List<Order> actions){
        Map<Date, Order> actionMap = Maps.newConcurrentMap();
        Collections.sort(actions);

        for ( Order a : actions ){
            if ( actionMap.containsKey(a.getTradeDate()) ){
                Order curr = actionMap.get(a.getTradeDate());

                if ( a.getTradeDirection().isHigherPriorityThan(curr.getTradeDirection()) ){
                    actionMap.put(a.getTradeDate(), a);
                }
            }else{
                actionMap.put(a.getTradeDate(),a);
            }
        }

        return Lists.newArrayList(actionMap.values());
    }

    public static List<Order> merge(final List<Order> actions, boolean andOption){

        List<Order> forMergeActions = andOption ? and(actions) : actions;
        return merge(forMergeActions);
    }

    private static List<Order> and(List<Order> actions){

        if ( actions == null || actions.size() == 0 )
            return Lists.newArrayList();

        for ( Order a : actions ){
            // if STOP loss, execute STOP loss order directly
            if ( a.getStrategy().name().equalsIgnoreCase(TradeStrategy.StopLossStrategy.toString()) && a.getTradeDirection().equals(TradeDirection.SELL)){
                return Lists.newArrayList(a);
            }
        }

        if ( actions.size() < 2 ){
            System.out.println(">>>>> actions.size() >= 2: " + actions.toString());
            return Lists.newArrayList();
        }

        TradeDirection dir0 = actions.get(0).getTradeDirection();
        for ( Order a : actions ){
            // and failed, not all buy
            if ( dir0.isBuy() && ! a.getTradeDirection().isBuy() ){
                return Lists.newArrayList();
            }else if ( dir0.isSell() && ! a.getTradeDirection().isSell() ){
                return Lists.newArrayList();
            }
        }

        System.out.println(">>>>> All SAME Action: " + actions.toString());

        return actions;
    }


}
