package org.preferences.managers;

import org.apache.log4j.Logger;
import org.logic.transactions.model.OptionImpl;
import org.logic.transactions.model.buysell.BuySellOption;
import org.logic.transactions.model.stoploss.StopLossOption;

import java.io.*;
import java.util.ArrayList;

public class PersistenceManager {

    private static final String STOP_LOSS_OPTIONS_FILE_NAME = "stopLossOptions.ser";
    private static final String BUY_SELL_OPTIONS_FILE_NAME = "buySellOptions.ser";

    private static Logger logger = Logger.getLogger(PersistenceManager.class);

    public static ArrayList<StopLossOption> loadStopLossOptionCollection() throws IOException, ClassNotFoundException, NullPointerException {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        ArrayList<StopLossOption> arrayList = new ArrayList<>();
        try {
            fis = new FileInputStream(STOP_LOSS_OPTIONS_FILE_NAME);
            ois = new ObjectInputStream(fis);
            arrayList = (ArrayList<StopLossOption>) ois.readObject();
        } catch (NullPointerException npe) {
            logger.info("Loaded cancel option list but it was empty!\n" + npe.getStackTrace().toString());
        } catch (EOFException eofe) {
            logger.info("Loaded cancel option list but it was empty!\n" + eofe.getStackTrace().toString());
        } finally {
            try {
                fis.close();
                ois.close();
            } catch (NullPointerException npe) {
                logger.info("Loaded cancel option list but it was empty!\n" + npe.getStackTrace().toString());
            } catch (EOFException eofe) {
                logger.info("Loaded cancel option list but it was empty!\n" + eofe.getStackTrace().toString());
            }
        }
        return arrayList;
    }

    /**
     * Just for test purposes
     */
    public static void clearStopLossOptionCollection() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(STOP_LOSS_OPTIONS_FILE_NAME);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        writer.print("");
        writer.close();
    }


    /**
     * Buy sell options.
     */
    public static void saveOptionCollection(ArrayList<? extends OptionImpl> options) throws IOException {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        String fileName = null;
        Class type = options.getClass().getComponentType();
        if (type.equals(BuySellOption.class)) {
            fileName = BUY_SELL_OPTIONS_FILE_NAME;
        } else if (type.equals(StopLossOption.class)) {
            fileName = STOP_LOSS_OPTIONS_FILE_NAME;
        }
        try {
            fout = new FileOutputStream(fileName);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(options);
        } finally {
            fout.close();
            oos.close();
        }
    }

    public static ArrayList<BuySellOption> loadBuySellOptionCollection() throws IOException, ClassNotFoundException, NullPointerException {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        ArrayList<BuySellOption> arrayList = new ArrayList<>();
        try {
            fis = new FileInputStream(BUY_SELL_OPTIONS_FILE_NAME);
            ois = new ObjectInputStream(fis);
            arrayList = (ArrayList<BuySellOption>) ois.readObject();
        } catch (NullPointerException npe) {
            logger.info("Loaded buy/sell option list but it was empty!\n" + npe.getStackTrace().toString());
        } catch (EOFException eofe) {
            logger.info("Loaded buy/sell option list but it was empty!\n" + eofe.getStackTrace().toString());
        } finally {
            try {
                fis.close();
                ois.close();
            } catch (NullPointerException npe) {
                logger.info("Loaded buy/sell option list but it was empty!\n" + npe.getStackTrace().toString());
            } catch (EOFException eofe) {
                logger.info("Loaded buy/sell option list but it was empty!\n" + eofe.getStackTrace().toString());
            }
        }
        return arrayList;
    }

    /**
     * Just for test purposes
     */
    public static void clearBuySellOptionCollection() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(BUY_SELL_OPTIONS_FILE_NAME);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        writer.print("");
        writer.close();
    }
}
