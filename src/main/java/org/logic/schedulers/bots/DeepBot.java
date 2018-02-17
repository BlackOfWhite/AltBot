package org.logic.schedulers.bots;

import org.apache.log4j.Logger;
import org.logic.exceptions.TooFewRecordsException;
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
import org.logic.utils.regression.LinearRegression;
import org.logic.utils.regression.Point;
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
    private static final int LIST_MAX_SIZE = 3;// 10 min = TIME_NORMAL_POLL * 12 * 10 = 120
    private static final double MIN_DROP_RATIO = 0.989;
    private static final double STOP_LOSS_RATIO = 0.99; // in relation to last price (bought price).
    // Market is disabled for 10 minutes after successful sell.
    private static final int EXHAUSTION_TIME = 120;
    // After 2minutes stop loss options can be executed. Used to not interfere with quick sells.
    private static final int STOP_LOSS_ACTIVATION_TIME = 24;
    private static final int HISTORICAL_DATA_MAX_SIZE = 45; // 4h. every minute
    public volatile static boolean active = false;
    private static volatile double sellAbove;
    private static Logger logger = Logger.getLogger(DeepBot.class);
    private static DeepBot instance;
    private static ScheduledExecutorService ses;
    private static HashMap<String, Integer> idleOrderCounters = new HashMap<>();
    private static HashMap<String, LinkedList<MarketVolumeAndLast>> marketHistoryMap = new HashMap<>();
    private static HashMap<String, Integer> marketExhausted = new HashMap<>();
    private static HashMap<String, Integer> stopLossActivationMap = new HashMap<>();
    // Historical
    private static HashMap<String, LinkedList<MarketVolumeAndLast>> historicalData = new HashMap<>();
    private static HashMap<String, Double> historicalDataRatio = new HashMap<>();
    private static int TICKS = 0;

    private DeepBot() {
    }

    public static DeepBot getInstance() {
        if (instance == null) {
            instance = new DeepBot();
            ses = Executors.newScheduledThreadPool(100);
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
            ses = Executors.newScheduledThreadPool(100);
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
        long start = System.currentTimeMillis();
        List<BotAvgOption> botAvgOptions = BotAvgOptionManager.getInstance().getOptionList();
        // 1. Get market history.
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
        double elapsed = (System.currentTimeMillis() - start);
        logger.debug("Elapsed: " + elapsed + " " + (int) (elapsed / 1000) + "." + elapsed % 1000);

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
            if (marketExhausted.size() > 0) {
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
            }

            // 4. Tick & historical data
            if (tickOneMin()) {
                getHistoricalData(botAvgOptions);
            }
            tick();

            // 5. Get necessary data.
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
            logger.debug("Trying to place a buy order for " + marketName + ". Last: " + last + ".");
            double btcBalance = marketBalanceBtc.getResult().getAvailable();
            // Check if there is enough btc in the wallet.
            if (btcBalance < botAvgOption.getBtc()) {
                logger.debug("Not enough BTC. You have " + btcBalance);
                return;
            }

            // More buy orders than sell order
            if (marketSummary.getResult().get(0).getOpenBuyOrders() < marketSummary.getResult().get(0).getOpenSellOrders()) {
                logger.debug("There is more sell orders than buy orders.");
            }

            // Check if price drop matches the conditions.
            if (!shouldPlaceBuyOrder(last, marketName)) {
                logger.debug("Conditions not met to place a DEEP BOT's buy order.");
                return;
            }

            // Check if market values are decreasing.
            try {
                // There is no key in the map if new historical data was fetched.
                if (!historicalDataRatio.containsKey(marketName)) {
                    if (isMarketInDeep(marketName)) {
                        logger.debug("Market is in deep.");
                        return;
                    }
                } else {
                    if (historicalDataRatio.get(marketName) < 0d) {
                        logger.debug("Market is in deep.");
                        return;
                    }
                }
            } catch (TooFewRecordsException e) {
                logger.error(e.getMessage());
                return;
            }

            double quantity = round(botAvgOption.getBtc() / last);
            logger.debug("Trying to buy " + quantity + " units of " + marketName + " for " + last + ".");
            buy(botAvgOption, quantity, last);
        } else {
            // Start stop-loss activator
            boolean allowStopLoss = true;
            if (stopLossActivationMap.containsKey(marketName)) {
                int count = stopLossActivationMap.get(marketName);
                if (count < STOP_LOSS_ACTIVATION_TIME) {
                    count++;
                    stopLossActivationMap.put(marketName, count);
                    allowStopLoss = false;
                } else {
                    stopLossActivationMap.remove(marketName);
                }
            }
            double sellAbove = botAvgOption.getSellAbove();
            double sellAndResetBelow = botAvgOption.getStopLoss();
            logger.debug("Trying to place a sell order for " + marketName + ". Last: " + last + ", sellAbove: " + sellAbove + " [" + (last / sellAbove) + "]." +
                    " Stop-loss at " + sellAndResetBelow + " [" + last / sellAndResetBelow + "]");
            if (!buy) {
                // Last action was Buy so now we sell all alt.
                // Must have certain gain from this transaction. We also safe sell if price drops too much in relation to bought price.
                double quantity = marketBalanceAlt.getResult().getBalance();
                if ((last >= sellAbove && sellAbove > 0)) {
                    logger.debug("Trying to sell " + quantity + " units of " + marketName + " for " + last + ".");
                    sell(marketName, quantity, last);
                } else if ((last <= sellAndResetBelow && sellAndResetBelow > 0 && allowStopLoss)) {
                    logger.debug("Trying to stop-loss " + quantity + " units of " + marketName + " for " + last + ".");
                    sell(marketName, quantity, last);
                }
            }
        }
    }

    /**
     * Compare prices from 2 last measurements. Returns true if latest price is lower by at least 1% then the price before it.
     *
     * @param last       Coin's last price.
     * @param marketName Full market name.
     * @return
     */
    private static boolean shouldPlaceBuyOrder(double last, String marketName) {
        LinkedList<MarketVolumeAndLast> list = marketHistoryMap.get(marketName);
        // Last below 99% of avg. Last should be not too small.
        double preLast = list.get(list.size() - 2).getLast();
        double deepRatio = last / preLast;
        // This condition is important.
        logger.debug("Looking for deep: " + preLast + ", " + last + ", " + deepRatio);
        // Last must be below DROP_RATE. Compare to 3 last objects. Last 15 sec.
        sellAbove = calculateSellPrice(preLast, last);
        return !(sellAbove <= 0);
    }

    /**
     * Calculates gain ratio depending on price drop. Drop is calculated from pre-last price, not from last price.
     * Ratios examples:
     * 0.99 || +0.3%
     * 0.985 || +0.7%
     * 0.98 || +1.1%
     * 0.975 || +1.5%
     *
     * @param preLast Coin's second last value.
     * @param last    Coin's last value.
     * @return Sell price. Returns negative value if drop ratio was too low.
     */
    private static double calculateSellPrice(double preLast, double last) {
        double ratio = last / preLast;
        if (ratio > MIN_DROP_RATIO) {
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
                // Activate new stop loss.
                stopLossActivationMap.put(marketName, 0);
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

    private static void tick() {
        TICKS++;
        TICKS = TICKS % 100000;
    }

    /**
     * Time normal poll must be lower than 30 sec.
     *
     * @return
     */
    private static boolean tickOneMin() {
        return TICKS % (60 / TIME_NORMAL_POLL) == 0;
    }

    /**
     * Get last price and volume data from 180 minutes.
     *
     * @param botAvgOptions
     */
    private static void getHistoricalData(List<BotAvgOption> botAvgOptions) {
        // There is already market history.
        for (BotAvgOption botAvgOption : botAvgOptions) {
            String marketName = botAvgOption.getMarketName();
            LinkedList<MarketVolumeAndLast> history = null;
            if (historicalData.containsKey(marketName)) {
                MarketTicksResponse marketTicksResponse = ModelBuilder.buildMarketLastTick(marketName, 1, TimeIntervalEnum.oneMin);
                if (!marketTicksResponse.isSuccess()) {
                    logger.debug("Failed to get market's last value.");
                    continue;

                }
                history = historicalData.get(marketName);
                history.add(new MarketVolumeAndLast(marketTicksResponse.getResult().get(0).getV(),
                        marketTicksResponse.getResult().get(0).getL()));
                // Trim
                if (history.size() > HISTORICAL_DATA_MAX_SIZE) {
                    history.removeFirst();
                }
                logger.debug("Added new historical tick for " + marketName + " coin.");
                // Remove last cached ratio.
                historicalDataRatio.remove(marketName);
            } else {
                MarketTicksResponse marketTicksResponse = ModelBuilder.buildMarketTicks(marketName, 1, TimeIntervalEnum.oneMin, 3);
                if (!marketTicksResponse.isSuccess()) {
                    logger.debug("Failed to get market's last value.");
                    continue;
                }
                history = new LinkedList<>();
                int size = marketTicksResponse.getResult().size();
                for (int x = size - HISTORICAL_DATA_MAX_SIZE; x < size; x++) {
                    history.add(new MarketVolumeAndLast(marketTicksResponse.getResult().get(x).getV(),
                            marketTicksResponse.getResult().get(x).getL()));
                }
                historicalData.put(marketName, history);
                logger.debug("Fetched historical data for " + marketName + ", size:" + history.size() + ".");
            }
        }
    }

    private static boolean isMarketInDeep(String marketName) throws TooFewRecordsException {
        if (historicalData.get(marketName).size() < HISTORICAL_DATA_MAX_SIZE) {
            String msg = "Too few objects in the historical data for " + marketName + " coin.";
            throw new TooFewRecordsException(msg);
        }
        int x = 0;
        List<Point> points = new ArrayList<>();
        for (MarketVolumeAndLast m : historicalData.get(marketName)) {
            points.add(new Point(x, m.getLast()));
            x++;
        }
        // Remove last one.
        points.remove(points.size() - 1);
        double ratioA = LinearRegression.linearRegression(points);
        historicalDataRatio.put(marketName, ratioA);
        return ratioA < 0;
    }
}
