package org.logic.schedulers.bots;

import org.apache.log4j.Logger;
import org.logic.models.responses.MarketBalanceResponse;
import org.logic.models.responses.MarketOrderResponse;
import org.logic.models.responses.MarketSummaryResponse;
import org.logic.models.responses.OrderResponse;
import org.logic.models.responses.v2.MarketTicksResponse;
import org.logic.schedulers.bots.model.MarketVolumeAndLast;
import org.logic.schedulers.bots.model.TimeIntervalEnum;
import org.logic.transactions.model.bots.BotAvgOption;
import org.logic.transactions.model.bots.BotAvgOptionManager;
import org.logic.utils.ModelBuilder;
import org.logic.utils.TimeUtils;
import org.preferences.Params;
import org.preferences.managers.PreferenceManager;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class AvgBot {

    // History map
    private static final int MARKET_TICKS_TIMESTAMP_PAST_HOURS = 2; // does not work

    private static final int TIME_HISTORY_POLL = 60; // Use 60 second interval - lowest possible.
    private static final int TIME_NORMAL_POLL = 10; // Use this one if history data was populated.
    private static final int VOLUME_RISE_INDICATOR = 2; // volume must grow by 4%
    private static final int AVG_RISE_INDICATOR = 2; // volume must grow by 4%
    // History map
    private static final TimeIntervalEnum POLL_INTERVAL = TimeIntervalEnum.oneMin;
    // Cancel pending order after N tries, if occasion missed.
    private static final int CANCEL_IDLE_ORDER_AFTER_N_TRIES = 30;
    private static final int INIT_LIST_MAX_SIZE = 160; // approx 2h - 160 * 1 min
    private static final int LIST_MAX_SIZE = 1800;// 30min = 10 * 6 * 30 = 1800
    // Ratios
    private static final double BUY_BELOW_RATIO = 0.99d;
    private static final double SELL_ABOVE_RATIO = 1.01d; //2.02% gain
    private static final double STOP_LOSS_RATIO = 0.95d; //2.02% gain
    public volatile static boolean active = false;
    private static ScheduledFuture<?> handle;
    private static boolean NORMAL_MODE_RUNNING = false;
    private static Logger logger = Logger.getLogger(AvgBot.class);
    private static AvgBot instance;
    private static ScheduledExecutorService ses;
    private static HashMap<String, Integer> idleOrderCounters = new HashMap<>();
    private static HashMap<String, LinkedList<MarketVolumeAndLast>> marketHistoryMap = new HashMap<>();

    private AvgBot() {
    }

    public static AvgBot getInstance() {
        if (instance == null) {
            instance = new AvgBot();
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
        handle = ses.scheduleAtFixedRate(() -> {
            executeRun();
        }, 0, TIME_HISTORY_POLL, TimeUnit.SECONDS);  // execute every x seconds
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

        // 1. Initialize or update markets history
        if (marketHistoryMap.size() != botAvgOptions.size()) {
            for (BotAvgOption botAvgOption : botAvgOptions) {
                String marketName = botAvgOption.getMarketName();
                if (!marketHistoryMap.containsKey(marketName)) {
                    logger.debug("Bot first run for " + marketName + " - initializing market ticks history.");
                    MarketTicksResponse marketTicksResponse = ModelBuilder.buildMarketTicks(marketName, TimeUtils.getTimestampPast(MARKET_TICKS_TIMESTAMP_PAST_HOURS), POLL_INTERVAL, TIME_HISTORY_POLL);
                    if (!marketTicksResponse.isSuccess()) {
                        logger.debug("Failed to get all market ticks - aborting.");
                        return;
                    }
                    // Trim
                    LinkedList<MarketTicksResponse.Result> history = marketTicksResponse.getResult();
                    LinkedList<MarketVolumeAndLast> copy = new LinkedList<>();
                    int size = history.size();
                    int start = history.size() > INIT_LIST_MAX_SIZE ? (size - INIT_LIST_MAX_SIZE) : 0;
                    for (int x = start; x < size; x++) {
                        copy.add(new MarketVolumeAndLast(history.get(x).getV(), history.get(x).getC()));
                    }
                    marketHistoryMap.put(marketName, copy);
                    logger.debug("Got history for market: " + marketName + ", size: " + marketTicksResponse.getResult().size() + ".");
                }
            }
            return;
        } else {
            // There is already market history.
            for (BotAvgOption botAvgOption : botAvgOptions) {
                String marketName = botAvgOption.getMarketName();
                if (marketHistoryMap.containsKey(marketName)) {
//                        MarketTicksResponse marketTicksResponse = ModelBuilder.buildMarketLastTick(marketName, TimeUtils.getTimestampPast(MARKET_TICKS_TIMESTAMP_PAST_HOURS), POLL_INTERVAL);
//                        if (!marketTicksResponse.isSuccess()) {
//                            logger.debug("Failed to get latest market ticks - aborting.");
//                            continue;
//                        } else {
//                            List<MarketTicksResponse.Result> history = marketHistoryMap.get(marketName);
//                            history.add(marketTicksResponse.getResult().get(0));
//                            // Trim
//                            if (history.size() > LIST_MAX_SIZE) {
//                                int size = history.size();
//                                List<MarketTicksResponse.Result> subHistory = history.subList(Math.max(size - LIST_MAX_SIZE, 0), size);
//                                marketHistoryMap.put(marketName, new LinkedList<>());
//                                marketHistoryMap.get(marketName).addAll(subHistory);
//                            }
//                            logger.debug("Added new tick for market: " + marketName + ".");
//                        }
                    MarketSummaryResponse marketSummaryResponse = ModelBuilder.buildMarketSummary(marketName);
                    if (!marketSummaryResponse.isSuccess()) {
                        logger.debug("Failed to get market's last value.");
                        continue;
                    } else {
                        LinkedList<MarketVolumeAndLast> history = marketHistoryMap.get(marketName);
                        history.add(new MarketVolumeAndLast(marketSummaryResponse.getResult().get(0).getVolume(),
                                marketSummaryResponse.getResult().get(0).getLast()));
                        // Trim
                        int size = history.size();
                        if (size > LIST_MAX_SIZE) {
                            history.removeFirst();
                        }
                        logger.debug("Added new tick for market: " + marketName + ".");
                    }
                }
            }
        }
        // Restart scheduler if history map was populated.
        if (!NORMAL_MODE_RUNNING) {
            handle.cancel(true);
            NORMAL_MODE_RUNNING = true;
            handle = ses.scheduleAtFixedRate(() -> {
                executeRun();
            }, 0, TIME_NORMAL_POLL, TimeUnit.SECONDS);
            return;
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

            // 3. Get necessary data.
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
            double buyBelow = priceAvg * BUY_BELOW_RATIO;
            logger.debug("Trying to place a buy order for " + marketName + ". Last: " + last + ", buyBelow: " + buyBelow + " [" + (last / buyBelow) + "].");
            double btcBalance = marketBalanceBtc.getResult().getAvailable();
            if (btcBalance < botAvgOption.getBtc()) {
                logger.debug("Not enough BTC. You have " + btcBalance);
                return;
            }
            if (last >= buyBelow) {
                logger.debug("Last price is too high to place a buy order.");
                return;
            }
            boolean avgRises = checkIfAvgRises(marketHistoryMap.get(marketName), AVG_RISE_INDICATOR);
            if (!avgRises) {
                logger.debug("Avg is decreasing for: " + marketName);
                return;
            }
            boolean volumeRises = checkIfVolumeRises(marketHistoryMap.get(marketName), VOLUME_RISE_INDICATOR);
            if (!volumeRises) {
                logger.debug("Volume is decreasing for: " + marketName);
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
                if (last >= sellAbove || last < sellAndResetBelow) {
                    double quantity = marketBalanceAlt.getResult().getBalance();
                    logger.debug("Trying to sell " + quantity + " units of " + marketName + " for " + last + ".");
                    sell(marketName, quantity, last);
                }
            }
        }
    }

    /**
     * Rounds down up to 6 decimal places. For example, 11,08654291 would be rounded to 11,086542.
     *
     * @param d
     * @return
     */
    private static double round(double d) {
        DecimalFormat df = new DecimalFormat("##.000000");
        df.setRoundingMode(RoundingMode.FLOOR);
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
                botAvgOption.setSellAbove(last * SELL_ABOVE_RATIO);
                botAvgOption.setStopLoss(last * STOP_LOSS_RATIO);
                BotAvgOptionManager.getInstance().updateOption(botAvgOption);
            } else {
                logger.debug("Fail - Placed an order to buy " + quantity + " " + marketName +
                        " for " + last + " each.\n" + orderResponse);
            }
        } catch (Exception e) {
            logger.error("Failed to place an order to buy " + quantity + " alt.");
            e.printStackTrace(); // TODO. Remove later.
        }
    }

    private static void sell(String marketName, double quantity, double last) {
        try {
            OrderResponse orderResponse = ModelBuilder.buildSellOrder(marketName, quantity, last);
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

    private static double calculateAverageVolume(LinkedList<MarketVolumeAndLast> list, int start) {
        double sum = 0;
        if (!list.isEmpty()) {
            for (int i = start; i < list.size(); i++) {
                sum += list.get(i).getVolume();
            }
            return sum / list.size();
        }
        return -1;
    }

    /**
     * Checks if volume rised by given percent.
     *
     * @param list
     * @return
     */
    private static boolean checkIfVolumeRises(LinkedList<MarketVolumeAndLast> list, int percent) {
        double lastV = list.getLast().getVolume();
        double firstV = list.getFirst().getVolume();
        double change = ((lastV - firstV) * 100.0d / firstV);
        if (change > 0 && change >= percent) {
            // Check if is above avg.
            double avg = calculateAverageVolume(list, 0);
            if (lastV > avg) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if avg ra\ised by given percent.
     *
     * @param list
     * @return
     */
    private static boolean checkIfAvgRises(LinkedList<MarketVolumeAndLast> list, int percent) {
        double lastC = list.getLast().getLast();
        double firstC = list.getFirst().getLast();
        double change = ((lastC - firstC) * 100.0d / firstC);
        if (change > 0 && change >= percent) {
            // Check if is above avg.
            double avg = calculateAverageLast(list, 0);
            if (lastC > avg) {
                return true;
            }
        }
        return false;
    }
}

