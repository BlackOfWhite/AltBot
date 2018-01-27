package org.logic.utils;

import org.logic.models.misc.BalancesSet;

public class MarketNameUtils {

    public static String getMarketNameForCurrency(String currency) {
        if (currency.equalsIgnoreCase("USDT")) {
            return "USDT-BTC";
        }
        if (currency.equalsIgnoreCase("BTC")) {
            return "BTC";
        }
        return "BTC-" + currency;
    }

    public static String getCoinNameFromMarketName(String marketName) {
        if (marketName.contains("-")) {
            return marketName.substring(marketName.lastIndexOf("-") + 1);
        } else {
            return marketName;
        }
    }

}
