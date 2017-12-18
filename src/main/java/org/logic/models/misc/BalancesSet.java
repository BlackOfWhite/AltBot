package org.logic.models.misc;

public class BalancesSet {
        private double amount;
        private double btc;
        
        public BalancesSet(double amount, double btc) {
            this.amount = amount;
            this.btc = btc;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public double getBtc() {
            return btc;
        }
    }