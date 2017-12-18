package org.preferences;

import org.logic.transactions.model.CancelOption;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class PreferenceManager {

    private static final String EMAIL_NOTIFICATION_ENABLED_KEY = "EMAIL_NOTIFICATION_ENABLED_KEY";
    private static final String HIDE_INSIGNIFICANT_ENABLED_KEY = "HIDE_INSIGNIFICANT_ENABLED_KEY";

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
}
