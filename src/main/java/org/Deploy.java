package org;

import org.preferences.managers.PersistenceManager;
import org.preferences.managers.PreferenceManager;

public class Deploy {

    public static void main(String[] args) {
        PersistenceManager.clearCancelOptionCollection();
        PreferenceManager.setEmailPassword("", true);
        PreferenceManager.setEmailAddress("");
        PreferenceManager.setApiSecretKey("", true);
        PreferenceManager.setApiKey("", true);
    }
}
