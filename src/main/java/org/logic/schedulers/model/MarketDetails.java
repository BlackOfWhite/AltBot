package org.logic.schedulers.model;

public class MarketDetails {
    private double last;
    private double totalAmount;
    private boolean allowNoBalance;

    public MarketDetails(double last, double totalAmount) {
        this(last, totalAmount, false);
    }

    public MarketDetails(double last, double totalAmount, boolean allowNoBalance) {
        this.last = last;
        this.totalAmount = totalAmount;
        this.allowNoBalance = allowNoBalance;
    }


    public double getLast() {
        return last;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setLast(double last) {
        this.last = last;
    }

    public void setTotalAmount(double totalAmount) {
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