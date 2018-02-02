package org.logic.schedulers.bots;

import org.apache.log4j.Logger;
import org.logic.models.responses.MarketBalanceResponse;
import org.logic.models.responses.MarketOrderResponse;
import org.logic.models.responses.MarketSummaryResponse;
import org.logic.models.responses.OrderResponse;
import org.logic.schedulers.bots.model.MarketVolumeAndLast;
import org.logic.transactions.model.bots.BotAvgOption;
import org.logic.transactions.model.bots.BotAvgOptionManager;
import org.logic.utils.ModelBuilder;
import org.preferences.Params;
import org.preferences.managers.PreferenceManager;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeepBot {

    private static final int TIME_NORMAL_POLL = 5; // Use this one if history data was populated.
    // Cancel pending order after N tries, if occasion missed.
    private static final int CANCEL_IDLE_ORDER_AFTER_N_TRIES = 24; // must be short. this bot is very quick. 24 * TIME_NORMAL_POLL sec = 120 sec.
    private static final int LIST_MAX_SIZE = 120;// 10 min = TIME_NORMAL_POLL * 12 * 10 = 120
    private static final double MIN_DROP_RATIO = 0.99;
    private static final double STOP_LOSS_RATIO = 0.95; // sell if rate of sellAbove * this ratio is lower.
    private static final int EXHAUSTION_TIME = 120; // 10 minutes. Market is disabled for 10 minutes after successful sell.
    public volatile static boolean active = false;
    private static volatile double sellAbove = 0.01;
    private static Logger logger = Logger.getLogger(DeepBot.class);
    private static DeepBot instance;
    private static ScheduledExecutorService ses;
    private static HashMap<String, Integer> idleOrderCounters = new HashMap<>();
    private static HashMap<String, LinkedList<MarketVolumeAndLast>> marketHistoryMap = new HashMap<>();
    private static HashMap<String, Integer> marketExhausted = new HashMap<>();

    private DeepBot() {
    }

    public static DeepBot getInstance() {
        if (instance == null) {
            instance = new DeepBot();
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
            executeRun();
        }, 0, TIME_NORMAL_POLL, TimeUnit.SECONDS);  // execute every x seconds
        active = true;
    }

    /**
     * Execute scheduler's tasks. There are 2 modes - HISTORY and NORMAL. HISTORY mode is used to fetch historical
     * data from Bittrex. It has fixed poll time equal to TIME_HISTORY_POLL. In this mode bot will not execute any
     * other actions.
     * <p>
     * After all historical data was fetched, bot switches NORMAL mode with fixed poll time equal to TIME_NORMAL_POLL.
     * In this mode it will fetch latest data from monitored markets. Then it will execute all operations related to
     * BOT_AVG.
     */
    private static void executeRun() {
        logger.debug("\nNew run..");
        List<BotAvgOption> botAvgOptions = BotAvgOptionManager.getInstance().getOptionList();
        // There is already market history.
        for (BotAvgOption botAvgOption : botAvgOptions) {
            String marketName = botAvgOption.getMarketName();
            MarketSummaryResponse marketSummaryResponse = ModelBuilder.buildMarketSummary(marketName);
            if (!marketSummaryResponse.isSuccess()) {
                logger.debug("Failed to get market's last value.");
                continue;
            }
            LinkedList<MarketVolumeAndLast> history = null;
            if (marketHistoryMap.containsKey(marketName)) {
                history = marketHistoryMap.get(marketName);
                history.add(new MarketVolumeAndLast(marketSummaryResponse.getResult().get(0).getVolume(),
                        marketSummaryResponse.getResult().get(0).getLast()));
                // Trim
                if (history.size() > LIST_MAX_SIZE) {
                    history.removeFirst();
                }
            } else {
                history = new LinkedList<>();
                history.add(new MarketVolumeAndLast(marketSummaryResponse.getResult().get(0).getVolume(),
                        marketSummaryResponse.getResult().get(0).getLast()));
                marketHistoryMap.put(marketName, history);
            }
//            logger.debug("Added new tick for market: " + marketName + " [" + history.size() + "/" + LIST_MAX_SIZE + "].");
        }

        // 2. Check for which coins there are no open orders. Cancel idle orders.
        HashSet<String> disabledMarkets = new HashSet<>();
        try {
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
                        }
                    }
                }
            }

            // 3. Check exhausted markets. This prevents scenarios like buy/sell/buy from happening.
            // Market is exhausted after single buy/sell.
            Iterator<Map.Entry<String, Integer>> it = marketExhausted.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = it.next();
                int value = entry.getValue();
                if (value < EXHAUSTION_TIME) {
                    entry.setValue(value + 1);
                } else {
                    it.remove();
                }
            }

            // 4. Get necessary data.
            MarketBalanceResponse marketBalanceBtc = ModelBuilder.buildMarketBalance("BTC");
            logger.debug(marketBalanceBtc);
            if (!marketBalanceBtc.isSuccess()) {
                logger.debug("Failed to get total BTC balance - aborting!");
                return;
            }

            for (BotAvgOption botAvgOption : botAvgOptions) {
                final String altCoin = botAvgOption.getMarketName();
                if (disabledMarkets.contains(altCoin) || marketHistoryMap.get(altCoin).size() < LIST_MAX_SIZE || marketExhausted.containsKey(altCoin)) {
                    continue;
                }
                MarketSummaryResponse marketSummary = ModelBuilder.buildMarketSummary(altCoin);
                logger.debug(marketSummary);
                MarketBalanceResponse marketBalanceAlt = ModelBuilder.buildMarketBalance(altCoin);
                logger.debug(marketBalanceAlt);
                MarketOrderResponse marketOrderHistory = ModelBuilder.buildMarketOrderHistory(altCoin);
                logger.debug(marketOrderHistory);
                placeOrder(altCoin, botAvgOption, marketSummary, marketOrderHistory, marketBalanceBtc, marketBalanceAlt);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
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
     * Place BUY order if there is enough BTC, last is above average and volume is rising.
     * Place SELL order if last is below sellRatio * boughtAt price OR if last is below safeLoss * boughtAt.
     *
     * @param marketName         In "BTC-ETH" format.
     * @param marketSummary
     * @param marketOrderHistory
     * @param marketBalanceBtc
     * @param marketBalanceAlt
     */
    private static void placeOrder(final String marketName, BotAvgOption botAvgOption, MarketSummaryResponse marketSummary, MarketOrderResponse marketOrderHistory,
                                   MarketBalanceResponse marketBalanceBtc, MarketBalanceResponse marketBalanceAlt) {
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
            double priceAvg = calculateAverageLast(marketHistoryMap.get(marketName), 0);
            logger.debug("Trying to place a buy order for " + marketName + ". Last: " + last + ", avg: " + priceAvg + ".");
            double btcBalance = marketBalanceBtc.getResult().getAvailable();
            if (btcBalance < botAvgOption.getBtc()) {
                logger.debug("Not enough BTC. You have " + btcBalance);
                return;
            }
            if (!checkPriceSpread(marketName, priceAvg)) {
                logger.debug("Prices spread is too big.");
                return;
            }
            if (!shouldPlaceBuyOrder(last, marketName)) {
                logger.debug("Conditions not met to place a DEEP BOT's buy order.");
                return;
            }
            double quantity = round(botAvgOption.getBtc() / last);
            logger.debug("Trying to buy " + quantity + " units of " + marketName + " for " + last + ".");
            buy(botAvgOption, quantity, last);
        } else {
            double sellAbove = botAvgOption.getSellAbove();
            double sellAndResetBelow = botAvgOption.getStopLoss();
            logger.debug("Trying to place a sell order for " + marketName + ". Last: " + last + ", sellAbove: " + sellAbove + " [" + (last / sellAbove) + "]." +
                    " Reset at " + sellAndResetBelow + " [" + last / sellAndResetBelow + "]");
            if (!buy) {
                // Last action was Buy so now we sell all alt.
                // Must have certain gain from this transaction. We also safe sell if price drops too much in relation to bought price.
                if ((last >= sellAbove && sellAbove > 0) || (last < sellAndResetBelow && sellAndResetBelow > 0)) {
                    double quantity = marketBalanceAlt.getResult().getBalance();
                    logger.debug("Trying to sell " + quantity + " units of " + marketName + " for " + last + ".");
                    sell(marketName, quantity, last);
                }
            }
        }
    }

    /**
     * Compare prices from 2 last measurements. Returns true if latest price is lower by at least 1% then the price before it.
     */
    private static boolean shouldPlaceBuyOrder(double last, String marketName) {
        LinkedList<MarketVolumeAndLast> list = marketHistoryMap.get(marketName);
        // Last below 99% of avg. Last should be not too small.
        double preLast = list.get(list.size() - 2).getLast();
        // This condition is important.
        if (last / preLast > MIN_DROP_RATIO) {
            return false;
        }
        // Last must be below DROP_RATE. Compare to 3 last objects. Last 15 sec.
        sellAbove = calculateGainRatio(preLast, last);
        if (sellAbove <= 0) {
            return false;
        }
        logger.debug("Deep found: " + preLast + ", " + last + ", " + preLast / last);
        return true;
    }

    /**
     * Calculates gain ratio depending on price drop. Drop is calculated from pre-last price, not from last price.
     * Ratios:
     * 0.99 || +0.3%
     * 0.985 || +0.7%
     * 0.98 || +1.1%
     * 0.975 || +1.5%
     *
     * @param preLast
     * @param last
     * @return
     */
    private static double calculateGainRatio(double preLast, double last) {
        double ratio = last / preLast;
        if (ratio > 0.99d) {
            return -99999;
        }
        double ratioDrop = 1 - ratio; // min 1 - 0.99 = 0.01
        double distFromPreLast = ratioDrop * 0.2d; // min 0.01 * 0.2 = 0.002
        return preLast - (distFromPreLast * preLast);
    }

    /**
     * Checks if distance of 95% of all values of price history is lower than 1% of price.
     * Last price is not considered.
     *
     * @param avg
     * @return
     */
    private static boolean checkPriceSpread(String marketName, double avg) {
        int count = 0;
        double maxDistance = avg * 1.02d;
        double minDistance = avg * 0.98d;
        LinkedList<MarketVolumeAndLast> list = marketHistoryMap.get(marketName);
        for (int x = 0; x < list.size() - 1; x++) {
            double last = list.get(x).getLast();
            if (last >= minDistance && last <= maxDistance) {
                count++;
            }
        }
        double ratio = count / (marketHistoryMap.get(marketName).size() - 1);
        return (ratio >= 0.95d);
    }

    /**
     * Rounds down up to 6 decimal places. For example, 11,08654291 would be rounded to 11,086542.
     *
     * @param d
     * @return
     */
    private static double round(double d) {
        DecimalFormat df = new DecimalFormat("##.000000");
        df.setRoundingMode(RoundingMode.DOWN);
        String formatted = df.format(d);
        if (formatted.contains(",")) {
            formatted = formatted.replace(",", ".");
        }
        return Double.parseDouble(formatted);
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
                botAvgOption.setSellAbove(sellAbove);
                botAvgOption.setStopLoss(last * STOP_LOSS_RATIO);
                BotAvgOptionManager.getInstance().updateOption(botAvgOption);
            } else {
                logger.debug("Fail - Placed an order to buy " + quantity + " " + marketName +
                        " for " + last + " each.\n" + orderResponse);
            }
        } catch (Exception e) {
            logger.error("Failed to place an order to buy " + quantity + " alt.");
        }
    }

    private static void sell(String marketName, double quantity, double last) {
        try {
            OrderResponse orderResponse = ModelBuilder.buildSellOrder(marketName, quantity, last);
            if (orderResponse.isSuccess()) {
                logger.debug("Success - Placed an order to sell " + quantity + " of " + marketName +
                        " for " + last + " each.");
                // Mark market as exhausted.
                marketExhausted.put(marketName, 0);
            } else {
                logger.debug("Fail - Placed an order to sell " + quantity + " of " + marketName +
                        " for " + last + " each.\n" + orderResponse);
            }
        } catch (Exception e) {
            logger.error("Failed to place an order to sell " + quantity + " alt. FAIL.");
        }
    }

    private static double calculateAverageLast(LinkedList<MarketVolumeAndLast> list, int start) {
        double sum = 0;
        if (!list.isEmpty()) {
            for (int i = start; i < list.size(); i++) {
                sum += list.get(i).getLast();
            }
            return sum / list.size();
        }
        return -1;
    }
}
