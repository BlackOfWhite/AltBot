package org.logic.models.responses;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class OrderResponse {


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
        return "OrderResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }

    public class Result {
        @Expose
        String uuid;

        @Override
        public String toString() {
            return "Result{" +
                    "uuid=" + uuid + "}";
        }

        public String getUuid() {
            return uuid;
        }
    }

//    {
//        "success" : true,
//            "message" : "",
//            "result" : {
//        "uuid" : "e606d53c-8d70-11e3-94b5-425861b86ab6"
//    }
//    }
}

