package org.logic.transactions;

import org.logic.transactions.model.CancelOption;
import org.preferences.PersistenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CancelOptionCollection {

    private static List<CancelOption> cancelOptionList = Collections.synchronizedList(new ArrayList());

    private static final CancelOptionCollection instance = new CancelOptionCollection();

    //private constructor to avoid client applications to use constructor
    private CancelOptionCollection() {
    }

    public static CancelOptionCollection getInstance() {
        // FETCH data from preferences
        if (cancelOptionList.isEmpty()) {
            loadCancelOptions();
        }
        return instance;
    }

    public static List<CancelOption> getCancelList() {
        return cancelOptionList;
    }

    public static boolean addCancelOption(final CancelOption cancelOption) {
        for (CancelOption cancelOption1 : cancelOptionList) {
            if (cancelOption.getMarketName().equalsIgnoreCase(cancelOption1.getMarketName())) {
                return false;
            }
        }
        cancelOptionList.add(cancelOption);
        ArrayList<CancelOption> cancelOptions = new ArrayList<>();
        for (CancelOption cancelOption1 : cancelOptionList) {
            cancelOptions.add(cancelOption1);
        }
        PersistenceManager.saveCancelOptionCollection(cancelOptions);
        return true;
    }

    private static void loadCancelOptions() {
        ArrayList<CancelOption> cancelOptions = PersistenceManager.loadCancelOptionCollection();
        cancelOptionList = Collections.synchronizedList(cancelOptions);
    }
}
