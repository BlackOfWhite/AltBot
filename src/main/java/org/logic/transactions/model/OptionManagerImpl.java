package org.logic.transactions.model;

import org.logic.transactions.model.buysell.BuySellOption;
import org.logic.transactions.model.stoploss.CancelOption;

import java.io.IOException;
import java.util.List;

public interface OptionManagerImpl<T extends OptionImpl> {

    void loadOptions() throws IOException, ClassNotFoundException;
    boolean reload();
    int removeOptionByUuid(final String uuid) throws IOException;
    void clearOptionCollection();
    void addOption(T option) throws IOException;
}
