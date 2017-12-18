package org.logic.models.requests;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import static org.logic.Params.BALANCE_MINIMUM;

public class MarketBalances {

    @Expose
    private boolean success;
    @Expose
    private String message;
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

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public class Result {

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

        @Expose
        double Balance;
        @Expose
        double Available;
        @Expose
        String Currency;

        public String getCurrency() {
            return Currency;
        }

        public boolean isEmpty() {
            return Balance < BALANCE_MINIMUM && Available < BALANCE_MINIMUM;
        }
    }

//    public void calculateTotalBalances() {
//        balanceBTC = 0.0d;
//        balanceUSD = 0.0d;
//        for (Result result : result) {
//            if (result.getBalance() > BALANCE_MINIMUM) {
//                balanceBTC += result.getBalance();
////                if (basecurrency is btc)
//            }
//        }
//    }
//
//    public double getBalanceBTC() {
//        return balanceBTC;
//    }
//
//    public double getBalanceUSD() {
//        return balanceUSD;
//    }
//
//    "Balance" : 4.21549076,
//            "Available" : 4.21549076,
}
