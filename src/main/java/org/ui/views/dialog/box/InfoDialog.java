package org.ui.views.dialog.box;

import javax.swing.*;

public class InfoDialog {

    public InfoDialog(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    public InfoDialog(String message) {
        JOptionPane.showMessageDialog(null, message);
    }


}
