package org.preferences.managers;

import org.apache.log4j.Logger;
import org.logic.transactions.model.buysell.BuySellOption;
import org.logic.transactions.model.stoploss.CancelOption;

import java.io.*;
import java.util.ArrayList;

public class PersistenceManager {

    private static final String CANCEL_OPTIONS_FILE_NAME = "cancelOptions.ser";
    private static final String BUY_SELL_OPTIONS_FILE_NAME = "buySellOptions.ser";

    private static Logger logger = Logger.getLogger(PersistenceManager.class);

    public static void saveCancelOptionCollection(ArrayList<CancelOption> cancelOptions) throws IOException {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try {
            fout = new FileOutputStream(CANCEL_OPTIONS_FILE_NAME);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(cancelOptions);
        } finally {
            fout.close();
            oos.close();
        }
    }

    public static ArrayList<CancelOption> loadCancelOptionCollection() throws IOException, ClassNotFoundException, NullPointerException {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        ArrayList<CancelOption> arrayList = new ArrayList<>();
        try {
            fis = new FileInputStream(CANCEL_OPTIONS_FILE_NAME);
            ois = new ObjectInputStream(fis);
            arrayList = (ArrayList<CancelOption>) ois.readObject();
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
    public static void clearCancelOptionCollection() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(CANCEL_OPTIONS_FILE_NAME);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        writer.print("");
        writer.close();
    }


    /**
     * Buy sell options.
     */
    public static void saveBuySellOptionCollection(ArrayList<BuySellOption> cancelOptions) throws IOException {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try {
            fout = new FileOutputStream(BUY_SELL_OPTIONS_FILE_NAME);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(cancelOptions);
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
