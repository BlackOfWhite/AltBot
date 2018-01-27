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

public class BotAvgOptionManager extends OptionManagerImpl {

    private static final BotAvgOptionManager instance = new BotAvgOptionManager();
    private static List<BotAvgOption> optionList = Collections.synchronizedList(new ArrayList());

    //private constructor to avoid client applications to use constructor
    private BotAvgOptionManager() {
    }

    public static BotAvgOptionManager getInstance() {
        return instance;
    }

    public List<BotAvgOption> getOptionList() {
        return optionList;
    }

    @Override
    public void addOption(final OptionImpl option) throws IOException, JAXBException {
        BotAvgOption buySellOption = (BotAvgOption) option;
        optionList.add(buySellOption);
        ArrayList<BotAvgOption> options = new ArrayList<>();
        for (BotAvgOption option1 : optionList) {
            options.add(option1);
        }
        PersistenceManager.saveBotAvgOptionCollection(options);
    }


    @Override
    public int removeOptionByMarketNameAndMode(String marketName, StopLossMode mode) throws IOException, JAXBException {
        return 0;
    }

    @Override
    public void loadOptions() throws IOException, ClassNotFoundException, JAXBException {
        ArrayList<BotAvgOption> options = PersistenceManager.loadBotAvgOptionCollection();
        optionList = Collections.synchronizedList(options);
    }

    @Override
    public void clearOptionCollection() {
        PersistenceManager.clearBotAvgOptionCollection();
        optionList.clear();
    }
}
