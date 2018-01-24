package org.logic.schedulers;

import org.apache.log4j.Logger;
import org.logic.models.misc.BalancesSet;
import org.logic.models.responses.*;
import org.logic.schedulers.model.MarketDetails;
import org.logic.smtp.MailSender;
import org.logic.transactions.model.stoploss.StopLossOption;
import org.logic.transactions.model.stoploss.StopLossOptionManager;
import org.logic.transactions.model.stoploss.modes.StopLossCondition;
import org.logic.transactions.model.stoploss.modes.StopLossMode;
import org.logic.utils.MarketNameUtils;
import org.logic.utils.ModelBuilder;
import org.preferences.Params;
import org.preferences.managers.PreferenceManager;
import org.ui.frames.MainFrame;
import org.ui.views.dialog.box.InfoDialog;
import org.ui.views.dialog.box.SingleInstanceDialog;

import javax.mail.MessagingException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.logic.schedulers.TransactionScheduler.CURRENT_ALT_COIN;
import static org.preferences.Constants.*;

public class MarketMonitor {

    private static final int SLEEP_TIME = 5;
    private static final String SUBJECT = "Open orders status changed!";
    public volatile static boolean active = false;
    public volatile static int COUNTER = -1;
    private static Logger logger = Logger.getLogger(MarketMonitor.class);
    private static MarketMonitor instance;
    private static ScheduledExecutorService ses;
    private static volatile MarketOrderResponse sharedMarketOrders;
    private static int ORDERS_COUNT = -1;
    private static MainFrame mainFrame;
    private static SingleInstanceDialog dialog;
    private static int DIALOG_DELAY = 5; // show dialog every 5 runs

    public static volatile HashMap<String, LinkedList<Double>> priceHistoryMap;
    public static HashMap<String, Double> avgValueMap;
    public static final int LIST_MAX_SIZE = 5000;
    public static final int LIST_OK_SIZE = 500;

    private static final int RETRY_COUNT = 3;

    private MarketMonitor() {
    }

    public static MarketMonitor getInstance() {
        if (instance == null) {
            instance = new MarketMonitor();
            priceHistoryMap = new HashMap<>();
            avgValueMap = new HashMap<>();
            ses = Executors.newScheduledThreadPool(10);
        }
        return instance;
    }

    public static void start(final MainFrame jFrame) {
        MarketMonitor.mainFrame = jFrame;
        if (active) {
            logger.debug("Scheduler already running");
            return;
        }
        if (ses.isShutdown()) {
            logger.debug("Recreating scheduler..");
            ses = Executors.newScheduledThreadPool(10);
        }
        ses.scheduleAtFixedRate(new Runnable() {
            public void run() {
                logger.debug("\nNew run..");
                try {
                    // Open market orders & settings validation
                    COUNTER = (COUNTER + 1) % 10000;
                    if (!loadAPIKeys()) {
                        return;
                    }
                    final MarketOrderResponse openMarketOrders = ModelBuilder.buildAllOpenOrders();
                    if (!validateResponse(openMarketOrders)) {
                        return;
                    }

                    // Main frame status bar
                    sharedMarketOrders = openMarketOrders;
                    final int totalOrdersCount = openMarketOrders.getResult().size();
                    final int buyOrdersCount = openMarketOrders.getBuyOrdersCount();
                    updateMainFrameStatus(totalOrdersCount, buyOrdersCount);

                    MarketBalancesResponse marketBalances = ModelBuilder.buildMarketBalances();

                    // Market name - last price map
                    final Map<String, MarketDetails> marketDetailsMap = createMarketDetailsMap(marketBalances, openMarketOrders);
                    if (marketDetailsMap != null) {
                        mainFrame.getPieChartFrame().setIsConnected(true);
                        updatePieChart(marketBalances, marketDetailsMap);
                        stopLossOrders(openMarketOrders, marketDetailsMap);
                    } else {
                        logger.debug("Some HTTP responses lost, not updating PieChart and Stop-loss orders!");
                        mainFrame.getPieChartFrame().setIsConnected(false);
                    }
                    sendNotification(totalOrdersCount);
                } catch (Exception e) {
                    logger.error(e.toString());
                    e.printStackTrace();
                }
            }
        }, 0, SLEEP_TIME, TimeUnit.SECONDS);  // execute every x seconds
        active = true;
        logger.debug("Scheduler started");
    }

    public static void stop() {
        ses.shutdown();
        active = false;
        logger.debug("Scheduler stopped");
    }

    public static boolean isRunning() {
        if (ses.isShutdown() || ses.isTerminated()) {
            return false;
        }
        return active;
    }

    private static void sendNotification(int openOrderCount) {
        if (ORDERS_COUNT == -1) {
            ORDERS_COUNT = openOrderCount;
            return;
        }
        if (ORDERS_COUNT != openOrderCount) {
            if (PreferenceManager.isEmailNotificationEnabled()) {
                String messageText = "Number of open orders changed from " + ORDERS_COUNT + " to " + openOrderCount;
                MailSender ms = new MailSender();
                try {
                    ms.sendEmailNotification(messageText, SUBJECT);
                } catch (MessagingException e) {
                    logger.error(e.getMessage() + "\n" + e.getStackTrace().toString());
                    if (COUNTER % DIALOG_DELAY == 0) {
                        new InfoDialog("Failed to authenticate email " + PreferenceManager.getEmailAddress());
                    }
                }
                logger.debug(messageText);
                ORDERS_COUNT = openOrderCount;
            }
        }
    }

    private static void updateMainFrameStatus(int totalOrders, int buyOrders) {
        mainFrame.updateStatusBar(totalOrders, buyOrders);
        if (COUNTER % DIALOG_DELAY == 0) {
            mainFrame.updateAPIStatusBar();
        }
    }

    /**
     * Price map is used only to get last price.
     *
     * @param marketBalances
     * @param marketDetailsMap
     */
    private static void updatePieChart(final MarketBalancesResponse marketBalances,
                                       final Map<String, MarketDetails> marketDetailsMap) {
        if (!mainFrame.isPieChartVisible()) {
            return;
        }
        Map<String, BalancesSet> map = new HashMap<>();
        for (MarketBalancesResponse.Result result : marketBalances.getResult()) {
            if (result.getBalance() < BALANCE_MINIMUM) {
                continue;
            }
            String marketName = MarketNameUtils.getMarketNameForCurrency(result.getCurrency());
            if (marketName.equals("BTC")) {
                map.put(result.getCurrency(), new BalancesSet(result.getBalance(), result.getBalance()));
            } else {
                double last = marketDetailsMap.get(marketName).getLast();
                double btc = last * result.getBalance();
                if (marketName.equalsIgnoreCase("USDT-BTC")) {
                    btc = result.getBalance() * (1 / last);
                }
                map.put(result.getCurrency(), new BalancesSet(result.getBalance(), btc));
            }
        }
        mainFrame.updatePieChartFrame(map);
    }

    public static MarketOrderResponse getOpenMarketOrders() {
        return sharedMarketOrders;
    }

    private static boolean loadAPIKeys() {
        if (COUNTER % DIALOG_DELAY != 0) {
            return true;
        }
        Params.API_KEY = PreferenceManager.getApiKey(true);
        Params.API_SECRET_KEY = PreferenceManager.getApiSecretKey(true);
        if (Params.API_KEY.trim().isEmpty() || Params.API_SECRET_KEY.trim().isEmpty()) {
            showDialog(DIALOG_FAILED_TO_LOAD_API_KEYS);
            mainFrame.updateAPIStatusBar();
            return false;
        }
        return true;
    }

    private static boolean validateResponse(Response response) {
        if (COUNTER % DIALOG_DELAY != 0) {
            return true;
        }
        if (response == null) {
            showDialog(NO_INTERNET_CONNECTION);
            return false;
        }
        if (!response.isSuccess() || response.getMessage().equals(MSG_APIKEY_INVALID)) {
            showDialog(DIALOG_INVALID_API_KEYS);
            return false;
        }
        return true;
    }

    /**
     * Creates a map containing currency and its attributes. Merges all currencies from market balances and open orders.
     * It is used to wallet chart and for stop orders, therefore only these currencies with balance greater than zero would be picked (this reduces number of requests to Bittrex).
     *
     * @param marketBalancesResponse
     * @param openMarketOrders
     * @return
     */
    public static Map<String, MarketDetails> createMarketDetailsMap(MarketBalancesResponse
                                                                            marketBalancesResponse, MarketOrderResponse openMarketOrders) {
        if (marketBalancesResponse == null || openMarketOrders == null) {
            logger.error("Either MarketBalancesResponse or MarketOrderResponse is null. MarketDetailsMap not created.");
            return null;
        }

        Map<String, MarketDetails> map = new HashMap<>();
        // Take only non-zero currencies from market balance.
        for (MarketBalancesResponse.Result result : marketBalancesResponse.getResult()) {
            String currency = result.getCurrency();
            if (result.getBalance() < BALANCE_MINIMUM) {
                continue;
            }
            if (!currency.startsWith("BTC")) {
                MarketDetails marketDetails = new MarketDetails(VALUE_NOT_SET, result.getBalance());
                if (currency.startsWith("USDT")) {
                    map.put(result.getCurrency() + "-BTC", marketDetails);
                } else {
                    map.put("BTC-" + result.getCurrency(), marketDetails);
                }
            }
        }
        // Take every currency from open orders.
        for (MarketOrderResponse.Result result : openMarketOrders.getResult()) {
            if (!map.containsKey(result.getExchange())) {
                map.put(result.getExchange(), new MarketDetails(VALUE_NOT_SET, VALUE_NOT_SET));
            }
        }
        // Add special currencies used just in TransactionScheduler. Just to collect their history.
        if (!map.containsKey(CURRENT_ALT_COIN)) {
            map.put("BTC-" + CURRENT_ALT_COIN, new MarketDetails(VALUE_NOT_SET, VALUE_NOT_SET, true));
        }
        // Merge and get last price.
        for (Map.Entry<String, MarketDetails> entry : map.entrySet()) {
            if (map.get(entry.getKey()).getTotalAmount() > BALANCE_MINIMUM || entry.getValue().isAllowNoBalance()) {
                MarketSummaryResponse marketSummary = ModelBuilder.buildMarketSummary(entry.getKey());
                try {
                    // update last price
                    MarketDetails marketDetails = entry.getValue();
                    marketDetails.setLast(marketSummary.getResult().get(0).getLast());
                    entry.setValue(marketDetails);
                    // Update map of average values
                    updatePriceHistoryMap(entry.getKey(), marketDetails.getLast());
                } catch (NullPointerException ex) {
                    logger.error("Invalid market:" + entry.getKey() + "\n" + ex);
                    return null;
                } catch (Exception ex) {
                    logger.error("Invalid market:" + entry.getKey() + "\n" + ex);
                    return null;
                }
            }
        }
        logger.debug("MarketDetails map: " + map.toString());
        return map;
    }

    private static void showDialog(String msg) {
        if (dialog == null || dialog.isClosed()) {
            dialog = new SingleInstanceDialog(msg);
        }
    }

    /**
     * This method will find all orders (candidates) for which stop-loss should be executed.
     * If given order's id is in the collection of stop orders, it will be cancelled and new sell order would be placed.
     * Current price must lower than stop-loss price minus THRESHOLD - useful in case when price drops dramatically.
     *
     * @param openMarketOrders
     * @return
     */
    private static void stopLossOrders(MarketOrderResponse openMarketOrders, Map<String, MarketDetails> marketDetailsMap) {
        // Make a copy here!
        List<StopLossOption> stopLossOptionList = new ArrayList<>(StopLossOptionManager.getInstance().getOptionList());
        logger.info("Number of stop-loss orders: " + stopLossOptionList.size());
        if (stopLossOptionList.size() > 0) {
            logger.info("Stop-loss orders: " + stopLossOptionList.toString());
        }

        double totalBtc = mainFrame.getPieChartFrame().getBtcSum();
        if (totalBtc < CHART_SIGNIFICANT_MINIMUM) {
            logger.debug("Total BTC value is too low. Aborting all stop-loss procedures.");
            return;
        }

        // Check if there are any valid stop-loss orders for ALL.
        for (StopLossOption stopLossOption : stopLossOptionList) {
            if (stopLossOption.isSellAll()) {
                boolean valid = false;
                if (stopLossOption.getCondition().equals(StopLossCondition.ABOVE)) {
                    if (totalBtc > stopLossOption.getCancelAt()) {
                        valid = true;
                    }
                } else {
                    if (totalBtc < stopLossOption.getCancelAt()) {
                        valid = true;
                    }
                }
                if (valid) {
                    new Thread(() -> executeStopLoss(new HashMap<>(marketDetailsMap), openMarketOrders, stopLossOption, null)).start();
                    logger.debug("Stop-loss ALL found, other stop-loss operations will be skipped!");
                    return;
                }
            }
        }

        // Check if there are any valid stop-loss orders for single order.
        for (StopLossOption stopLossOption : stopLossOptionList) {
            String marketName = stopLossOption.getMarketName();
            double cancelAt = stopLossOption.getCancelAt();
            if (!stopLossOption.isSellAll() && marketDetailsMap.containsKey(marketName)) {
                boolean valid = false;
                if (stopLossOption.getCondition().equals(StopLossCondition.ABOVE)) {
                    if (marketDetailsMap.get(marketName).getLast() > cancelAt) {
                        valid = true;
                    }
                } else {
                    if (marketDetailsMap.get(marketName).getLast() < cancelAt) {
                        valid = true;
                    }
                }
                if (valid) {
                    executeStopLoss(new HashMap<>(marketDetailsMap), openMarketOrders, stopLossOption, stopLossOption.getMarketName());
                }
            }
        }
    }

    /**
     * Leave singleMarketName as NULL, to execute stop-loss for all markets. Pass any value to execute stop-loss just for one market.
     *
     * @param marketDetailsMap
     * @param openMarketOrders
     * @param stopLossOption
     * @param singleMarketName
     */
    private static void executeStopLoss(Map<String, MarketDetails> marketDetailsMap,
                                        MarketOrderResponse openMarketOrders,
                                        StopLossOption stopLossOption, final String singleMarketName) {
        int count;
        Set<String> marketNamesToSell = new HashSet<>();
        // Cancel all open orders
        for (MarketOrderResponse.Result result : openMarketOrders.getResult()) {
            count = 0;
            String marketName = result.getExchange();
            // Check if is in the single market mode.
            if (singleMarketName != null && !marketName.equalsIgnoreCase(singleMarketName)) {
                continue;
            }
            if ((result.getOrderType().equals("LIMIT_SELL") && stopLossOption.getMode().equals(StopLossMode.SELL)) ||
                    (result.getOrderType().equals("LIMIT_BUY") && stopLossOption.getMode().equals(StopLossMode.BUY)) ||
                    stopLossOption.getMode().equals(StopLossMode.BOTH)) {
                String orderId = result.getOrderUuid();
                // Cancel one. Allow to retry.
                while (count <= RETRY_COUNT) {
                    if (count == RETRY_COUNT) {
                        logger.debug("One of the cancel operations failed! Aborting stop-loss all!");
                        return;
                    }
                    OrderResponse orderResponse = ModelBuilder.buildCancelOrderById(orderId);
                    if (orderResponse.isSuccess()) {
                        logger.debug("Successfully cancelled order with id: " + orderId + " for coin " + marketName);
                        marketNamesToSell.add(marketName);
                        break;
                    } else {
                        count++;
                    }
                }
            }
        }

        // Sell all alt coins. Allow retires.
        for (String marketName : marketNamesToSell) {
            count = 0;
            double totalAmount = marketDetailsMap.get(marketName).getTotalAmount();
            if (totalAmount > BALANCE_MINIMUM) {
                double last = marketDetailsMap.get(marketName).getLast();
                if (singleMarketName != null && !marketName.equalsIgnoreCase(singleMarketName)) {
                    continue;
                }
                while (count <= RETRY_COUNT) {
                    if (count == RETRY_COUNT) {
                        logger.debug("One of the sell operations failed! Aborting stop-loss all!");
                        break;
                    }
                    // TODO
                    // MAY CAUSE PROBLEMS IF ONLY LIMIT_SELL TYPES ARE GOING TO BE SOLD, BUT THERE ARE LIMIT_BUYs REMAINING.
                    // IN SUCH CASE THE totalAmount will be too high!
                    OrderResponse orderResponse = ModelBuilder.buildSellOrder(marketName, totalAmount, last);
                    if (orderResponse.isSuccess()) {
                        try {
                            String marketToRemoveFromList = singleMarketName == null ? "ALL" : marketName;
                            StopLossOptionManager.getInstance().
                                    removeOptionByMarketNameAndMode(marketToRemoveFromList, stopLossOption.getMode());
                        } catch (IOException e) {
                            logger.error(e.getMessage() + "\nFailed to remove stop-loss option");
                            return;
                        } catch (JAXBException e) {
                            logger.error(e.getMessage() + "\nFailed to remove stop-loss option");
                            return;
                        }
                        logger.debug("Successfully placed sell order for " + totalAmount + " units of " + marketName + ", " + last + " each.");
                        break;
                    } else {
                        count++;
                    }
                }
            }
        }
    }

    private static void updatePriceHistoryMap(String marketName, double last) {
        if (!priceHistoryMap.containsKey(marketName)) {
            priceHistoryMap.put(marketName, new LinkedList<>());
        }
        if (priceHistoryMap.get(marketName).size() >= LIST_MAX_SIZE) {
            priceHistoryMap.get(marketName).remove(0);
        }
        priceHistoryMap.get(marketName).add(last);
        //
        double avg = priceHistoryMap.get(marketName).stream().mapToDouble(val -> val).average().getAsDouble();
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
        logger.debug("** Avg price for " + marketName + " is: " + df.format(avg) + ". Size: " + priceHistoryMap.get(marketName).size());

        // Update avg price
        avgValueMap.put(marketName, avg);
    }
}
