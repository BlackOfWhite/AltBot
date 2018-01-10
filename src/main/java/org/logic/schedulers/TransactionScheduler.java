package org.logic.schedulers;

import org.apache.log4j.Logger;
import org.logic.models.JSONParser;
import org.logic.models.responses.MarketBalanceResponse;
import org.logic.models.responses.MarketOrderResponse;
import org.logic.models.responses.MarketSummaryResponse;
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

    private static final int TIME = 4;
    private static final String CURRENT_ALT_COIN = "XZC";
    private static final int PERCENT_GAIN = 5;
    public volatile static boolean active = false;
    private static Logger logger = Logger.getLogger(TransactionScheduler.class);
    private static TransactionScheduler instance;
    private static ScheduledExecutorService ses;

    private static final double sellAbove = 0.00678;
    private static final double buyBelow = 0.00658;
    private static final double stopBelow = 0.00625;
    private static final double btc = 0.004;


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
        ses.scheduleAtFixedRate(new Runnable() {
            public void run() {
                logger.debug("\nNew run..");
                try {
                    // Check if there are open orders for this coin:
                    MarketOrderResponse marketOrderResponse = ModelBuilder.buildAllOpenOrders();
                    for (MarketOrderResponse.Result result : marketOrderResponse.getResult()) {
                        if (result.getExchange().endsWith(CURRENT_ALT_COIN)) {
                            logger.debug("there still pending orders for this coin");
                            return;
                        }
                    }

                    // get alt info
                    MarketSummaryResponse marketSummary = ModelBuilder.buildMarketSummary("BTC-" + CURRENT_ALT_COIN);
                    logger.debug(marketSummary);
                    MarketBalanceResponse marketBalanceBtc = ModelBuilder.buildMarketBalance("BTC");
                    logger.debug(marketBalanceBtc);
                    MarketBalanceResponse marketBalanceAlt = ModelBuilder.buildMarketBalance(CURRENT_ALT_COIN);
                    logger.debug(marketBalanceAlt);
                    // get order history
                    String response = MarketRequests.getOrderHistory(CURRENT_ALT_COIN);
                    MarketOrderResponse marketOrderHistory = JSONParser.parseMarketOrder(response);
                    logger.debug(marketOrderHistory);

                    placeOrder(marketSummary, marketOrderHistory, marketBalanceBtc, marketBalanceAlt);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
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

//    private static void placeOrder(MarketSummaryResponse marketSummary,
//                                   MarketOrderResponse marketOrderHistory, MarketBalanceResponse marketBalanceResponse) {
//        double high = marketSummary.getResult().get(0).getHigh();
//        double low = marketSummary.getResult().get(0).getLow();
//        double ask = marketSummary.getResult().get(0).getAsk(); // willing to sell
//        double bid = marketSummary.getResult().get(0).getBid(); // willing to buy
//        double last = marketSummary.getResult().get(0).getLast();
//
////        // No orders placed yet
////        if (marketOrder.getResult().isEmpty()) {
////            logger.debug("There are no orders. OK.");
//            // Check if last action was completed. Should be since we have no open orders..
//            if (marketOrderHistory.getResult().get(0).getQuantityRemaining() < BALANCE_MINIMUM) {
//                logger.debug("Last action is complete. OK.");
//                // What was the last action?
//                // Sell
//                if (marketOrderHistory.getResult().get(0).getOrderType().equals("LIMIT_SELL")) {
//                    // Last action was Sell so now we buy.
//                    logger.debug("Last action was sell so now we buy. OK.");
//                    // Do we have any BTC?
//                    if (!marketBalanceResponse.getResult().isEmpty()) {
//                        logger.debug("There are some BTC in the wallet. OK.");
//                        double btcBalance = marketBalanceResponse.getResult().getAvailable();
//                        // We have no coins of this type. So we place a buy order.
//                        if (marketSummary.getResult().isEmpty()) {
//                            logger.debug("You do not own any coin of this type (alt). OK.");
//                            if (last < ((high + low) / 2.0d)) {
//                                logger.debug("Last transaction price was below low/high avg. OK.");
//                                double quantity = round(btcBalance / last);
//                                logger.debug("Trying to buy " + quantity + " units of " + CURRENT_ALT_COIN + " for " + last + ".");
//                                try {
////                                    MarketRequests.placeOrderBuy(CURRENT_ALT_COIN, quantity, last);
//                                    logger.debug("Placed an order to buy " + quantity + " alt. OK.");
//                                } catch (Exception e) {
//                                    logger.error("Failed to place an order to buy " + quantity + " alt. FAIL.");
//                                }
//                            }
//                        }
//                    }
//                }
//                // Buy
//                else if (marketOrderHistory.getResult().get(0).getOrderType().equals("LIMIT_BUY")) {
//                    // Last action was Buy so now we sell all alt.
//                    logger.debug("Last action was buy so now we sell. OK.");
//                    // Do we have any alt?
//                    if (!marketBalance.getResult().isEmpty()) {
//                        logger.debug("There are some alt in the wallet. OK.");
//                        double boughtAt = marketOrderHistory.getResult().get(0).getPricePerUnit();
//                        // We want to sell it with {@profit} % profit.
//                        double quantity = marketBalance.getResult().getBalance();
//                        double sellFor = boughtAt + (boughtAt * (PERCENT_GAIN / 100));
//                        logger.debug("Trying to sell " + quantity + " units of " + CURRENT_ALT_COIN + " for " + sellFor + ".");
//                        try {
////                            MarketRequests.placeOrderSell(CURRENT_ALT_COIN, quantity, sellFor);
//                            logger.debug("Placed an order to sell " + quantity + " alt. OK.");
//                        } catch (Exception e) {
//                            logger.error("Failed to place an order to sell " + quantity + " alt. FAIL.");
//                        }
//                    }
//                }
//            }
//        }
//        logger.debug("PlaceOrder done..");
//    }

    private static void placeOrder(MarketSummaryResponse marketSummary, MarketOrderResponse marketOrderHistory,
                                   MarketBalanceResponse marketBalanceBtc, MarketBalanceResponse marketBalanceAlt) {
        double last = marketSummary.getResult().get(0).getLast();
        // Now we sell or buy?
        boolean buy;
        try {
            buy = marketOrderHistory.getResult().get(0).getOrderType().equalsIgnoreCase("LIMIT_SELL");
        } catch (Exception e) {
            logger.debug("failed to check if last was buy or sell");
            return;
        }

        if (last < stopBelow) {
            logger.debug("Stopping because its below stop losss");
            return;
        }
        if (marketBalanceAlt.getResult().isEmpty() && buy) {
            logger.debug("trying to buy");
            double btcBalance = marketBalanceBtc.getResult().getAvailable();
            if (btcBalance < btc) {
                logger.debug("Tryna to buy but you dont have enoguh btc");
                return;
            }
            // We have no coins of this type. So we place a buy order.
            logger.debug("You do not own any coin of this type (alt). OK.");
            if (last < buyBelow) {
                logger.debug("Last transaction price was below low/high avg. OK.");
                double quantity = round(btc / last);
                logger.debug("Trying to buy " + quantity + " units of " + CURRENT_ALT_COIN + " for " + last + ".");
                try {
//                                    MarketRequests.placeOrderBuy(CURRENT_ALT_COIN, quantity, last + 0.00002);
                    logger.debug("Woo: Placed an order to buy " + quantity + " " + CURRENT_ALT_COIN +
                            " for " + last + " each.");
                } catch (Exception e) {
                    logger.error("Failed to place an order to buy " + quantity + " alt. FAIL.");
                }
            }

        } else {
            logger.debug("trying to sell");
            if (!buy) {
                logger.debug("last was buy now we sell.");
                // Last action was Buy so now we sell all alt.
                if (last > sellAbove) {
                    double quantity = marketBalanceAlt.getResult().getBalance();
                    logger.debug("Trying to sell " + quantity + " units of " + CURRENT_ALT_COIN + " for " + last + ".");
                    try {
                        MarketRequests.placeOrderSell(CURRENT_ALT_COIN, quantity, last - 0.00002);
                        logger.debug("WOO: Placed an order to sell " + quantity + " of " + CURRENT_ALT_COIN +
                                " for " + last + " each.");
                    } catch (Exception e) {
                        logger.error("Failed to place an order to sell " + quantity + " alt. FAIL.");
                    }
                }
            }
        }
    }

    private static double round(double d) {
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        return Double.parseDouble(df.format(d));
    }

    private static boolean loadAPIKeys() {
        Params.API_KEY = PreferenceManager.getApiKey(true);
        Params.API_SECRET_KEY = PreferenceManager.getApiSecretKey(true);
        return true;
    }

}
