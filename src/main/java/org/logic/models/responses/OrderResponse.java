package org.logic.models.responses;

import com.google.gson.annotations.Expose;

import static org.preferences.Constants.MSG_REQUEST_TIMEOUT;

public class OrderResponse extends Response {

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

    public void setResponseTimedOut() {
        this.success = false;
        this.message = MSG_REQUEST_TIMEOUT;
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

