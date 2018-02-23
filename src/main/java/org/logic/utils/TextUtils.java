package org.logic.utils;

public class TextUtils {

    public static String getDoubleAsText(double value, int decimal) {
        String v = String.format("%." + decimal + "f", value);
        if (v.contains(",")) {
            v = v.replace(",", ".");
        }
        return v;
    }

    public static String getDoubleAsText(double value) {
        return getDoubleAsText(value, 8);
    }

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
