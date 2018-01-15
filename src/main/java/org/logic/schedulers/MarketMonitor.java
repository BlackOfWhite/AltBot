package org.logic.schedulers;

import org.apache.log4j.Logger;
import org.logic.models.JSONParser;
import org.logic.models.misc.BalancesSet;
import org.logic.models.responses.*;
import org.logic.requests.MarketRequests;
import org.logic.smtp.MailSender;
import org.logic.transactions.model.stoploss.CancelOption;
import org.logic.transactions.model.stoploss.CancelOptionManager;
import org.logic.utils.MarketNameUtils;
import org.logic.utils.ModelBuilder;
import org.preferences.Params;
import org.preferences.managers.PreferenceManager;
import org.ui.frames.MainFrame;
import org.ui.views.dialog.box.InfoDialog;
import org.ui.views.dialog.box.SingleInstanceDialog;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private MarketMonitor() {
    }

    public static MarketMonitor getInstance() {
        if (instance == null) {
            instance = new MarketMonitor();
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
                    final Map<String, Double> priceMap = createLastPriceMap(marketBalances, openMarketOrders);
                    if (priceMap != null) {
                        mainFrame.getPieChartFrame().setIsConnected(true);
                        updatePieChart(marketBalances, priceMap);
//                        if (mainFrame.getPieChartFrame().getBtcSum() > 0.024) {
//                            sellAll(priceMap, openMarketOrders);
//                            return;
//                        }
                        stopLossOrdersByOrderId(openMarketOrders, priceMap);
                    } else {
                        logger.debug("Some HTTP responses lost, not updating PieChart and stop-loss!");
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

    /**
     * This method will find all orders (candidates) for which stop-loss should be executed.
     * If given order's id is in the collection of stop orders, it will be cancelled and new sell order would be placed.
     * Current price must lower than stop-loss price minus THRESHOLD - useful in case when price drops dramatically.
     *
     * @param openMarketOrders
     * @return
     */
    private static void stopLossOrdersByOrderId(MarketOrderResponse openMarketOrders, Map<String, Double> priceMap) {
//        List<CancelOption> cancelOptionList = CancelOptionManager.getInstance().getOptionList();
//        logger.info("Number of stop-loss orders: " + cancelOptionList.size());
//        if (cancelOptionList.size() > 0) {
//            logger.info("Stop-loss orders: " + cancelOptionList.toString());
//        }
//        for (CancelOption cancelOption : cancelOptionList) {
//            for (MarketOrderResponse.Result result : openMarketOrders.getResult()) {
//                if (cancelOption.getUuid().equalsIgnoreCase(result.getOrderUuid())) {
//                    // find last market value for this currency
//                    double last = priceMap.get(cancelOption.getMarketName());
//                    double cancelBelow = cancelOption.getCancelBelow();
//                    if (cancelBelow >= last && (last >= cancelBelow *
//                            (cancelOption.getThreshold() / 100.0d)) && last >= BALANCE_MINIMUM) {
//                        final String uuid = cancelOption.getUuid();
//                        final double amount = result.getQuantityRemaining();
//                        OrderResponse orderResponse = ModelBuilder.buildCancelOrderById(uuid);
//                        if (orderResponse.isSuccess()) {
//                            logger.debug("Successfully canceled order: " + uuid);
//                            // Place new sell order
//                            double rate = last - STOP_LOSS_SELL_THRESHOLD;
//                            if (rate < BALANCE_MINIMUM) {
//                                logger.error("Failed to place new sell order with too low rate: " + rate + ".");
//                                return;
//                            }
//                            String response = null;
//                            try {
//                                response = MarketRequests.placeOrderSell(result.getExchange(), amount, rate);
//                            } catch (Exception e) {
//                                logger.error("Failed to place new sell order after cancelling order with id: \" + uuid");
//                            }
//                            OrderResponse sellOrderResponse = JSONParser.parseOrderResponse(response);
//                            if (sellOrderResponse.isSuccess()) {
//                                try {
//                                    CancelOptionManager.getInstance().removeOptionByUuid(uuid);
//                                } catch (IOException e) {
//                                    logger.error("Failed to remove cancel option with id: " + uuid);
//                                }
//                                logger.debug("Successfully placed new sell order after cancelling order with id: " + uuid);
//                            }
//                        } else {
//                            logger.error("Failed to cancel order: " + uuid + ". Reason: " + orderResponse.getMessage());
//                        }
//                    }
//                }
//            }
//        }
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
     * @param priceMap
     */
    private static void updatePieChart(final MarketBalancesResponse marketBalances,
                                       final Map<String, Double> priceMap) {
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
                double last = priceMap.get(marketName);
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
     * @param marketOrderResponse
     * @return
     */
    public static Map<String, Double> createLastPriceMap(MarketBalancesResponse
                                                                 marketBalancesResponse, MarketOrderResponse marketOrderResponse) {
        Map<String, Double> map = new HashMap<>();
        // Take only non-zero currencies from market balance.
        for (MarketBalancesResponse.Result result : marketBalancesResponse.getResult()) {
            String currency = result.getCurrency();
            if (result.getBalance() < BALANCE_MINIMUM) {
                continue;
            }
            if (!currency.startsWith("BTC")) {
                if (currency.startsWith("USDT")) {
                    map.put(result.getCurrency() + "-BTC", -1.0);
                } else {
                    map.put("BTC-" + result.getCurrency(), -1.0);
                }
            }
        }
        // Take every currency from orders
        for (MarketOrderResponse.Result result : marketOrderResponse.getResult()) {
            map.put(result.getExchange(), -1.0);
        }
        // Marge and get last price.
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (map.get(entry.getKey()) < 0.0d) {
                MarketSummaryResponse marketSummary = ModelBuilder.buildMarketSummary(entry.getKey());
                try {
                    entry.setValue(marketSummary.getResult().get(0).getLast());
                } catch (NullPointerException ex) {
                    logger.error("Invalid market:" + entry.getKey() + "\n" + ex);
                    return null;
                } catch (Exception ex) {
                    logger.error("Invalid market:" + entry.getKey() + "\n" + ex);
                    return null;
                }
            }
        }
        logger.debug(map.toString());
        return map;
    }

    private static void showDialog(String msg) {
        if (dialog == null || dialog.isClosed()) {
            dialog = new SingleInstanceDialog(msg);
        }
    }

    private static void sellAll(Map<String, Double> lastPriceMap, MarketOrderResponse openMarketOrders) {
        int count = 1;
        boolean cancelFail = false;
        for(MarketOrderResponse.Result result : openMarketOrders.getResult()) {
            count = 0;
            if (cancelFail) {
                return;
            }
            if (result.getOrderType().equals("LIMIT_SELL")) {
                String orderId = result.getOrderUuid();
                while (count <= 4) {
                    if (count == 4) {
                        logger.debug("Cancel operation failed! Aborting 'sellAll'!");
                        cancelFail = true;
                        break;
                    }
                    OrderResponse orderResponse = ModelBuilder.buildCancelOrderById(orderId);
                    if (orderResponse.isSuccess()) {
                        count = 5;
                        logger.debug("Successfully cancelled order with id: " + orderId + " for coin " + result.getExchange());
                    } else {
                        count++;
                    }
                }
            }
        }
    }
}
