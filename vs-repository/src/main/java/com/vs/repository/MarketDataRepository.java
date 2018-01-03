package com.vs.repository;

import com.google.common.collect.Iterables;
import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.Stock;
import com.vs.dao.utility.DataAccessService;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MarketDataRepository {
    public static List<HistoricalData> getAllMarketDataByYear(String year) {
        Predicate<HistoricalData> criteria = d -> d.getDate().getYear() == Integer.parseInt(year);
        return DataAccessService.findAllBy(HistoricalData.class, criteria);
    }

    public static List<HistoricalData> getAllMarketDataBy(String code, String year) {
        Predicate<HistoricalData> criteria = d -> d.getStockCode().equalsIgnoreCase(code);
        criteria = criteria.and(d -> d.getDate().getYear() == Integer.parseInt(year));
        return DataAccessService.findAllBy(HistoricalData.class, criteria);
    }

    public static List<HistoricalData> getAllMarketDataBy(List<String> codes, String year) {
        Predicate<HistoricalData> criteria = d -> codes.contains(d.getStockCode());
        criteria = criteria.and(d -> d.getDate().getYear() == Integer.parseInt(year));
        return DataAccessService.findAllBy(HistoricalData.class, criteria);
    }

    public static HistoricalData getMarketDataBy(String code, LocalDate date) {
        Predicate<HistoricalData> criteria = d -> d.getStockCode().equalsIgnoreCase(code);
        criteria = criteria.and(d -> d.getDate().equals(date));
        return Iterables.getFirst(DataAccessService.findAllBy(HistoricalData.class, criteria), null);
    }

    public static List<HistoricalData> getAllMarketDataBy(String code, LocalDate startDate, LocalDate endDate) {
        Predicate<HistoricalData> criteria = d -> d.getStockCode().equalsIgnoreCase(code);
        criteria = criteria.and(d -> startDate.isBefore(d.getDate()));
        criteria = criteria.and(d -> endDate.isAfter(d.getDate()));
        return DataAccessService.findAllBy(HistoricalData.class, criteria);
    }

    public static List<HistoricalData> filterBy(List<HistoricalData> datas, LocalDate startDate, LocalDate endDate) {
        Predicate<HistoricalData> criteria = d -> startDate.isBefore(d.getDate());
        criteria = criteria.and(d -> endDate.isAfter(d.getDate()));
        return datas.stream().filter(criteria).collect(Collectors.toList());
    }

    public static List<HistoricalData> getAllMarketDataBy(String code) {
        Predicate<HistoricalData> criteria = d -> d.getStockCode().equalsIgnoreCase(code);
        return DataAccessService.findAllBy(HistoricalData.class, criteria);
    }

    public static HistoricalData getLatestMarketData(String code) {
        Predicate<HistoricalData> criteria = d -> d.getStockCode().equalsIgnoreCase(code);
        List<HistoricalData> marketDataList = DataAccessService.findAllBy(HistoricalData.class, criteria).stream().sorted().collect(Collectors.toList());
        return Iterables.getFirst(marketDataList, null);
    }

    public static void insert(final List<HistoricalData> datas) {
        DataAccessService.save(HistoricalData.class, datas, true);
    }

    public static int getMarketCount(String code) {
        Predicate<HistoricalData> criteria = d -> d.getStockCode().equalsIgnoreCase(code);
        return DataAccessService.findAllBy(HistoricalData.class, criteria).size();
    }

    public static int getMarketCount(String code, LocalDate date) {
        Predicate<HistoricalData> criteria = d -> d.getStockCode().equalsIgnoreCase(code);
        criteria = criteria.and(d -> d.getDate().equals(date));
        return DataAccessService.findAllBy(HistoricalData.class, criteria).size();
    }

    public static List<String> getAllExistingCodes() {
        return DataAccessService.findAll(Stock.class).stream().map(d -> d.getCode()).collect(Collectors.toList());
    }
}
