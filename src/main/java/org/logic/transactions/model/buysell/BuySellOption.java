package org.logic.transactions.model.buysell;

import org.logic.transactions.model.OptionImpl;

import java.io.Serializable;

public class BuySellOption implements Serializable, OptionImpl {
    private static final long serialVersionUID = 1L;
    private String marketName;

    public BuySellOption(String marketName) {
        this.marketName = marketName;
    }

    public String getMarketName() {
        return marketName;
    }

    @Override
    public String toString() {
        return "BuySellOption{" +
                "marketName='" + marketName + "'\'}'";
    }
}