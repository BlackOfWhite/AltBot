package org.logic.utils;

import org.apache.log4j.Logger;
import org.logic.models.JSONParser;
import org.logic.models.responses.MarketBalancesResponse;
import org.logic.models.responses.MarketOrderResponse;
import org.logic.models.responses.MarketSummaryResponse;
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
            e.printStackTrace();
        }
        logger.debug(marketBalances);
        return marketBalances;
    }

    public static MarketOrderResponse buildAllOpenOrders() {
        MarketOrderResponse openMarketOrders = null;
        try {
            String response = MarketRequests.getOpenOrders();
            openMarketOrders = JSONParser.parseMarketOrder(response);
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
//        logger.debug(marketSummary);
        return marketSummary;
    }


}
