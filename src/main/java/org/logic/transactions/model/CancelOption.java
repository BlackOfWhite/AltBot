package org.logic.transactions.model;

import java.io.Serializable;

public class CancelOption implements Serializable{

    private String marketName;
    private double cancelBelow;
    private String uuid;

    public CancelOption(String marketName, double cancelBelow, String uuid) {
        this.marketName = marketName;
        this.cancelBelow = cancelBelow;
        this.uuid = uuid;
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
