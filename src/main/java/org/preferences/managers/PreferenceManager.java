package org.preferences.managers;

import org.logic.encryption.Base64Crypto;

import java.util.prefs.Preferences;

public class PreferenceManager {

    private static final String EMAIL_NOTIFICATION_ENABLED_KEY = "EMAIL_NOTIFICATION_ENABLED_KEY";
    private static final String HIDE_INSIGNIFICANT_ENABLED_KEY = "HIDE_INSIGNIFICANT_ENABLED_KEY";
    private static final String EMAIL_ADDRESS_KEY = "EMAIL_ADDRESS_KEY";
    private static final String EMAIL_ADDRESS_PASSWORD_KEY = "EMAIL_ADDRESS_PASSWORD_KEY";
    private static final String API_KEY = "API_KEY";
    private static final String API_SECRET_KEY = "API_SECRET_KEY";

    public static boolean isEmailNotificationEnabled() {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        return prefs.getBoolean(EMAIL_NOTIFICATION_ENABLED_KEY, true);
    }

    public static void changeEmailNotificationEnabled() {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        boolean value = prefs.getBoolean(EMAIL_NOTIFICATION_ENABLED_KEY, true);
        prefs.putBoolean(EMAIL_NOTIFICATION_ENABLED_KEY, !value);
    }

    public static boolean isHideInsignificantEnabled() {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        return prefs.getBoolean(HIDE_INSIGNIFICANT_ENABLED_KEY, true);
    }

    public static void changeHideInsignificantEnabled() {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        boolean value = prefs.getBoolean(HIDE_INSIGNIFICANT_ENABLED_KEY, false);
        prefs.putBoolean(HIDE_INSIGNIFICANT_ENABLED_KEY, !value);
    }

    public static String getEmailAddress() {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        return prefs.get(EMAIL_ADDRESS_KEY, "");
    }

    public static void setEmailAddress(String email) {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        prefs.put(EMAIL_ADDRESS_KEY, email);
    }

    public static String getEmailPassword(boolean decrypt) {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        String s = prefs.get(EMAIL_ADDRESS_PASSWORD_KEY, "");
        if (decrypt) {
            return Base64Crypto.decode(s);
        }
        return s;
    }

    public static void setEmailPassword(String password, boolean encrypt) {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        if (encrypt) {
            prefs.put(EMAIL_ADDRESS_PASSWORD_KEY, Base64Crypto.encode(password));
        } else {
            prefs.put(EMAIL_ADDRESS_PASSWORD_KEY, password);
        }
    }

    public static String getApiKey(boolean decrypt) {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        String key = prefs.get(API_KEY, "");
        if (decrypt) {
            return Base64Crypto.decode(key).toString();
        }
        return key;
    }

    public static void setApiKey(String apiKey, boolean encrypt) {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        if (encrypt) {
            prefs.put(API_KEY, Base64Crypto.encode(apiKey));
        } else {
            prefs.put(API_KEY, apiKey);
        }
    }

    public static String getApiSecretKey(boolean decrypt) {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        String key = prefs.get(API_SECRET_KEY, "");
        if (decrypt) {
            return Base64Crypto.decode(key).toString();
        }
        return key;
    }

    public static void setApiSecretKey(String apiSecretKey, boolean encrypt) {
        Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);
        if (encrypt) {
            prefs.put(API_SECRET_KEY, Base64Crypto.encode(apiSecretKey));
        } else {
            prefs.put(API_SECRET_KEY, apiSecretKey);
        }
    }

    public static String getApiKeyObfucate() {
        String apiKey = getApiKey(true);
        if (apiKey.length() <= 8) {
            return apiKey;
        }
        return obfMask(apiKey);
    }

    public static String getApiKeySecretObfucate() {
        String apiKey = getApiSecretKey(true);
        if (apiKey.length() <= 8) {
            return apiKey;
        }
        return obfMask(apiKey);
    }

    private static String obfMask(String s) {
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i < 4 || ((i + 4) >= s.length())) {
                masked.append(c);
            } else {
                masked.append("*");
            }
        }
        return masked.toString();
    }
}
