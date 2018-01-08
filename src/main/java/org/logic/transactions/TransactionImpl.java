package org.logic.transactions;

public interface TransactionImpl {
    boolean isOnlyOnePerMarketAllowed();
    boolean isBuying = false;
    double rate = -1, amount = -1;
    String marketName = null;
}
