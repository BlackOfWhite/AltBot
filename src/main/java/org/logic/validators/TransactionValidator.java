package org.logic.validators;

import org.logic.models.requests.MarketOrder;
import org.logic.schedulers.MarketMonitor;

public class TransactionValidator {

    private String marketName;

    public TransactionValidator(String marketName) {
        this.marketName = marketName;
    }

    /**
     * Checks if given market name exists in the list of open orders.
     * Apply this validator if only one order of this kind can be opened.
     */
    public boolean isMarketValid() {
        MarketOrder marketOrders = MarketMonitor.getInstance().getOpenMarketOrders();
        if (marketOrders == null) {
            return false;
        }
        for (MarketOrder.Result result : marketOrders.getResult()) {
            if (result.getOrderType().equalsIgnoreCase(marketName)) {
                return false;
            }
        }
        return true;
    }

}