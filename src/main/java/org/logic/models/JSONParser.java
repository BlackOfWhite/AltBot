package org.logic.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.logic.models.requests.MarketBalance;
import org.logic.models.requests.MarketBalances;
import org.logic.models.requests.MarketOrder;
import org.logic.models.requests.MarketSummary;
import org.logic.models.responses.OrderResponse;

public class JSONParser {

    public static MarketSummary parseMarketSummary (String json) {
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        MarketSummary marketSummary = g.fromJson(json, MarketSummary.class);
        return marketSummary;
    }

    public static MarketOrder parseMarketOrder (String json) {
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        MarketOrder marketOrder = g.fromJson(json, MarketOrder.class);
        return marketOrder;
    }

    public static MarketBalance parseMarketBalance (String json) {
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        MarketBalance marketBalance = g.fromJson(json, MarketBalance.class);
        return marketBalance;
    }

    public static MarketBalances parseMarketBalances (String json) {
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        MarketBalances marketBalances = g.fromJson(json, MarketBalances.class);
        return marketBalances;
    }

    public static OrderResponse parseOrderResponse (String json) {
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return g.fromJson(json, OrderResponse.class);
    }



}
