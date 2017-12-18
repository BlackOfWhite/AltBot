package org.logic.transactions;

import org.apache.log4j.Logger;
import org.logic.models.JSONParser;
import org.logic.models.responses.OrderResponse;
import org.logic.requests.MarketRequests;
import org.logic.transactions.model.CancelOption;

public class ClassicTransaction {

    private String marketName;
    private double rate, amount, cancelAt;
    private Logger logger = Logger.getLogger(ClassicTransaction.class);

    public ClassicTransaction(String marketName, double amount, double rate, double cancelAt) {
        this.marketName = marketName;
        this.rate = rate;
        this.amount = amount;
        this.cancelAt = cancelAt;
    }

    public String createClassicTransaction() {
        OrderResponse orderResponse;
        String message;
        try {
            final String response = MarketRequests.placeOrderBuy(marketName, amount, rate);
            orderResponse = JSONParser.parseOrderResponse(response);
            String uuid = orderResponse.getResult().get(0).getUuid();
            message = "Successfully created new order with id: " + uuid + " for market " + marketName + ".";
            if (cancelAt > 0.0d) {
                CancelOption cancelOption = new CancelOption(marketName, cancelAt, uuid);
                if (CancelOptionCollection.getInstance().addCancelOption(cancelOption)) {
                    message += " Stop-loss is set to: " + cancelOption + ".";
                } else {
                    message += " Failed to register stop-loss monitor.";
                }
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return "Error: " + e.getMessage() + ". Transaction not completed.";
        }
        return message;
    }

    public String getMarketName() {
        return marketName;
    }

    public double getRate() {
        return rate;
    }

    public double getAmount() {
        return amount;
    }

    public double getCancelAt() {
        return cancelAt;
    }
}
