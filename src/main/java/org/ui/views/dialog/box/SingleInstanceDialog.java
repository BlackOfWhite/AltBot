package org.ui.views.dialog.box;

import org.ui.frames.util.SingleInstanceFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Used for dialogs that may pop up too often and stop application.
 */
public class SingleInstanceDialog extends SingleInstanceFrame {
    private String message;

    public SingleInstanceDialog(String message) {
        this.message = message;
        init();
    }

    JPanel createPanelToPopDialog(final JFrame parent) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JButton button = new JButton("OK");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.dispose();
            }
        });
        panel.add(button, BorderLayout.SOUTH);
        panel.add(new JLabel("<html>" + message + "</html>"), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    private void init() {
        this.setTitle("Info Dialog");
        this.add(createPanelToPopDialog(this));
        this.setSize(400, 200);
        this.setResizable(false);
        this.setVisible(true);
        centerPosition();
    }

    public String getMessage() {
        return message;
    }

}
