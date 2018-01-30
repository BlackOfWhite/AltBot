package org.logic.validators;

import org.logic.models.responses.MarketOrderResponse;
import org.logic.schedulers.monitors.MarketMonitor;

public class TransactionValidator {

    private String marketName;

    public TransactionValidator(String marketName) {
        this.marketName = marketName;
    }

    /**
     * Checks if given market name exists in the list of open orders.
     * Apply this validator if only one order of this kind can be opened.
     */
    public boolean isOnlyOneOrderForGivenMarketName() {
        MarketOrderResponse marketOrders = MarketMonitor.getInstance().getOpenMarketOrders();
        if (marketOrders == null) {
            return false;
        }
        for (MarketOrderResponse.Result result : marketOrders.getResult()) {
            if (result.getOrderType().equalsIgnoreCase(marketName)) {
                return false;
            }
        }
        return true;
    }

}