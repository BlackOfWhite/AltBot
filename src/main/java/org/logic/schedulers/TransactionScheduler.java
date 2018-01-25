package org.logic.schedulers;

import org.apache.log4j.Logger;
import org.logic.models.JSONParser;
import org.logic.models.responses.MarketBalanceResponse;
import org.logic.models.responses.MarketOrderResponse;
import org.logic.models.responses.MarketSummaryResponse;
import org.logic.models.responses.OrderResponse;
import org.logic.requests.MarketRequests;
import org.logic.utils.ModelBuilder;
import org.preferences.Params;
import org.preferences.managers.PreferenceManager;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TransactionScheduler {

    public static final String CURRENT_ALT_COIN = "ADX";
    private static final int TIME = 4;
    private static final double buyBelowRatio = 0.98d; // 2% below avg
    private static final double totalGainRatio = 1.04d; // 4% above bought price
    private static final double sellAndResetRatio = 0.095d; // 5% will auto sell also below this, below bought price
    private static final double stopBelow = 0.0005;
    private static final double btc = 0.0155;
    public volatile static boolean active = false;
    private static Logger logger = Logger.getLogger(TransactionScheduler.class);
    private static TransactionScheduler instance;
    private static ScheduledExecutorService ses;

    private static double lastTimeBought = -1;

    private TransactionScheduler() {
    }

    public static TransactionScheduler getInstance() {
        if (instance == null) {
            instance = new TransactionScheduler();
            ses = Executors.newScheduledThreadPool(10);
        }
        loadAPIKeys();
        return instance;
    }

    public static void start() {
        if (active) {
            logger.debug("Scheduler already running");
            return;
        }
        if (ses.isShutdown()) {
            logger.debug("Recreating scheduler");
            ses = Executors.newScheduledThreadPool(10);
        }
        ses.scheduleAtFixedRate(() -> {
            logger.debug("\nNew run..");
            try {
                // Check if there are open orders for this coin:
                MarketOrderResponse marketOrderResponse = ModelBuilder.buildAllOpenOrders();
                for (MarketOrderResponse.Result result : marketOrderResponse.getResult()) {
                    if (result.getExchange().endsWith(CURRENT_ALT_COIN)) {
                        logger.debug("There are still pending orders for " + CURRENT_ALT_COIN + ".");
                        return;
                    }
                }

                MarketSummaryResponse marketSummary = ModelBuilder.buildMarketSummary("BTC-" + CURRENT_ALT_COIN);
                logger.debug(marketSummary);
                MarketBalanceResponse marketBalanceBtc = ModelBuilder.buildMarketBalance("BTC");
                logger.debug(marketBalanceBtc);
                MarketBalanceResponse marketBalanceAlt = ModelBuilder.buildMarketBalance(CURRENT_ALT_COIN);
                logger.debug(marketBalanceAlt);
                String response = MarketRequests.getOrderHistory(CURRENT_ALT_COIN);
                MarketOrderResponse marketOrderHistory = JSONParser.parseMarketOrder(response);
                logger.debug(marketOrderHistory);

                placeOrder(marketSummary, marketOrderHistory, marketBalanceBtc, marketBalanceAlt);
//                placeOrderAlignToLowest(marketSummary, marketOrderHistory, marketBalanceBtc, marketBalanceAlt);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }, 0, TIME, TimeUnit.SECONDS);  // execute every x seconds
        active = true;
    }

    public static void stop() {
        ses.shutdown();
        active = false;
        logger.debug("Scheduler stopped");
    }

    private static boolean isRunning() {
        if (ses.isShutdown() || ses.isTerminated()) {
            return true;
        }
        return active;
    }

    /**
     * If this is first order for this coin, it would be LIMIT_BUY.
     *
     * @param marketSummary
     * @param marketOrderHistory
     * @param marketBalanceBtc
     * @param marketBalanceAlt
     */
    private static void placeOrder(MarketSummaryResponse marketSummary, MarketOrderResponse marketOrderHistory,
                                   MarketBalanceResponse marketBalanceBtc, MarketBalanceResponse marketBalanceAlt) {
        if (MarketMonitor.getInstance().priceHistoryMap.get("BTC-" + CURRENT_ALT_COIN).size() < MarketMonitor.LIST_OK_SIZE) {
            logger.debug("Too few records in the history map to start a bot!");
            return;
        }
        double last = marketSummary.getResult().get(0).getLast();
        // Now we sell or buy?
        boolean buy;
        try {
            buy = marketOrderHistory.getResult().get(0).getOrderType().equalsIgnoreCase("LIMIT_SELL");
        } catch (Exception e) {
            logger.debug("Failed to check if last was buy or sell. Is this a first transaction?");
            buy = true;
        }
        if (marketBalanceAlt.getResult().isEmpty() && buy) {
            double avg = MarketMonitor.getInstance().avgValueMap.get("BTC-" + CURRENT_ALT_COIN);
            double buyBelow = avg * buyBelowRatio;
            logger.debug("Trying to place a buy order for " + CURRENT_ALT_COIN + ". Last: " + last + ", buyBelow: " + buyBelow + " [" + (last / buyBelow) + "].");
            double btcBalance = marketBalanceBtc.getResult().getAvailable();
            if (btcBalance < btc) {
                logger.debug("Not enough BTC. You have " + btcBalance);
                return;
            }
            if (last > buyBelow) {
                logger.debug("Last price is too high to place a buy order.");
                return;
            }
            double quantity = round(btc / last);
            logger.debug("Trying to buy " + quantity + " units of " + CURRENT_ALT_COIN + " for " + last + ".");
            buy(quantity, last);
        } else {
            double sellAbove = lastTimeBought * totalGainRatio;
            double sellAndResetBelow = lastTimeBought * sellAndResetRatio;
            logger.debug("Trying to place a sell order for " + CURRENT_ALT_COIN + ". Last: " + last + ", sellAbove: " + sellAbove + " [" + (last / sellAbove) + "].");
            if (!buy) {
                // Last action was Buy so now we sell all alt.
                // Must have certain gain from this transaction. We also safe sell if price drops too much in relation to bought price.
                if (last >= sellAbove || last < sellAndResetBelow) {
                    double quantity = marketBalanceAlt.getResult().getBalance();
                    logger.debug("Trying to sell " + quantity + " units of " + CURRENT_ALT_COIN + " for " + last + ".");
                    sell(quantity, last);
                }
            }
        }
    }

    /**
     * If this is first order for this coin, it would be LIMIT_BUY.
     *
     * @param marketSummary
     * @param marketOrderHistory
     * @param marketBalanceBtc
     * @param marketBalanceAlt
     */
    private static void placeOrderAlignToLowest(MarketSummaryResponse marketSummary, MarketOrderResponse marketOrderHistory,
                                                MarketBalanceResponse marketBalanceBtc, MarketBalanceResponse marketBalanceAlt) {
        double last = marketSummary.getResult().get(0).getLast();
        double low = marketSummary.getResult().get(0).getLow();
        // Stop
        if (last < stopBelow) {
            logger.debug("Aborting - is below stop value.");
            return;
        }
        // Now we sell or buy?
        boolean buy;
        try {
            buy = marketOrderHistory.getResult().get(0).getOrderType().equalsIgnoreCase("LIMIT_SELL");
        } catch (Exception e) {
            logger.debug("Failed to check if last was buy or sell. Is this a first transaction?");
            buy = true;
        }

        if (marketBalanceAlt.getResult().isEmpty() && buy) {
            logger.debug("Trying to place a buy order for " + CURRENT_ALT_COIN + ".");
            double btcBalance = marketBalanceBtc.getResult().getAvailable();
            if (btcBalance < btc) {
                logger.debug("Not enough BTC. You have " + btcBalance);
                return;
            }
            if (last > (1.005 * low)) {
                logger.debug("Last price is too high to place a buy order.");
                return;
            }
            double quantity = round((btcBalance * 0.95) / last);
            logger.debug("Trying to buy " + quantity + " units of " + CURRENT_ALT_COIN + " for " + last + ".");
            buy(quantity, last);
        } else {
            logger.debug("Trying to place sell order for " + CURRENT_ALT_COIN + ".");
            if (!buy) {
                // Last action was Buy so now we sell all alt.
                if (last >= (low * 1.018)) {
                    double quantity = marketBalanceAlt.getResult().getBalance();
                    logger.debug("Trying to sell " + quantity + " units of " + CURRENT_ALT_COIN + " for " + last + ".");
                    sell(quantity, last);
                }
            }
        }
    }

    private static double round(double d) {
        DecimalFormat df = new DecimalFormat("#,####");
        df.setRoundingMode(RoundingMode.CEILING);
        return Double.parseDouble(df.format(d));
    }

    private static boolean loadAPIKeys() {
        Params.API_KEY = PreferenceManager.getApiKey(true);
        Params.API_SECRET_KEY = PreferenceManager.getApiSecretKey(true);
        return true;
    }

    private static void buy(double quantity, double last) {
        try {
            OrderResponse orderResponse = ModelBuilder.buildBuyOrder("BTC-" + CURRENT_ALT_COIN, quantity, last);
            if (orderResponse.isSuccess()) {
                logger.debug("Success - Placed an order to buy " + quantity + " " + CURRENT_ALT_COIN +
                        " for " + last + " each.");
                lastTimeBought = last;
            } else {
                logger.debug("Fail - Placed an order to buy " + quantity + " " + CURRENT_ALT_COIN +
                        " for " + last + " each.\n" + orderResponse);
            }
        } catch (Exception e) {
            logger.error("Failed to place an order to buy " + quantity + " alt. FAIL.");
        }
    }

    private static void sell(double quantity, double last) {
        try {
            OrderResponse orderResponse = ModelBuilder.buildSellOrder("BTC-" + CURRENT_ALT_COIN, quantity, last);
            if (orderResponse.isSuccess()) {
                logger.debug("Success - Placed an order to sell " + quantity + " of " + CURRENT_ALT_COIN +
                        " for " + last + " each.");
            } else {
                logger.debug("Fail - Placed an order to sell " + quantity + " of " + CURRENT_ALT_COIN +
                        " for " + last + " each.\n" + orderResponse);
            }
        } catch (Exception e) {
            logger.error("Failed to place an order to sell " + quantity + " alt. FAIL.");
        }
    }
}

