package org.logic.transactions;

import org.apache.log4j.Logger;
import org.logic.models.responses.OrderResponse;
import org.logic.utils.ModelBuilder;
import org.logic.validators.TransactionValidator;

public class ClassicTransaction {

    private String marketName;
    private double rate, amount;
    private Logger logger = Logger.getLogger(ClassicTransaction.class);
    private boolean isBuying; // true for buy option, otherwise is sell

    public ClassicTransaction(String marketName, double amount, double rate, boolean isBuying) {
        this.marketName = marketName;
        this.rate = rate;
        this.amount = amount;
        this.isBuying = isBuying;
    }

    public String createClassicTransaction() {
        String message;
        if (isOnlyOnePerMarketAllowed()) {
            if (new TransactionValidator(marketName).isOnlyOneOrderForGivenMarketName()) {
                return "There is already at least one order for this market. Please close all open orders to create a new order for market " + marketName;
            }
        }
        try {
            OrderResponse orderResponse = isBuying ? ModelBuilder.buildBuyOrder(marketName, amount, rate) :
                    ModelBuilder.buildSellOrder(marketName, amount, rate);
            if (!orderResponse.isSuccess()) {
                return orderResponse.getMessage();
            }
            String uuid = orderResponse.getResult().getUuid();
            message = "Successfully created new order with id: " + uuid + " for market " + marketName + ".";
        } catch (Exception e) {
            logger.error(e.getMessage() + "\n" + e.getStackTrace().toString());
            return "Error: " + e.getMessage() + "\n" + e.getStackTrace().toString() + ". Transaction not completed.";
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
    
    public boolean isOnlyOnePerMarketAllowed() {
        return false;
    }
}
