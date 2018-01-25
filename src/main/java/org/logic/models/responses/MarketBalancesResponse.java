package org.logic.models.responses;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import static org.preferences.Constants.BALANCE_MINIMUM;

public class MarketBalancesResponse extends Response {

    @Expose
    private ArrayList<Result> result;

    public ArrayList<Result> getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "MarketBalances{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }

    // Just for test purpose
    public void addResult(double balance, double available, String currency) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(new Result(balance, available, currency));
    }

    public class Result {

        @Expose
        double Balance;
        @Expose
        double Available;
        @Expose
        String Currency;

        public Result(double balance, double available, String currency) {
            Balance = balance;
            Available = available;
            Currency = currency;
        }

        public double getBalance() {
            return Balance;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "Balance=" + Balance +
                    ", Available=" + Available +
                    ", Currency='" + Currency + '\'' +
                    '}';
        }

        public double getAvailable() {
            return Available;
        }

        public String getCurrency() {
            return Currency;
        }

        public boolean isEmpty() {
            return Balance < BALANCE_MINIMUM && Available < BALANCE_MINIMUM;
        }
    }
}
