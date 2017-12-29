package org.logic.schedulers;

import org.apache.log4j.Logger;
import org.logic.models.JSONParser;
import org.logic.models.responses.MarketBalanceResponse;
import org.logic.models.responses.MarketOrderResponse;
import org.logic.models.responses.MarketSummaryResponse;
import org.logic.requests.MarketRequests;
import org.logic.requests.PublicRequests;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TransactionScheduler {

    private static final int TIME = 10;
    private static final String CURRENT_ALT_COIN = "OMG";
    private static final int PERCENT_GAIN = 5;
    public volatile static boolean active = false;
    private static Logger logger = Logger.getLogger(TransactionScheduler.class);
    private static TransactionScheduler instance;
    private static ScheduledExecutorService ses;

    private TransactionScheduler() {
    }

    public static TransactionScheduler getInstance() {
        if (instance == null) {
            instance = new TransactionScheduler();
            ses = Executors.newScheduledThreadPool(10);
        }
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
                    String response = PublicRequests.getMarketSummary(CURRENT_ALT_COIN);
                    MarketSummaryResponse marketSummary = JSONParser.parseMarketSummary(response);
                    logger.debug(marketSummary);
                    // get orders for alt
                    response = MarketRequests.getOpenOrders("XMR");
                    MarketOrderResponse marketOrder = JSONParser.parseMarketOrder(response);
                    logger.debug(marketOrder);
                    // get alt balance
                    response = MarketRequests.getBalance("XMR");
                    MarketBalanceResponse marketBalance = JSONParser.parseMarketBalance(response);
                    logger.debug(marketBalance);
                    // get btc balance
                    response = MarketRequests.getBalance("BTC");
                    MarketBalanceResponse marketBalanceBTC = JSONParser.parseMarketBalance(response);
                    logger.debug(marketBalanceBTC);
                    // get order history
                    response = MarketRequests.getOrderHistory(CURRENT_ALT_COIN);
                    MarketOrderResponse marketOrderHistory = JSONParser.parseMarketOrder(response);
                    logger.debug(marketOrderHistory);

                    placeOrder(marketSummary, marketOrder, marketBalance, marketBalanceBTC, marketOrderHistory);
                } catch (Exception e) {
                    logger.error(e.getMessage());
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

    private static void placeOrder(MarketSummaryResponse marketSummary, MarketOrderResponse marketOrder,
                                   MarketBalanceResponse marketBalance, MarketBalanceResponse marketBalanceBTC,
                                   MarketOrderResponse marketOrderHistory) {
        double high = marketSummary.getResult().get(0).getHigh();
        double low = marketSummary.getResult().get(0).getLow();
        double ask = marketSummary.getResult().get(0).getAsk(); // willing to sell
        double bid = marketSummary.getResult().get(0).getBid(); // willing to buy
        double last = marketSummary.getResult().get(0).getLast();

        // No orders placed yet
        if (marketOrder.getResult().isEmpty()) {
            logger.debug("There are no orders. OK.");
            // Check if last action was completed. Should be since we have no open orders..
            if (marketOrderHistory.getResult().get(0).getQuantityRemaining() < 0.000000) {
                logger.debug("Last action is complete. OK.");
                // What was the last action?
                // Sell
                if (marketOrderHistory.getResult().get(0).getOrderType().equals("LIMIT_SELL")) {
                    // Last action was Sell so now we buy.
                    logger.debug("Last action was sell so now we buy. OK.");
                    // Do we have any BTC?
                    if (!marketBalanceBTC.getResult().isEmpty()) {
                        logger.debug("There are some BTC in the wallet. OK.");
                        double btcBalance = marketBalanceBTC.getResult().getAvailable();
                        // We have no coins of this type. So we place a buy order.
                        if (marketBalance.getResult().isEmpty()) {
                            logger.debug("You do not own any coin of this type (alt). OK.");
                            if (last < ((high + low) / 2.0d)) {
                                logger.debug("Last transaction price was below low/high avg. OK.");
                                double quantity = round(btcBalance / last);
                                logger.debug("Trying to buy " + quantity + " units of " + CURRENT_ALT_COIN + " for " + last + ".");
                                try {
//                                    MarketRequests.placeOrderBuy(CURRENT_ALT_COIN, quantity, last);
                                    logger.debug("Placed an order to buy " + quantity + " alt. OK.");
                                } catch (Exception e) {
                                    logger.error("Failed to place an order to buy " + quantity + " alt. FAIL.");
                                }
                            }
                        }
                    }
                }
                // Buy
                else if (marketOrderHistory.getResult().get(0).getOrderType().equals("LIMIT_BUY")) {
                    // Last action was Buy so now we sell all alt.
                    logger.debug("Last action was buy so now we sell. OK.");
                    // Do we have any alt?
                    if (!marketBalance.getResult().isEmpty()) {
                        logger.debug("There are some alt in the wallet. OK.");
                        double boughtAt = marketOrderHistory.getResult().get(0).getPricePerUnit();
                        // We want to sell it with {@profit} % profit.
                        double quantity = marketBalance.getResult().getBalance();
                        double sellFor = boughtAt + (boughtAt * (PERCENT_GAIN / 100));
                        logger.debug("Trying to sell " + quantity + " units of " + CURRENT_ALT_COIN + " for " + sellFor + ".");
                        try {
//                            MarketRequests.placeOrderSell(CURRENT_ALT_COIN, quantity, sellFor);
                            logger.debug("Placed an order to sell " + quantity + " alt. OK.");
                        } catch (Exception e) {
                            logger.error("Failed to place an order to sell " + quantity + " alt. FAIL.");
                        }
                    }
                }
            }
        }
        logger.debug("PlaceOrder done..");
    }


    private static double round(double d) {
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        return Double.parseDouble(df.format(d));
    }

}
