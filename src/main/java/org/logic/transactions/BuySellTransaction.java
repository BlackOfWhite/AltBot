package org.logic.transactions;

import org.apache.log4j.Logger;
import org.logic.models.JSONParser;
import org.logic.models.responses.OrderResponse;
import org.logic.requests.MarketRequests;
import org.logic.validators.TransactionValidator;

import java.io.IOException;

public class BuySellTransaction implements TransactionImpl {

    private String marketName;
    private double rate, amount;
    private Logger logger = Logger.getLogger(BuySellTransaction.class);
    private boolean isBuying; // true for buy option, otherwise is sell

    public BuySellTransaction(String marketName, double amount, double rate, boolean isBuying) {
        this.marketName = marketName;
        this.rate = rate;
        this.amount = amount;
        this.isBuying = isBuying;
    }

    public String createBuySellTransaction() {
        OrderResponse orderResponse;
        String message = "";
        if (isOnlyOnePerMarketAllowed()) {
            if (new TransactionValidator(marketName).isOnlyOneOrderForGivenMarketName()) {
                return "There is already at least one order for this market. Please close all open orders to create a new order for market " + marketName;
            }
        }
        try {
            final String response = isBuying ? MarketRequests.placeOrderBuy(marketName, amount, rate) :
                    MarketRequests.placeOrderSell(marketName, amount, rate);
            orderResponse = JSONParser.parseOrderResponse(response);
            if (!orderResponse.isSuccess()) {
                return orderResponse.getMessage();
            }
            String uuid = orderResponse.getResult().getUuid();
            message = "Successfully created new order with id: " + uuid + " for market " + marketName + ".";
        } catch (IOException ioe) {
            message += " Failed to register stop-loss monitor.";
            logger.error(ioe.getMessage() + "\n" + ioe.getStackTrace().toString());
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

    @Override
    public boolean isOnlyOnePerMarketAllowed() {
        return true;
    }
}
