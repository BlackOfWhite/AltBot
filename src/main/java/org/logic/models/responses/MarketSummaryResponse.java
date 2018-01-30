package org.logic.models.responses;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class MarketSummaryResponse extends Response {

    @Override
    public String toString() {
        return "MarketSummary{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }

    @Expose
    private ArrayList<Result> result;

    public ArrayList<Result> getResult() {
        return result;
    }

    public class Result {
        @Override
        public String toString() {
            return "Result{" +
                    "High=" + High +
                    ", Low=" + Low +
                    ", Ask=" + Ask +
                    ", Bid=" + Bid +
                    ", Last=" + Last +
                    ", Volume=" + Volume +
                    '}';
        }

        @Expose
        double High;
        @Expose
        double Last;
        @Expose
        double Volume;

        public double getLast() {
            return Last;
        }

        public double getHigh() {
            return High;
        }

        public double getLow() {
            return Low;
        }

        public double getAsk() {
            return Ask;
        }

        public double getBid() {
            return Bid;
        }

        public double getVolume() {
            return Volume;
        }

        @Expose
        double Low;
        @Expose
        double Ask; // willing to sell;
        @Expose
        double Bid; // po tyle chca kupic
    }

    //    "success" : true,
//            "message" : "",
//            "result" : [{
//        "MarketName" : "BTC-LTC",
//                "High" : 0.01350000,
//                "Low" : 0.01200000,
//                "Volume" : 3833.97619253,
//                "Last" : 0.01349998,
//                "BaseVolume" : 47.03987026,
//                "TimeStamp" : "2014-07-09T07:22:16.72",
//                "Bid" : 0.01271001,
//                "Ask" : 0.01291100,
//                "OpenBuyOrders" : 45,
//                "OpenSellOrders" : 45,
//                "PrevDay" : 0.01229501,
//                "Created" : "2014-02-13T00:00:00",
//                "DisplayMarketName" : null
//    }
//    ]
}
