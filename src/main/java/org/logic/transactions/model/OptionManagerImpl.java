package org.logic.transactions.model;

import org.apache.log4j.Logger;
import org.logic.transactions.model.buysell.BuySellOptionManager;

import java.io.IOException;

public abstract class OptionManagerImpl<T extends OptionImpl> {

    private static Logger logger = Logger.getLogger(BuySellOptionManager.class);

    protected abstract void loadOptions() throws IOException, ClassNotFoundException;
    protected abstract int removeOptionByMarketName(final String marketName) throws IOException;
    protected abstract void clearOptionCollection();
    protected abstract void addOption(T option) throws IOException;
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
}
