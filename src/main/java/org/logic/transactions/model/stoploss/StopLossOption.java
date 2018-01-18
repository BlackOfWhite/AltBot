package org.logic.transactions.model.stoploss;

import org.logic.transactions.model.OptionImpl;
import org.logic.transactions.model.stoploss.modes.StopLossCondition;
import org.logic.transactions.model.stoploss.modes.StopLossMode;

import java.io.Serializable;

public class StopLossOption implements Serializable, OptionImpl {
    private static final long serialVersionUID = 1L;
    private String marketName;
    private double cancelAt;
    private StopLossCondition condition;
    private StopLossMode mode;
    private boolean sellAll;

    public StopLossOption(String marketName, double cancelAt, StopLossCondition condition, StopLossMode mode, boolean sellAll) {
        this.marketName = marketName;
        this.cancelAt = cancelAt;
        this.condition = condition;
        this.mode = mode;
        this.sellAll = sellAll;
    }

    public StopLossCondition getCondition() {
        return condition;
    }

    public String getMarketName() {
        return marketName;
    }

    public StopLossMode getMode() {
        return mode;
    }

    public double getCancelAt() {
        return cancelAt;
    }

    public boolean isSellAll() {
        return sellAll;
    }

    @Override
    public String toString() {
        return "StopLossOption{" +
                "marketName='" + marketName + '\'' +
                ", cancelAt=" + cancelAt +
                ", condition=" + condition +
                ", mode=" + mode +
                ", sellAll=" + sellAll +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StopLossOption that = (StopLossOption) o;

        if (!marketName.equals(that.marketName)) return false;
        return condition == that.condition;
    }

    @Override
    public int hashCode() {
        int result = marketName.hashCode();
        result = 31 * result + condition.hashCode();
        return result;
    }
}
