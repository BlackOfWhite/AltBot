package org.logic.transactions.model;

import java.io.Serializable;

public class CancelOption implements Serializable {

    private String marketName;
    private double cancelBelow;
    private String uuid;
    private int threshold; // 1-99%. default is 5%.

    public CancelOption(String marketName, double cancelBelow, String uuid) {
        this(marketName, cancelBelow, uuid, 5);
    }

    public CancelOption(String marketName, double cancelBelow, String uuid, int threshold) {
        this.marketName = marketName;
        this.cancelBelow = cancelBelow;
        this.uuid = uuid;
        this.threshold = threshold;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getMarketName() {
        return marketName;
    }

    public double getCancelBelow() {
        return cancelBelow;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "CancelOption{" +
                "marketName='" + marketName + '\'' +
                ", cancelBelow=" + cancelBelow +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
