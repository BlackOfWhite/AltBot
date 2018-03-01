package org.ui.frames.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

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
        centerPosition();
    }

    public boolean isClosed() {
        if (!isClosed) {
            return !isDisplayable();
        }
        return isClosed;
    }

    protected void closeFrame() {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        isClosed = true;
        this.dispose();
    }

    protected void centerPosition() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getWidth() / 2, dim.height / 2 - this.getHeight() / 2);
    }
}
