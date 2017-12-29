package org.logic.requests;

import org.apache.log4j.Logger;
import org.logic.utils.Converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static org.preferences.Constants.MSG_REQUEST_TIMEOUT;
import static org.preferences.Constants.REQUEST_TIMEOUT_SECONDS;
import static org.preferences.Params.API_KEY;

public class MarketRequests {

    private static Logger logger = Logger.getLogger(MarketRequests.class);

    public static String placeOrderBuy(String marketName, double quantity, double rate) throws Exception {
        long nonce = System.currentTimeMillis();
        URL url = new URL("https://bittrex.com/api/v1.1/market/buylimit?apikey=" + API_KEY +
                "&nonce=" + nonce +
                "&market=" + marketName +
                "&quantity=" + quantity + "&rate=" + rate
        );
        return sendRequest(url);
    }

    public static String placeOrderSell(String marketName, double quantity, double rate) throws Exception {
        long nonce = System.currentTimeMillis();
        URL url = new URL("https://bittrex.com/api/v1.1/market/selllimit?apikey=" + API_KEY +
                "&nonce=" + nonce +
                "&market=" + marketName +
                "&quantity=" + quantity + "&rate=" + rate
        );
        return sendRequest(url);
    }

    public static String getOpenOrders() throws Exception {
        long nonce = System.currentTimeMillis();
        URL url = new URL("https://bittrex.com/api/v1.1/market/getopenorders?apikey=" + API_KEY + "&nonce=" + nonce);
        return sendRequest(url);
    }

    public static String getOpenOrders(String altCoin) throws Exception {
        long nonce = System.currentTimeMillis();
        URL url = new URL("https://bittrex.com/api/v1.1/market/getopenorders?apikey=" + API_KEY + "&nonce=" + nonce + "&market=BTC-" + altCoin);
        return sendRequest(url);
    }

    public static String getOrderHistory(String altCoin) throws Exception {
        long nonce = System.currentTimeMillis();
        URL url = new URL("https://bittrex.com/api/v1.1/account/getorderhistory?apikey=" + API_KEY + "&nonce=" + nonce + "&market=btc-" + altCoin);
        return sendRequest(url);
    }


    public static String getBalance(String altCoin) throws Exception {
        long nonce = System.currentTimeMillis();
        URL url = new URL("https://bittrex.com/api/v1.1/account/getbalance?apikey=" + API_KEY + "&nonce=" + nonce + "&currency=" + altCoin);
        return sendRequest(url);
    }

    public static String getBalances() throws Exception {
        long nonce = System.currentTimeMillis();
        URL url = new URL("https://bittrex.com/api/v1.1/account/getbalances?apikey=" + API_KEY + "&nonce=" + nonce);
        return sendRequest(url);
    }

    public static String cancelOrder(final String uuid) throws Exception {
        long nonce = System.currentTimeMillis();
        URL url = new URL("https://bittrex.com/api/v1.1/market/cancel?apikey=" + API_KEY + "&nonce=" + nonce + "&uuid=" + uuid);
        return sendRequest(url);
    }

    private static String sendRequest(URL url) {
        // prepare sign
        String response = "";
        Converter converter = new Converter(url.toString());
        String sign = converter.calculate();
        URLConnection urlConnection = null;
        try {
            urlConnection = url.openConnection();
            urlConnection.setRequestProperty("apisign", sign);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            urlConnection.getInputStream()));
            // SET REQUEST TIMEOUT
            String inputLine;
            long endTimeMillis = System.currentTimeMillis() + (REQUEST_TIMEOUT_SECONDS * 1000);
            while ((inputLine = in.readLine()) != null) {
//                logger.debug(inputLine);
                response = inputLine;
                if (System.currentTimeMillis() > endTimeMillis) {
                    return MSG_REQUEST_TIMEOUT;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}


