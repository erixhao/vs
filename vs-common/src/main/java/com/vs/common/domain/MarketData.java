package com.vs.common.domain;

import com.vs.common.domain.annotation.MapInfo;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Data
public abstract class MarketData extends Entity implements Comparable<MarketData>, Serializable {
    @MapInfo(name = "stockCode", position = 0)
    protected String stockCode;
    @MapInfo(name = "date", position = 1)
    protected LocalDate date;
    @MapInfo(name = "yesterdayClose", position = 9)
    protected double yesterdayClose;
    @MapInfo(name = "open", position = 2)
    protected double open;
    @MapInfo(name = "close", position = 4)
    protected double close;
    @MapInfo(name = "high", position = 3)
    protected double high;
    @MapInfo(name = "low", position = 5)
    protected double low;
    @MapInfo(name = "adjClose", position = 8)
    protected double adjClose;
    @MapInfo(name = "volume", position = 6)
    protected long volume;
    @MapInfo(name = "volumeAmount", position = 7)
    protected double volumeAmount;
    //    @MapInfo(name = "updateDate", position = 10)
    protected LocalDate updateDate;

    protected String key = "";

    public double getPercentage() {
        if (this.yesterdayClose == 0)
            return 0;
        else
            return (this.close - this.yesterdayClose) * 100 / this.yesterdayClose;
    }

    public boolean isCloseUp() {
        return close >= open;
    }

    public boolean isCloseDown() {
        return close < open;
    }

    protected void clear() {
        this.open = 0;
        this.close = 0;
        this.high = 0;
        this.low = 0;
        this.volume = 0;
        this.yesterdayClose = 0;
    }

    public String getKey() {
        if (key.equals("")) {
            key = this.stockCode + "_" + this.date;
        }
        return key;
    }

    public int compareTo(@NotNull MarketData o) {
        return this.getKey().compareTo(o.getKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarketData)) return false;
        if (!super.equals(o)) return false;
        MarketData that = (MarketData) o;
        return Objects.equals(getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getKey());
    }
}