package org.logic.models.responses;

import com.google.gson.annotations.Expose;

import static org.preferences.Constants.BALANCE_MINIMUM;

public class MarketBalanceResponse extends Response {

    @Expose
    private Result result;

    public Result getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "MarketBalance{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }

    public class Result {

        @Expose
        double Balance;
        @Expose
        double Available;

        public double getBalance() {
            return Balance;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "Balance=" + Balance +
                    ", Available=" + Available +
                    '}';
        }

        public double getAvailable() {
            return Available;
        }

        public boolean isEmpty() {
            return Balance < BALANCE_MINIMUM && Available < BALANCE_MINIMUM;
        }
    }

//
//    "Balance" : 4.21549076,
//            "Available" : 4.21549076,
}
