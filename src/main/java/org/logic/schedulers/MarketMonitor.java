package org.logic.schedulers;

import org.apache.log4j.Logger;
import org.logic.models.JSONParser;
import org.logic.models.misc.BalancesSet;
import org.logic.models.requests.MarketBalances;
import org.logic.models.requests.MarketOrder;
import org.logic.models.requests.MarketSummary;
import org.logic.requests.MarketRequests;
import org.logic.requests.PublicRequests;
import org.logic.transactions.model.CancelOptionCollection;
import org.logic.transactions.model.CancelOption;
import org.logic.utils.MarketNameUtils;
import org.logic.utils.ModelBuilder;
import org.preferences.managers.PreferenceManager;
import org.ui.frames.MainFrame;
import org.ui.views.dialog.box.InfoDialog;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.preferences.Constants.ADDRESSEE;
import static org.preferences.Constants.PASSWORD;
import static org.preferences.Params.BALANCE_MINIMUM;

public class MarketMonitor {

    private static Logger logger = Logger.getLogger(MarketMonitor.class);

    private static MarketMonitor instance;
    private static ScheduledExecutorService ses;
    private static final int SLEEP_TIME = 5;
    public volatile static boolean active = false;

    private static volatile MarketOrder sharedMarketOrders;

    private static int ORDERS_COUNT = -1;
    private static final String FROM = ADDRESSEE;
    private static final String SUBJECT = "Open orders status changed!";

    private static MainFrame mainFrame;

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
                    final MarketOrder openMarketOrders = ModelBuilder.buildAllOpenOrders();
                    sharedMarketOrders = openMarketOrders;
                    final int size = openMarketOrders.getResult().size();
                    final int buyOrders = openMarketOrders.getBuyOrdersCount();
                    updateMainFrameStatus(size, buyOrders, CancelOptionCollection.getCancelList().size());

                    MarketBalances marketBalances = ModelBuilder.buildMarketBalances();
                    updatePieChart(marketBalances);

                    sendNotification(size);
                    cancelOrders(openMarketOrders);
                } catch (Exception e) {
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

    private static boolean isRunning() {
        if (ses.isShutdown() || ses.isTerminated()) {
            return true;
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
                sendEmailNotification(ORDERS_COUNT, openOrderCount);
                logger.debug("Number of open orders changed from " + ORDERS_COUNT + " to " + openOrderCount);
                ORDERS_COUNT = openOrderCount;
            }
        }
    }

    public static void sendEmailNotification(int count1, int count2) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");

        Session session = Session.getDefaultInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(ADDRESSEE, PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(ADDRESSEE));
            message.setSubject(SUBJECT);
            try {
                message.setText("Number of open orders changed from " + count1 + " to " + count2);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            Transport.send(message);
            logger.debug("Mail send to: " + ADDRESSEE);
        } catch (MessagingException e) {
            new InfoDialog(mainFrame, "Failed to authenticate email " + ADDRESSEE);
            throw new RuntimeException(e);
        }
    }

//    public static MarketOrder getOpenMarketOrders() {
//        return openMarketOrders;
//    }

    private static int cancelOrders(MarketOrder openMarketOrders) {
        List<CancelOption> cancelOptionList = CancelOptionCollection.getCancelList();
        for (CancelOption cancelOption : cancelOptionList) {
            for (MarketOrder.Result result : openMarketOrders.getResult()) {
                if (cancelOption.getMarketName().equalsIgnoreCase(result.getExchange())) {
                    String response = null;
                    try {
                        response = PublicRequests.getMarketSummary(cancelOption.getMarketName());
                    } catch (Exception e) {
                        logger.debug("Response is null");
                    }
                    if (response != null) {
                        MarketSummary marketSummary = JSONParser.parseMarketSummary(response);
                        logger.debug(marketSummary);
                        if (cancelOption.getCancelBelow() <= marketSummary.getResult().get(0).getLast()) {
                            try {
                                final String uuid = cancelOption.getUuid();
                                String resp = MarketRequests.cancelOrder(uuid);
                                if (resp != null && resp.isEmpty()) {
                                    logger.debug("Successfully canceled order: " + uuid);
                                }
                            } catch (Exception e) {
                                logger.debug("Error while creating request's URL");
                            }
                        }
                    }
                }
            }
        }
        return cancelOptionList.size();
    }

    private static void updateMainFrameStatus(int totalOrders, int buyOrders, int stopLossCount) {
        mainFrame.updateStatusBar(totalOrders, buyOrders, stopLossCount);
    }

    private static void updatePieChart(final MarketBalances marketBalances) {
        if (!mainFrame.isPieChartVisible()) {
            return;
        }
        Map<String, BalancesSet> map = new HashMap<>();
        for (MarketBalances.Result result : marketBalances.getResult()) {
            if (result.getBalance() < BALANCE_MINIMUM) {
                continue;
            }
            try {
                String marketName = MarketNameUtils.getMarketNameForCurrency(result.getCurrency());
                if (marketName.equals("BTC")) {
                    map.put(result.getCurrency(), new BalancesSet(result.getBalance(), result.getBalance()));
                } else {
                    MarketSummary marketSummary = ModelBuilder.buildMarketSummary(marketName);
                    if (marketSummary != null) {
                        double btc = marketSummary.getResult().get(0).getLast() * result.getBalance();
                        if (marketName.equalsIgnoreCase("USDT-BTC")) {
                            btc = result.getBalance() * (1 / marketSummary.getResult().get(0).getLast());
                        }
                        map.put(result.getCurrency(), new BalancesSet(result.getBalance(), btc));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mainFrame.updatePieChartFrame(map);
    }

    public static MarketOrder getOpenMarketOrders() {
        return sharedMarketOrders;
    }
}
