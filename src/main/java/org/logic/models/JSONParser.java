package org.logic.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.logic.models.responses.*;
import org.logic.models.responses.v2.MarketTicksResponse;

import static org.preferences.Constants.REQUEST_TIMEOUT_SECONDS;

public class JSONParser {

    public static MarketSummaryResponse parseMarketSummary(String json) {
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        MarketSummaryResponse marketSummary = g.fromJson(json, MarketSummaryResponse.class);
        return marketSummary;
    }

    public static MarketOrderResponse parseMarketOrder(String json) {
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        MarketOrderResponse marketOrder = g.fromJson(json, MarketOrderResponse.class);
        return marketOrder;
    }

    public static MarketBalanceResponse parseMarketBalance(String json) {
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        MarketBalanceResponse marketBalance = g.fromJson(json, MarketBalanceResponse.class);
        return marketBalance;
    }

    public static MarketBalancesResponse parseMarketBalances(String json) {
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        MarketBalancesResponse marketBalances = g.fromJson(json, MarketBalancesResponse.class);
        return marketBalances;
    }

    public static MarketTicksResponse parseMarketTicksResponse(String json) {
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        MarketTicksResponse marketTicksResponse = g.fromJson(json, MarketTicksResponse.class);
        return marketTicksResponse;
    }

    public static OrderResponse parseOrderResponse(String json) {
        if (json.equals(REQUEST_TIMEOUT_SECONDS)) {
            OrderResponse orderResponse = new OrderResponse();
            orderResponse.setResponseTimedOut();
            return orderResponse;
        }
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return g.fromJson(json, OrderResponse.class);
    }


}
