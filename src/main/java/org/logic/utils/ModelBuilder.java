package org.logic.utils;

import org.apache.log4j.Logger;
import org.logic.models.JSONParser;
import org.logic.models.responses.*;
import org.logic.requests.MarketRequests;
import org.logic.requests.PublicRequests;

public class ModelBuilder {

    private static Logger logger = Logger.getLogger(ModelBuilder.class);

    public static MarketBalancesResponse buildMarketBalances() {
        MarketBalancesResponse marketBalances = null;
        try {
            String response = MarketRequests.getBalances();
            marketBalances = JSONParser.parseMarketBalances(response);
        } catch (Exception e) {
            logger.error(e.getMessage() + "\nFailed to create MarketBalancesResponse object");
        }
//        logger.debug(marketBalances);
        return marketBalances;
    }

    public static MarketBalanceResponse buildMarketBalance(final String coinName) {
        MarketBalanceResponse marketBalance = null;
        try {
            String response = MarketRequests.getBalance(coinName);
            marketBalance = JSONParser.parseMarketBalance(response);
        } catch (Exception e) {
            logger.error(e.getMessage() + "\nFailed to create MarketBalanceResponse object");
        }
//        logger.debug(marketBalance);
        return marketBalance;
    }

    public static MarketOrderResponse buildAllOpenOrders() {
        MarketOrderResponse openMarketOrders = null;
        try {
            String response = MarketRequests.getOpenOrders();
            openMarketOrders = JSONParser.parseMarketOrder(response);
        } catch (Exception e) {
            logger.error(e.getMessage() + "\nFailed to create MarketOrderResponse object");
        }
        logger.debug(openMarketOrders);
        return openMarketOrders;
    }

    public static MarketSummaryResponse buildMarketSummary(String marketName) {
        MarketSummaryResponse marketSummary = null;
        try {
            String response = PublicRequests.getMarketSummary(marketName);
            marketSummary = JSONParser.parseMarketSummary(response);
        } catch (Exception e) {
            logger.error(e.getMessage() + "\nFailed to create MarketSummaryResponse object");
        }
//        logger.debug(marketSummary);
        return marketSummary;
    }

    public static OrderResponse buildCancelOrderById(String uuid) {
        OrderResponse orderResponse = null;
        try {
            String response = MarketRequests.cancelOrder(uuid);
            orderResponse = JSONParser.parseOrderResponse(response);
        } catch (Exception e) {
            logger.error(e.getMessage() + "\nFailed to create OrderResponse object");
        }
//        logger.debug(marketSummary);
        return orderResponse;
    }
}
