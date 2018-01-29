package org.logic.models.responses.v2;

import com.google.gson.annotations.Expose;
import org.logic.models.responses.Response;

import java.util.ArrayList;
import java.util.LinkedList;

public class MarketTicksResponse extends Response {

    @Expose
    private LinkedList<Result> result;

    public LinkedList<Result> getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "MarketTicksResponse{" +
                "result=" + result +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }

    public class Result {

        @Expose
        double BV; // base volume
        @Expose
        double C; // close
        @Expose
        double H; // high
        @Expose
        double L; // low
        @Expose
        double O; // open
        @Expose
        String T; // timestamp
        @Expose
        double V; // 24h vol

        public double getBV() {
            return BV;
        }

        public double getC() {
            return C;
        }

        public double getH() {
            return H;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "BV=" + BV +
                    ", C=" + C +
                    ", H=" + H +
                    ", L=" + L +
                    ", O=" + O +
                    ", T=" + T +
                    ", V=" + V +
                    '}';
        }

        public double getL() {
            return L;
        }

        public double getO() {
            return O;
        }

        public String getT() {
            return T;
        }

        public double getV() {
            return V;
        }
    }
}

/**
 * URL
 * <p>
 * https://bittrex.com/Api/v2.0/pub/market/GetTicks
 * <p>
 * METHOD
 * <p>
 * GET
 * <p>
 * PARAMS
 * <p>
 * marketName:string, tickInterval:string, _:int
 * <p>
 * EXAMPLE
 * <p>
 * https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName=BTC-CVC&tickInterval=thirtyMin&_=1500915289433
 * <p>
 * COMMENT
 * <p>
 * Probably _ is a timestamp. tickInterval must be in [“oneMin”, “fiveMin”, “thirtyMin”, “hour”, “day”].
 * <p>
 * RESPONSE :
 * <p>
 * {
 * success : true,
 * message : "",
 * result : [ // Array of candle objects.
 * {
 * BV: 13.14752793,          // base volume
 * C: 0.000121,              // close
 * H: 0.00182154,            // high
 * L: 0.0001009,             // low
 * O: 0.00182154,            // open
 * T: "2017-07-16T23:00:00", // timestamp
 * V: 68949.3719684          // 24h volume
 * },
 * ...
 * { ... }]
 * }
 **/