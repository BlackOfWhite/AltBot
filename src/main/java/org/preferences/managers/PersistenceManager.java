package org.preferences.managers;

import org.apache.log4j.Logger;
import org.logic.transactions.model.buysell.BotAvgOption;
import org.logic.transactions.model.buysell.BotAvgOptions;
import org.logic.transactions.model.stoploss.StopLossOption;
import org.logic.transactions.model.stoploss.StopLossOptions;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;

public class PersistenceManager {

    private static final String STOP_LOSS_OPTIONS_FILE_NAME = "stopLossOptions.xml";
    private static final String BOT_AVG_OPTIONS_FILE_NAME = "botAvgOptions.xml";

    private static Logger logger = Logger.getLogger(PersistenceManager.class);

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

    public static void saveStopLossOptionCollection(ArrayList<StopLossOption> options) throws IOException, JAXBException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(STOP_LOSS_OPTIONS_FILE_NAME));
        JAXBContext context = JAXBContext.newInstance(StopLossOptions.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(new StopLossOptions(options), writer);
        writer.close();
    }

    public static void saveBotAvgOptionCollection(ArrayList<BotAvgOption> options) throws IOException, JAXBException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(BOT_AVG_OPTIONS_FILE_NAME));
        JAXBContext context = JAXBContext.newInstance(BotAvgOption.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(new BotAvgOptions(options), writer);
        writer.close();
    }

    public static ArrayList<BotAvgOption> loadBotAvgOptionCollection() throws IOException, ClassNotFoundException, NullPointerException, JAXBException {
        JAXBContext context = JAXBContext.newInstance(BotAvgOptions.class);
        Unmarshaller um = context.createUnmarshaller();
        BotAvgOptions collection = (BotAvgOptions) um.unmarshal(new File(BOT_AVG_OPTIONS_FILE_NAME));
        return collection.getCollection();
    }

    public static ArrayList<StopLossOption> loadStopLossOptionCollection() throws IOException, ClassNotFoundException, NullPointerException, JAXBException {
        JAXBContext context = JAXBContext.newInstance(StopLossOptions.class);
        Unmarshaller um = context.createUnmarshaller();
        StopLossOptions collection = (StopLossOptions) um.unmarshal(new File(STOP_LOSS_OPTIONS_FILE_NAME));
        return collection.getCollection();
    }

    /**
     * Just for test purposes
     */
    public static void clearBotAvgOptionCollection() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(BOT_AVG_OPTIONS_FILE_NAME);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        writer.print("");
        writer.close();
    }
}
