package org.swing.ui.model.frames.util;

import javax.swing.*;

public abstract class SingleInstanceFrame extends JFrame {

    private boolean isClosed = true;

    public SingleInstanceFrame() {
        isClosed = false;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                isClosed = true;
            }
        });
    }

    public boolean isClosed() {
        return isClosed;
    }

}
