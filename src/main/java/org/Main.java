package org;

import org.apache.log4j.Logger;
import org.logic.schedulers.MarketMonitor;
import org.logic.schedulers.TransactionScheduler;
import org.logic.transactions.model.stoploss.CancelOptionManager;
import org.ui.frames.MainFrame;

import javax.swing.*;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        setLAFStyle();
        MainFrame mainFrame = new MainFrame();
        logger.debug("MainFrame created.");

        CancelOptionManager.getInstance().reload();
        startSchedulers(mainFrame);
    }

    private static void setLAFStyle() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
        } catch (InstantiationException e) {
            logger.error(e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage());
        } catch (UnsupportedLookAndFeelException e) {
            logger.error(e.getMessage());
        }
    }

    private static void startSchedulers(final MainFrame mainFrame) {
        // Start OrderMonitor
        logger.debug("Starting schedulers.");
        MarketMonitor.getInstance().start(mainFrame);
        TransactionScheduler.getInstance().start();
    }
}
