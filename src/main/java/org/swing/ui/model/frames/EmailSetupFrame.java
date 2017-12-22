package org.swing.ui.model.frames;

import org.preferences.PreferenceManager;
import org.swing.ui.model.frames.util.SingleInstanceFrame;

import javax.swing.*;

public class EmailSetupFrame extends SingleInstanceFrame {

    public EmailSetupFrame(JCheckBoxMenuItem jCheckBoxMenuItem) {
        PreferenceManager.changeEmailNotificationEnabled();
        jCheckBoxMenuItem.setState(PreferenceManager.isEmailNotificationEnabled());
    }

}
