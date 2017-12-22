package org.logic.models.responses;

import com.google.gson.annotations.Expose;
import com.sun.org.apache.xpath.internal.operations.Or;

import java.util.ArrayList;

import static org.preferences.Constants.MSG_REQUEST_TIMEOUT;

public class OrderResponse extends Response{

    @Expose
    private Result result;

    public Result getResult() {
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

    public void setResponseTimedOut() {
        this.success = false;
        this.message = MSG_REQUEST_TIMEOUT;
    }
//    {
//        "success" : true,
//            "message" : "",
//            "result" : {
//        "uuid" : "e606d53c-8d70-11e3-94b5-425861b86ab6"
//    }
//    }
}

