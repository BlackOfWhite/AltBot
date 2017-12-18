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

}
