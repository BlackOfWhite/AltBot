package org.logic.transactions.model.stoploss;

import org.apache.log4j.Logger;
import org.logic.transactions.model.OptionImpl;
import org.logic.transactions.model.OptionManagerImpl;
import org.preferences.managers.PersistenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CancelOptionManager implements OptionManagerImpl {

    private static final CancelOptionManager instance = new CancelOptionManager();
    private static List<CancelOption> cancelOptionList = Collections.synchronizedList(new ArrayList());
    private static Logger logger = Logger.getLogger(CancelOptionManager.class);

    //private constructor to avoid client applications to use constructor
    private CancelOptionManager() {
    }

    public static CancelOptionManager getInstance() {
        return instance;
    }

    @Override
    public boolean reload() {
        try {
            loadOptions();
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage() + "\n" + e.getStackTrace().toString());
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage() + "\n" + e.getStackTrace().toString());
        }
        return false;
    }

    public List<CancelOption> getOptionList() {
        return cancelOptionList;
    }

    @Override
    public void addOption(final OptionImpl option) throws IOException {
        CancelOption cancelOption = (CancelOption) option;
        cancelOptionList.add(cancelOption);
        ArrayList<CancelOption> cancelOptions = new ArrayList<>();
        for (CancelOption cancelOption1 : cancelOptionList) {
            cancelOptions.add(cancelOption1);
        }
        PersistenceManager.saveCancelOptionCollection(cancelOptions);
    }


    @Override
    public int removeOptionByUuid(final String uuid) throws IOException {
        int count = 0;
        for (Iterator<CancelOption> iterator = cancelOptionList.iterator(); iterator.hasNext();) {
            CancelOption cancelOption = iterator.next();
            if (cancelOption.getUuid().equals(uuid)) {
                // Remove the current element from the iterator and the list.
                iterator.remove();
                count++;
            }
        }
        if (count > 0) {
            ArrayList<CancelOption> cancelOptions = new ArrayList<>();
            for (CancelOption cancelOption1 : cancelOptionList) {
                cancelOptions.add(cancelOption1);
            }
            PersistenceManager.saveCancelOptionCollection(cancelOptions);
        }
        return count;
    }

    @Override
    public void loadOptions() throws IOException, ClassNotFoundException {
        ArrayList<CancelOption> cancelOptions = PersistenceManager.loadCancelOptionCollection();
        cancelOptionList = Collections.synchronizedList(cancelOptions);
    }

    @Override
    public void clearOptionCollection() {
        PersistenceManager.clearCancelOptionCollection();
        cancelOptionList.clear();
    }
}
