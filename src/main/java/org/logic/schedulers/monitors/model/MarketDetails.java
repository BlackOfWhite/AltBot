package org.logic.schedulers.monitors.model;

import org.logic.exceptions.ValueNotSetException;

import static org.preferences.Constants.VALUE_NOT_SET;

public class MarketDetails {
    private Double last;
    private Double totalAmount;
    private boolean allowNoBalance;

    public MarketDetails(Double last, Double totalAmount) {
        this(last, totalAmount, false);
    }

    public MarketDetails(Double last, Double totalAmount, boolean allowNoBalance) {
        this.last = last;
        this.totalAmount = totalAmount;
        this.allowNoBalance = allowNoBalance;
    }

    public Double getLast() throws ValueNotSetException {
        if (last == VALUE_NOT_SET) {
            throw new ValueNotSetException("Last value not set.");
        }
        return last;
    }

    public void setLast(Double last) {
        this.last = last;
    }

    public Double getTotalAmount() throws ValueNotSetException {
        if (totalAmount == VALUE_NOT_SET) {
            throw new ValueNotSetException("TotalAmount not set.");
        }
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isAllowNoBalance() {
        return allowNoBalance;
    }

    @Override
    public String toString() {
        return "MarketDetails{" +
                "last=" + last +
                ", totalAmount=" + totalAmount +
                '}';
    }
}