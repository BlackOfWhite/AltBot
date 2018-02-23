package org.logic.models.responses;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import static org.preferences.Constants.ORDER_TYPE_BUY;
import static org.preferences.Constants.ORDER_TYPE_SELL;

public class MarketOrderResponse extends Response {

    @Expose
    private ArrayList<Result> result;

    public ArrayList<Result> getResult() {
        return result;
    }

    // Just for testing purposes
    public void addResult(String exchange) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(new Result(1, 1, "", 1, "", exchange));
    }

    @Override
    public String toString() {
        return "MarketOrder{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }

    public int getSellOrdersCount() {
        int sum = 0;
        for (Result result : result) {
            if (result.OrderType.equals(ORDER_TYPE_SELL)) {
                sum++;
            }
        }
        return sum;
    }

    public int getBuyOrdersCount() {
        int sum = 0;
        for (Result result : result) {
            if (result.OrderType.equals(ORDER_TYPE_BUY)) {
                sum++;
            }
        }
        return sum;
    }

    public class Result {
        @Expose
        double PricePerUnit;
        @Expose
        String OrderType;
        @Expose
        double QuantityRemaining;
        @Expose
        String OrderUuid;
        @Expose
        String Exchange;
        @Expose
        double Price;
        @Expose
        double Limit;

        public Result(double price, double pricePerUnit, String orderType, double quantityRemaining, String orderUuid, String exchange) {
            Price = price;
            PricePerUnit = pricePerUnit;
            OrderType = orderType;
            QuantityRemaining = quantityRemaining;
            OrderUuid = orderUuid;
            Exchange = exchange;
        }

        public String getOrderType() {
            return OrderType;
        }

        public double getQuantityRemaining() {
            return QuantityRemaining;
        }

        public double getLimit() {
            return Limit;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "PricePerUnit=" + PricePerUnit +
                    ", OrderType='" + OrderType + '\'' +
                    ", QuantityRemaining=" + QuantityRemaining +
                    ", OrderUuid='" + OrderUuid + '\'' +
                    ", Exchange='" + Exchange + '\'' +
                    ", Price=" + Price +
                    ", Limit='" + Limit + '\'' +
                    '}';
        }

        public double getPricePerUnit() {
            return PricePerUnit;
        }

        public String getExchange() {
            return Exchange;
        }

        public String getOrderUuid() {
            return OrderUuid;
        }

        public double getPrice() {
            return Price;
        }
    }


    /**
     * "Uuid" : null,
     "OrderUuid" : "09aa5bb6-8232-41aa-9b78-a5a1093e0211",
     "Exchange" : "BTC-LTC",
     "OrderType" : "LIMIT_SELL",
     "Quantity" : 5.00000000,
     "QuantityRemaining" : 5.00000000,
     "Limit" : 2.00000000,
     "CommissionPaid" : 0.00000000,
     "Price" : 0.00000000,
     "PricePerUnit" : null,
     "Opened" : "2014-07-09T03:55:48.77",
     "Closed" : null,
     "CancelInitiated" : false,
     "ImmediateOrCancel" : false,
     "IsConditional" : false,
     "Condition" : null,
     "ConditionTarget" : null
     }, {
     "Uuid" : null,
     "OrderUuid" : "8925d746-bc9f-4684-b1aa-e507467aaa99",
     "Exchange" : "BTC-LTC",
     "OrderType" : "LIMIT_BUY",
     "Quantity" : 100000.00000000,
     "QuantityRemaining" : 100000.00000000,
     "Limit" : 0.00000001,
     "CommissionPaid" : 0.00000000,
     "Price" : 0.00000000,
     "PricePerUnit" : null,
     "Opened" : "2014-07-09T03:55:48.583",
     "Closed" : null,
     "CancelInitiated" : false,
     "ImmediateOrCancel" : false,
     "IsConditional" : false,
     "Condition" : null,
     "ConditionTarget" : null
     */
}
