package org.logic.transactions.model.buysell;

import org.logic.transactions.model.OptionImpl;
import org.logic.transactions.model.OptionManagerImpl;
import org.preferences.managers.PersistenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuySellOptionManager extends OptionManagerImpl {

    private static final BuySellOptionManager instance = new BuySellOptionManager();
    private static List<BuySellOption> optionList = Collections.synchronizedList(new ArrayList());

    //private constructor to avoid client applications to use constructor
    private BuySellOptionManager() {
    }

    public BuySellOptionManager getInstance() {
        return instance;
    }

    public List<BuySellOption> getOptionList() {
        return optionList;
    }

    @Override
    public void addOption(final OptionImpl option) throws IOException {
        BuySellOption buySellOption = (BuySellOption) option;
        optionList.add(buySellOption);
        ArrayList<BuySellOption> options = new ArrayList<>();
        for (BuySellOption option1 : optionList) {
            options.add(option1);
        }
        PersistenceManager.saveOptionCollection(options);
    }

    @Override
    public int removeOptionByMarketName(final String uuid) throws IOException {
//        int count = 0;
//        for (Iterator<BuySellOption> iterator = optionList.iterator(); iterator.hasNext();) {
//            int i = 0;
//            BuySellOption option = iterator.next();
//            if (i == ) {
//                // Remove the current element from the iterator and the list.
//                iterator.remove();
//                count++;
//            }
//        }
//        if (count > 0) {
//            ArrayList<BuySellOption> cancelOptions = new ArrayList<>();
//            for (BuySellOption cancelOption1 : optionList) {
//                cancelOptions.add(cancelOption1);
//            }
//            PersistenceManager.saveBuySellOptionCollection(cancelOptions);
//        }
//        return count;
        return -1;
    }

    @Override
    public void loadOptions() throws IOException, ClassNotFoundException {
        ArrayList<BuySellOption> options = PersistenceManager.loadBuySellOptionCollection();
        optionList = Collections.synchronizedList(options);
    }

    @Override
    public void clearOptionCollection() {
        PersistenceManager.clearBuySellOptionCollection();
        optionList.clear();
    }
}
