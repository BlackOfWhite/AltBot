package org.logic.transactions.model.stoploss;

import org.logic.transactions.model.OptionImpl;

import java.io.Serializable;

public class StopLossOption implements Serializable, OptionImpl {
    private static final long serialVersionUID = 1L;
    private String marketName;
    private double cancelAt;
    private StopLossCondition condition;

    public StopLossOption(String marketName, double cancelAt, StopLossCondition condition) {
        this.marketName = marketName;
        this.cancelAt = cancelAt;
        this.condition = condition;
    }

    public StopLossCondition getCondition() {
        return condition;
    }

    public String getMarketName() {
        return marketName;
    }

    public double getCancelAt() {
        return cancelAt;
    }

    @Override
    public String toString() {
        return "CancelOption{" +
                "marketName='" + marketName + '\'' +
                ", cancelAt=" + cancelAt +
                ", condition=" + condition +
                '}';
    }
}
