package org.logic.utils;

import org.apache.log4j.Logger;
import org.logic.models.JSONParser;
import org.logic.models.requests.MarketOrder;
import org.logic.models.requests.MarketBalances;
import org.logic.models.requests.MarketSummary;
import org.logic.requests.MarketRequests;
import org.logic.requests.PublicRequests;

public class ModelBuilder {

    private static Logger logger = Logger.getLogger(ModelBuilder.class);

    public static MarketBalances buildMarketBalances() {
        MarketBalances marketBalances = null;
        try {
            String response = MarketRequests.getBalances();
            marketBalances = JSONParser.parseMarketBalances(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug(marketBalances);
        return marketBalances;
    }

    public static MarketOrder buildAllOpenOrders() {
        MarketOrder openMarketOrders = null;
        try {
            String response = MarketRequests.getOpenOrders();
            openMarketOrders = JSONParser.parseMarketOrder(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug(openMarketOrders);
        return openMarketOrders;
    }

    public static MarketSummary buildMarketSummary(String marketName) {
        MarketSummary marketSummary = null;
        try {
            String response = PublicRequests.getMarketSummary(marketName);
            marketSummary = JSONParser.parseMarketSummary(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        logger.debug(marketSummary);
        return marketSummary;
    }





}
