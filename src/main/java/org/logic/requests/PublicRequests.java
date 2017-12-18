package org.logic.requests;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class PublicRequests {

    private static String URL;
    private static Logger logger = Logger.getLogger(PublicRequests.class);

    public static void getAllMarketSummaries() throws Exception{
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

    public static String getMarketSummary(String marketName) throws Exception{
        URL url = new URL("https://bittrex.com/api/v1.1/public/getmarketsummary?market=" + marketName);
        URLConnection urlConnection = url.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        urlConnection.getInputStream()));
        String inputLine;
        String response = "";

        while ((inputLine = in.readLine()) != null) {
            logger.debug(inputLine);
            response = inputLine;
        }
        in.close();
        return response;
    }



}
