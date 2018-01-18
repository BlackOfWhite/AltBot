package org.logic.schedulers.model;

public class MarketDetails {
    private double last;
    private double totalAmount;

    public MarketDetails(double last, double totalAmount) {
        this.last = last;
        this.totalAmount = totalAmount;
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

    @Override
    public String toString() {
        return "MarketDetails{" +
                "last=" + last +
                ", totalAmount=" + totalAmount +
                '}';
    }
}