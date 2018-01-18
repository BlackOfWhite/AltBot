package org.logic.transactions.model.stoploss.modes;

public enum StopLossMode {
    SELL("Stop-loss only LIMIT_SELL"), BUY("Stop-loss only LIMIT_BUY"), BOTH("Both");

    private final String fieldDescription;

    private StopLossMode(String value) {
        fieldDescription = value;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }
}
