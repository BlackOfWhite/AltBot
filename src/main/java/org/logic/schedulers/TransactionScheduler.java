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

import static org.preferences.Constants.BALANCE_MINIMUM;
import static org.preferences.Constants.DIALOG_FAILED_TO_LOAD_API_KEYS;


public class TransactionScheduler {

    private static final int TIME = 10;
    private static final String CURRENT_ALT_COIN = "XZC";
    private static final int PERCENT_GAIN = 5;
    public volatile static boolean active = false;
    private static Logger logger = Logger.getLogger(TransactionScheduler.class);
    private static TransactionScheduler instance;
    private static ScheduledExecutorService ses;

    private static final double sellAbove = 0.0068355;
    private static final double buyBelow = 0.00651;
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
                    // get alt info
                    MarketSummaryResponse marketSummary = ModelBuilder.buildMarketSummary("BTC-" + CURRENT_ALT_COIN);
                    logger.debug(marketSummary);
                    MarketBalanceResponse marketBalanceResponse = ModelBuilder.buildMarketBalance("BTC");
                    logger.debug(marketBalanceResponse);
                    // get order history
                    String response = MarketRequests.getOrderHistory(CURRENT_ALT_COIN);
                    MarketOrderResponse marketOrderHistory = JSONParser.parseMarketOrder(response);
                    logger.debug(marketOrderHistory);

                    placeOrder(marketSummary, marketOrderHistory, marketBalanceResponse);
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

    private static void placeOrder(MarketSummaryResponse marketSummary,
                                   MarketOrderResponse marketOrderHistory, MarketBalanceResponse marketBalanceResponse) {
        double last = marketSummary.getResult().get(0).getLast();
        // Was last sell or buy?
        boolean lastWasBuy = true;
        for (MarketOrderResponse.Result response : marketOrderHistory.getResult()) {
            if (response.getExchange().endsWith(CURRENT_ALT_COIN)) {
                if (response.getQuantityRemaining() > BALANCE_MINIMUM) {
                    logger.debug("There are still open order for this coin!");
                    return;
                } else {
                    if (response.getOrderType().equalsIgnoreCase("LIMIT_SELL")) {
                        lastWasBuy = false;
                        break;
                    } else if (response.getOrderType().equalsIgnoreCase("LIMIT_BUY")) {
                        lastWasBuy = true;
                        break;
                    } else {
                        return;
                    }
                }
            }
        }
        if (marketBalanceResponse.getResult().isEmpty()) {
            logger.debug("We do not have any btc!");
            return;
        }
        if (!lastWasBuy) {
            // Last action was Sell so now we buy.
            double btcBalance = marketBalanceResponse.getResult().getAvailable();
            if (btcBalance < btc) {
                logger.debug("Youdo nbot have enoguh btc");
                return;
            }
            // We have no coins of this type. So we place a buy order.
            if (marketSummary.getResult().isEmpty()) {
                logger.debug("You do not own any coin of this type (alt). OK.");
                if (last < buyBelow) {
                    logger.debug("Last transaction price was below low/high avg. OK.");
                    double quantity = round(btc / last);
                    logger.debug("Trying to buy " + quantity + " units of " + CURRENT_ALT_COIN + " for " + last + ".");
                    try {
//                                    MarketRequests.placeOrderBuy(CURRENT_ALT_COIN, quantity, last);
                        logger.debug("Placed an order to buy " + quantity + " alt. OK.");
                    } catch (Exception e) {
                        logger.error("Failed to place an order to buy " + quantity + " alt. FAIL.");
                    }
                }
            }
        } else {
            // Last action was Buy so now we sell all alt.
            double quantity = marketBalanceResponse.getResult().getBalance();
            double sellFor = sellAbove;
            logger.debug("Trying to sell " + quantity + " units of " + CURRENT_ALT_COIN + " for " + sellFor + ".");
            try {
//                            MarketRequests.placeOrderSell(CURRENT_ALT_COIN, quantity, sellFor);
                logger.debug("Placed an order to sell " + quantity + " alt. OK.");
            } catch (Exception e) {
                logger.error("Failed to place an order to sell " + quantity + " alt. FAIL.");
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
