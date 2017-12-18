package org.logic.models.requests;

import com.google.gson.annotations.Expose;

import static org.logic.Params.BALANCE_MINIMUM;

public class MarketBalance {

    @Expose
    private boolean success;
    @Expose
    private String message;
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
                    '}';
        }

        public double getAvailable() {
            return Available;
        }

        @Expose
        double Balance;
        @Expose
        double Available;

        public boolean isEmpty() {
            return Balance < BALANCE_MINIMUM && Available < BALANCE_MINIMUM;
        }
    }

//
//    "Balance" : 4.21549076,
//            "Available" : 4.21549076,
}
