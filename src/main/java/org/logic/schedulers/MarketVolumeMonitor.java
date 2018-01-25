package org.logic.schedulers;

import org.apache.log4j.Logger;
import org.logic.models.responses.MarketOrderResponse;
import org.logic.utils.ModelBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MarketVolumeMonitor {

    private static final int TIME = 10;
    public volatile static boolean active = false;
    private static Logger logger = Logger.getLogger(MarketVolumeMonitor.class);
    private static MarketVolumeMonitor instance;
    private static ScheduledExecutorService ses;

    private MarketVolumeMonitor() {
    }

    public static MarketVolumeMonitor getInstance() {
        if (instance == null) {
            instance = new MarketVolumeMonitor();
            ses = Executors.newScheduledThreadPool(5);
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
        ses.scheduleAtFixedRate(() -> {
            logger.debug("\nNew run..");
            try {
                // Check if there are open orders for this coin:
                MarketOrderResponse marketOrderResponse = ModelBuilder.buildAllOpenOrders();
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
}
