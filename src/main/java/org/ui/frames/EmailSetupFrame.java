package org.ui.frames;

import org.preferences.managers.PreferenceManager;
import org.ui.frames.util.SingleInstanceFrame;

import javax.swing.*;

public class EmailSetupFrame extends SingleInstanceFrame {

    public EmailSetupFrame(JCheckBoxMenuItem jCheckBoxMenuItem) {
        PreferenceManager.changeEmailNotificationEnabled();
        jCheckBoxMenuItem.setState(PreferenceManager.isEmailNotificationEnabled());
    }

}
