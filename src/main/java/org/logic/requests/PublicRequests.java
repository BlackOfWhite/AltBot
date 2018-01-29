package org.logic.requests;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static org.preferences.Constants.REQUEST_TIMEOUT_SECONDS;

public class PublicRequests {

    private static Logger logger = Logger.getLogger(PublicRequests.class);

    public static void getAllMarketSummaries() throws Exception {
        URL url = new URL("https://bittrex.com/api/v1.1/public/getmarketsummaries/");
        URLConnection urlConnection = url.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        urlConnection.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            logger.debug(inputLine);
        in.close();
    }

    public static String getMarketSummary(String marketName) throws Exception {
        URL url = new URL("https://bittrex.com/api/v1.1/public/getmarketsummary?market=" + marketName);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setReadTimeout(1000 * REQUEST_TIMEOUT_SECONDS);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        urlConnection.getInputStream()));
        String inputLine;
        String response = "";
        while ((inputLine = in.readLine()) != null) {
//            logger.debug(inputLine);
            response = inputLine;
        }
        in.close();
        return response;
    }

    /**
     * Use full market name.
     * Timestamp is starting period from which we will get ticks.
     * Default tick is 30 minutes.
     *
     * @param marketName
     * @param timestamp
     * @return
     * @throws Exception
     */
    public static String getMarketTicksWithInterval(String marketName, long timestamp) throws Exception {
        URL url = new URL("https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName=" + marketName +
                "&tickInterval=thirtyMin&_=1500915289433");
        URLConnection urlConnection = url.openConnection();
        urlConnection.setReadTimeout(1000 * REQUEST_TIMEOUT_SECONDS * 3);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        urlConnection.getInputStream()));
        String inputLine;
        String response = "";
        while ((inputLine = in.readLine()) != null) {
//            logger.debug(inputLine);
            response = inputLine;
        }
        in.close();
        return response;
    }

    /**
     * Use full market name. Get last market tick.
     * Default tick is 30 minutes.
     *
     * @param marketName
     * @param timestamp
     * @return
     * @throws Exception
     */
    public static String getMarketLastTick(String marketName, long timestamp) throws Exception {
        URL url = new URL("https://bittrex.com/Api/v2.0/pub/market/GetLatestTick?marketName=" + marketName +
                "&tickInterval=thirtyMin&_=1500915289433");
        URLConnection urlConnection = url.openConnection();
        urlConnection.setReadTimeout(1000 * REQUEST_TIMEOUT_SECONDS * 3);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        urlConnection.getInputStream()));
        String inputLine;
        String response = "";
        while ((inputLine = in.readLine()) != null) {
//            logger.debug(inputLine);
            response = inputLine;
        }
        in.close();
        return response;
    }


}
