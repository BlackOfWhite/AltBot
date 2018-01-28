package org.logic.schedulers;

import org.apache.log4j.Logger;
import org.logic.models.responses.MarketBalanceResponse;
import org.logic.models.responses.MarketOrderResponse;
import org.logic.models.responses.MarketSummaryResponse;
import org.logic.models.responses.OrderResponse;
import org.logic.transactions.model.buysell.BotAvgOption;
import org.logic.transactions.model.buysell.BotAvgOptionManager;
import org.logic.utils.ModelBuilder;
import org.preferences.Params;
import org.preferences.managers.PreferenceManager;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TransactionScheduler {

    //    public static final String marketName = "XVG";
    private static final int TIME = 4;
    //    private static final double buyBelowRatio = 0.975d; // 2.5% below avg
//    private static final double totalGainRatio = 1.035d; // 3.5% above bought price
//    private static final double sellAndResetRatio = 0.094d; // 6% will auto sell also below this, below bought price
//    private static final double btc = 0.017;
    public volatile static boolean active = false;
    private static Logger logger = Logger.getLogger(TransactionScheduler.class);
    private static TransactionScheduler instance;
    private static ScheduledExecutorService ses;

    // History map
    public static final int LIST_OK_SIZE = 20; // 560 - 45min


    // Cancel pending order after N tries, if occasion missed.
    private static final int CANCEL_IDLE_ORDER_AFTER_N_TRIES = 100;
    private static HashMap<String, Integer> idleOrderCounters = new HashMap<>();

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
            HashSet<String> disabledMarkets = new HashSet<>();
            List<BotAvgOption> botAvgOptions = BotAvgOptionManager.getInstance().getOptionList();
            try {
                // Check for which coins there are no open orders. Cancel idle orders.
                final MarketOrderResponse marketOrderResponse = ModelBuilder.buildAllOpenOrders();
                for (BotAvgOption botAvgOption : botAvgOptions) {
                    String altCoin = botAvgOption.getMarketName();
                    for (MarketOrderResponse.Result result : marketOrderResponse.getResult()) {
                        // Checks if there is any open order for given coin.
                        if (result.getExchange().endsWith(altCoin)) {
                            disabledMarkets.add(altCoin);
                            if (!idleOrderCounters.containsKey(altCoin)) {
                                idleOrderCounters.put(altCoin, 0);
                            }
                            int idleOrderCounter = idleOrderCounters.get(altCoin);
                            if (idleOrderCounter < CANCEL_IDLE_ORDER_AFTER_N_TRIES) {
                                logger.debug("There are still pending orders for " + altCoin + ".");
                                idleOrderCounter++;
                                idleOrderCounters.put(altCoin, idleOrderCounter);
                                continue;
                            } else {
                                // Cancel idle order
                                logger.debug("Trying to cancel idle " + result.getOrderType() + " order for " + altCoin + " market.");
                                OrderResponse orderResponse = ModelBuilder.buildCancelOrderById(result.getOrderUuid());
                                if (orderResponse.isSuccess()) {
                                    logger.debug("Successfully cancelled idle " + result.getOrderType() + " order for " + altCoin + " market.");
                                    idleOrderCounters.put(altCoin, 0);
                                } else {
                                    logger.debug("Failed to cancel idle " + result.getOrderType() + " order for " + altCoin + " market.");
                                }
                                continue;
                            }
                        }
                    }
                }

                MarketBalanceResponse marketBalanceBtc = ModelBuilder.buildMarketBalance("BTC");
                logger.debug(marketBalanceBtc);
                if (!marketBalanceBtc.isSuccess()) {
                    logger.debug("Failed to get total BTC balance - aborting!");
                    return;
                }

                for (BotAvgOption botAvgOption : botAvgOptions) {
                    if (disabledMarkets.contains(botAvgOption.getMarketName())) {
                        continue;
                    }
                    final String altCoin = botAvgOption.getMarketName();
                    MarketSummaryResponse marketSummary = ModelBuilder.buildMarketSummary(altCoin);
                    logger.debug(marketSummary);
                    MarketBalanceResponse marketBalanceAlt = ModelBuilder.buildMarketBalance(altCoin);
                    logger.debug(marketBalanceAlt);
                    MarketOrderResponse marketOrderHistory = ModelBuilder.buildMarketOrderHistory(altCoin);
                    logger.debug(marketOrderHistory);
                    placeOrder(altCoin, botAvgOption, marketSummary, marketOrderHistory, marketBalanceBtc, marketBalanceAlt);
                    //                placeOrderAlignToLowest(marketSummary, marketOrderHistory, marketBalanceBtc, marketBalanceAlt);
                }
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
     * @param marketName         In "BTC-ETH" format.
     * @param marketSummary
     * @param marketOrderHistory
     * @param marketBalanceBtc
     * @param marketBalanceAlt
     */
    private static void placeOrder(final String marketName, BotAvgOption botAvgOption, MarketSummaryResponse marketSummary, MarketOrderResponse marketOrderHistory,
                                   MarketBalanceResponse marketBalanceBtc, MarketBalanceResponse marketBalanceAlt) {
        int size = MarketMonitor.getInstance().priceHistoryMap.get(marketName).size();
        if (size < LIST_OK_SIZE) {
            logger.debug("Too few records [" + size + "/" + LIST_OK_SIZE + "] in the history map to start a bot for " + marketName + ". Aborting.");
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
            double avg = MarketMonitor.getInstance().avgValueMap.get(marketName);
            double buyBelow = avg * botAvgOption.getBuyBelowRatio();
            logger.debug("Trying to place a buy order for " + marketName + ". Last: " + last + ", buyBelow: " + buyBelow + " [" + (last / buyBelow) + "].");
            double btcBalance = marketBalanceBtc.getResult().getAvailable();
            if (btcBalance < botAvgOption.getBtc()) {
                logger.debug("Not enough BTC. You have " + btcBalance);
                return;
            }
            if (last > buyBelow) {
                logger.debug("Last price is too high to place a buy order.");
                return;
            }
            double quantity = round(botAvgOption.getBtc() / last);
            logger.debug("Trying to buy " + quantity + " units of " + marketName + " for " + last + ".");
            buy(botAvgOption, quantity, last);
        } else {
            double lastTimeBought = botAvgOption.getBoughtAt();
            double sellAbove = lastTimeBought * botAvgOption.getTotalGainRatio();
            double sellAndResetBelow = lastTimeBought * botAvgOption.getSellAndResetRatio();
            logger.debug("Trying to place a sell order for " + marketName + ". Last: " + last + ", sellAbove: " + sellAbove + " [" + (last / sellAbove) + "]." +
                    " Reset at " + sellAndResetBelow + " [" + last / sellAndResetBelow + "]");
            if (!buy) {
                // Last action was Buy so now we sell all alt.
                // Must have certain gain from this transaction. We also safe sell if price drops too much in relation to bought price.
                if (last >= sellAbove || last < sellAndResetBelow) {
                    double quantity = marketBalanceAlt.getResult().getBalance();
                    logger.debug("Trying to sell " + quantity + " units of " + marketName + " for " + last + ".");
                    sell(marketName, quantity, last);
                }
            }
        }
    }

   /*
    private static void placeOrderAlignToLowest(String marketName, MarketSummaryResponse marketSummary, MarketOrderResponse marketOrderHistory,
                                                MarketBalanceResponse marketBalanceBtc, MarketBalanceResponse marketBalanceAlt) {
        double last = marketSummary.getResult().get(0).getLast();
        double low = marketSummary.getResult().get(0).getLow();
        // Now we sell or buy?
        boolean buy;
        try {
            buy = marketOrderHistory.getResult().get(0).getOrderType().equalsIgnoreCase("LIMIT_SELL");
        } catch (Exception e) {
            logger.debug("Failed to check if last was buy or sell. Is this a first transaction?");
            buy = true;
        }

        if (marketBalanceAlt.getResult().isEmpty() && buy) {
            logger.debug("Trying to place a buy order for " + marketName + ".");
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
            logger.debug("Trying to buy " + quantity + " units of " + marketName + " for " + last + ".");
            buy(marketName, quantity, last);
        } else {
            logger.debug("Trying to place sell order for " + marketName + ".");
            if (!buy) {
                // Last action was Buy so now we sell all alt.
                if (last >= (low * 1.018)) {
                    double quantity = marketBalanceAlt.getResult().getBalance();
                    logger.debug("Trying to sell " + quantity + " units of " + marketName + " for " + last + ".");
                    sell(marketName, quantity, last);
                }
            }
        }
    }*/

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

    /**
     * @param botAvgOption Use full market name, e.g. BTC-ETH.
     * @param quantity
     * @param last
     */
    private static void buy(BotAvgOption botAvgOption, double quantity, double last) {
        String marketName = botAvgOption.getMarketName();
        try {
            OrderResponse orderResponse = ModelBuilder.buildBuyOrder(marketName, quantity, last);
            if (orderResponse.isSuccess()) {
                logger.debug("Success - Placed an order to buy " + quantity + " " + marketName +
                        " for " + last + " each.");
                botAvgOption.setBoughtAt(last);
                BotAvgOptionManager.getInstance().updateOption(botAvgOption);
            } else {
                logger.debug("Fail - Placed an order to buy " + quantity + " " + marketName +
                        " for " + last + " each.\n" + orderResponse);
            }
        } catch (Exception e) {
            logger.error("Failed to place an order to buy " + quantity + " alt. FAIL.");
        }
    }

    private static void sell(String marketName, double quantity, double last) {
        try {
            OrderResponse orderResponse = ModelBuilder.buildSellOrder("BTC-" + marketName, quantity, last);
            if (orderResponse.isSuccess()) {
                logger.debug("Success - Placed an order to sell " + quantity + " of " + marketName +
                        " for " + last + " each.");
            } else {
                logger.debug("Fail - Placed an order to sell " + quantity + " of " + marketName +
                        " for " + last + " each.\n" + orderResponse);
            }
        } catch (Exception e) {
            logger.error("Failed to place an order to sell " + quantity + " alt. FAIL.");
        }
    }
}

