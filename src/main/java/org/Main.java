package org;

import org.apache.log4j.Logger;
import org.logic.schedulers.MarketMonitor;
import org.swing.ui.model.frames.MainFrame;

import javax.swing.*;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        setLAFStyle();
        MainFrame mainFrame = new MainFrame();
        logger.debug("MainFrame created.");

        startSchedulers(mainFrame);
    }

    private static void setLAFStyle() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private static void startSchedulers(final MainFrame mainFrame) {
        // Start OrderMonitor
        logger.debug("Starting schedulers.");
        MarketMonitor.getInstance().start(mainFrame);
    }
}
