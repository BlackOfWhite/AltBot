package org.logic.transactions.model.stoploss;

import org.logic.exceptions.EntryExistsException;
import org.logic.transactions.model.OptionImpl;
import org.logic.transactions.model.OptionManagerImpl;
import org.logic.transactions.model.stoploss.modes.StopLossMode;
import org.preferences.managers.PersistenceManager;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class StopLossOptionManager extends OptionManagerImpl {

    private static final StopLossOptionManager instance = new StopLossOptionManager();
    private static List<StopLossOption> optionList = new CopyOnWriteArrayList();

    //private constructor to avoid client applications to use constructor
    private StopLossOptionManager() {
    }

    public static StopLossOptionManager getInstance() {
        return instance;
    }

    public List<StopLossOption> getOptionList() {
        return optionList;
    }

    @Override
    public void addOption(final OptionImpl option) throws IOException, EntryExistsException, JAXBException {
        StopLossOption stopLossOption = (StopLossOption) option;
        if (optionList.contains(option)) {
            throw new EntryExistsException(stopLossOption.getCondition().toString() + " option for market " +
                    stopLossOption.getMarketName() + " already exists.");
        }
        optionList.add(stopLossOption);
        ArrayList<StopLossOption> stopLossOptions = new ArrayList<>();
        for (StopLossOption stopLossOption1 : optionList) {
            stopLossOptions.add(stopLossOption1);
        }
        PersistenceManager.saveStopLossOptionCollection(stopLossOptions);
    }

    /**
     * Use full market name, e.g. BTC-ETH.
     * Only unique sets of marketName and mode! This is very important. Do not allow duplicates!
     *
     * @param marketName
     * @return
     * @throws IOException
     */
    @Override
    public synchronized int removeOptionByMarketNameAndMode(final String marketName, final StopLossMode mode) throws IOException, JAXBException {
        int index = -1;
        int count = 0;
        ArrayList<StopLossOption> copy = new ArrayList<>(optionList);
        Iterator<StopLossOption> iterator = copy.iterator();
        while (iterator.hasNext()) {
            index++;
            StopLossOption stopLossOption = iterator.next();
            if (stopLossOption.getMarketName().equalsIgnoreCase(marketName) && stopLossOption.getMode().equals(mode)) {
                iterator.remove();
                count++;
                break;
            }
        }
        if (count > 0 && index > -1) {
            PersistenceManager.saveStopLossOptionCollection(copy);
            optionList.remove(index);
        }
        return count;
    }

    @Override
    public void loadOptions() throws IOException, ClassNotFoundException, JAXBException {
        ArrayList<StopLossOption> stopLossOptions = PersistenceManager.loadStopLossOptionCollection();
        optionList = new CopyOnWriteArrayList<>(stopLossOptions);
    }

    @Override
    public void clearOptionCollection() {
        PersistenceManager.clearStopLossOptionCollection();
        optionList.clear();
    }
}
