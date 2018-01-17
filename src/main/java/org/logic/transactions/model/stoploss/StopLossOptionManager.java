package org.logic.transactions.model.stoploss;

import org.logic.transactions.model.OptionImpl;
import org.logic.transactions.model.OptionManagerImpl;
import org.preferences.managers.PersistenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StopLossOptionManager extends OptionManagerImpl {

    private static final StopLossOptionManager instance = new StopLossOptionManager();
    private static List<StopLossOption> optionList = Collections.synchronizedList(new ArrayList());

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
    public void addOption(final OptionImpl option) throws IOException {
        StopLossOption stopLossOption = (StopLossOption) option;
        optionList.add(stopLossOption);
        ArrayList<StopLossOption> stopLossOptions = new ArrayList<>();
        for (StopLossOption stopLossOption1 : optionList) {
            stopLossOptions.add(stopLossOption1);
        }
        PersistenceManager.saveOptionCollection(stopLossOptions);
    }

    /**
     * Use full market name, e.g. BTC-ETH.
     *
     * @param marketName
     * @return
     * @throws IOException
     */
    @Override
    public int removeOptionByMarketName(final String marketName) throws IOException {
        int count = 0;
        for (Iterator<StopLossOption> iterator = optionList.iterator(); iterator.hasNext(); ) {
            StopLossOption stopLossOption = iterator.next();
            if (stopLossOption.getMarketName().equalsIgnoreCase(marketName)) {
                // Remove the current element from the iterator and the list.
                iterator.remove();
                count++;
            }
        }
        if (count > 0) {
            ArrayList<StopLossOption> stopLossOptions = new ArrayList<>();
            for (StopLossOption stopLossOption1 : optionList) {
                stopLossOptions.add(stopLossOption1);
            }
            PersistenceManager.saveOptionCollection(stopLossOptions);
        }
        return count;
    }

    @Override
    public void loadOptions() throws IOException, ClassNotFoundException {
        ArrayList<StopLossOption> stopLossOptions = PersistenceManager.loadStopLossOptionCollection();
        optionList = Collections.synchronizedList(stopLossOptions);
    }

    @Override
    public void clearOptionCollection() {
        PersistenceManager.clearStopLossOptionCollection();
        optionList.clear();
    }
}
