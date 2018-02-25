package org;

import de.javasoft.plaf.synthetica.SyntheticaBlackMoonLookAndFeel;
import org.apache.log4j.Logger;
import org.logic.schedulers.monitors.MarketMonitor;
import org.logic.transactions.model.bots.BotAvgOptionManager;
import org.logic.transactions.model.stoploss.StopLossOptionManager;
import org.ui.frames.MainFrame;

import javax.swing.*;
import java.text.ParseException;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        setLAFStyle();
        MainFrame mainFrame = new MainFrame();
        logger.debug("MainFrame created.");

        StopLossOptionManager.getInstance().reload();
        BotAvgOptionManager.getInstance().reload();
        startSchedulers(mainFrame);
    }

    private static void setLAFStyle() {
        try {
//            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.setLookAndFeel(new SyntheticaBlackMoonLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            logger.error(e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void startSchedulers(final MainFrame mainFrame) {
        // Start OrderMonitor
        logger.debug("Starting schedulers.");
        MarketMonitor.getInstance().start(mainFrame);
//        TransactionScheduler.getInstance().start();
//        DeepBot.getInstance().start();
    }
}
