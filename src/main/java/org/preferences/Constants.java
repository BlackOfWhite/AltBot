package org.preferences;

public class Constants {

    public static final String MSG_MIN_TRADE_REQUIREMENT_NOT_MET = "MIN_TRADE_REQUIREMENT_NOT_MET";
    public static final String MSG_INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS";
    public static final String MSG_INVALID_MARKET = "INVALID_MARKET";
    public static final String MSG_ZERO_OR_NEGATIVE_NOT_ALLOWED = "ZERO_OR_NEGATIVE_NOT_ALLOWED";
    public static final String MSG_REQUEST_PROCESSING_PROBLEM = "There was a problem processing your request.";
    public static final String MSG_APIKEY_INVALID = "APIKEY_INVALID";

    public static final String MSG_REQUEST_TIMEOUT = "Request timeout.";
    public static final int REQUEST_TIMEOUT_SECONDS = 5;

    public static final double BALANCE_MINIMUM = 0.00000001;
    public static final double CHART_SIGNIFICANT_MINIMUM = 0.000001;
    public static final int MAX_INPUT_VALUE = 1000000;

    public static final String ORDER_TYPE_SELL = "LIMIT_SELL";
    public static final String ORDER_TYPE_BUY = "LIMIT_BUY";

    // Stop-loss. Subtract this value from last market price, to make sure order is executed immediately.
    public static final double STOP_LOSS_SELL_THRESHOLD = 0.0000005;

    // Dialogs
    public static final String DIALOG_FAILED_TO_LOAD_API_KEYS = "Failed to load one or more API keys! Please go to 'API Setup' section in the 'Settings' menu.";
    public static final String DIALOG_INVALID_API_KEYS = "API keys are invalid. Please go to 'API Setup' section in the 'Settings' menu.";


}
