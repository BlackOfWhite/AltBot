package org.logic.transactions.model.buysell;

import org.logic.transactions.model.OptionImpl;
import org.logic.transactions.model.OptionManagerImpl;
import org.logic.transactions.model.stoploss.modes.StopLossMode;
import org.preferences.managers.PersistenceManager;

import javax.xml.bind.JAXBException;
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
    public void addOption(final OptionImpl option) throws IOException, JAXBException {
        BuySellOption buySellOption = (BuySellOption) option;
        optionList.add(buySellOption);
        ArrayList<BuySellOption> options = new ArrayList<>();
        for (BuySellOption option1 : optionList) {
            options.add(option1);
        }
        PersistenceManager.saveBuySellOptionCollection(options);
    }


    @Override
    public int removeOptionByMarketNameAndMode(String marketName, StopLossMode mode) throws IOException, JAXBException {
        return 0;
    }

    @Override
    public void loadOptions() throws IOException, ClassNotFoundException, JAXBException {
        ArrayList<BuySellOption> options = PersistenceManager.loadBuySellOptionCollection();
        optionList = Collections.synchronizedList(options);
    }

    @Override
    public void clearOptionCollection() {
        PersistenceManager.clearBuySellOptionCollection();
        optionList.clear();
    }
}
