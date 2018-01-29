package org.logic.models.responses;

import com.google.gson.annotations.Expose;

import static org.preferences.Constants.*;

public abstract class Response {

    @Expose
    protected boolean success;
    @Expose
    protected String message;

    public String getMessage() {
        switch (message) {
            case MSG_MIN_TRADE_REQUIREMENT_NOT_MET:
                return "Minimum trade requirement not met. Minimum order size is 0.00050000.";
            case MSG_INSUFFICIENT_FUNDS:
                return "Insufficient funds.";
            case MSG_INVALID_MARKET:
                return "Invalid market name.";
            case MSG_ZERO_OR_NEGATIVE_NOT_ALLOWED:
                return "Zero or negative value not allowed.";
            case MSG_REQUEST_TIMEOUT:
                return "Response timeout. Please try again.";
            case MSG_APIKEY_INVALID:
                return DIALOG_FAILED_TO_LOAD_API_KEYS;
        }
        if (message.startsWith(MSG_REQUEST_PROCESSING_PROBLEM)) {
            return "There was a problem processing your request.";
        }
        return "Unknown response";
    }

    protected String getPlainMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
