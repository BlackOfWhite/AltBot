package org.ui.views.textfield;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import static org.preferences.Constants.MAX_INPUT_VALUE;
import static org.ui.Constants.DOUBLE_FIELD_INVALID;

public class HintTextField extends JTextField implements FocusListener {

    private final String hint;
    private boolean showingHint;

    public HintTextField(final String hint) {
        super(hint);
        this.hint = hint;
        this.showingHint = true;
        super.addFocusListener(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setText("");
            showingHint = false;
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setText(hint);
            showingHint = true;
        }
    }

    @Override
    public String getText() {
        return showingHint ? "" : super.getText();
    }

    public double getAsDouble() {
        double d = 0;
        try {
            d = Double.parseDouble(getText());
        } catch (Exception ex) {
            return DOUBLE_FIELD_INVALID;
        }
        return d;
    }

    public boolean isValidDouble() {
        try {
            Double.parseDouble(getText());
            if (Double.parseDouble(getText()) > MAX_INPUT_VALUE) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean isValidDoubleOrEmpty() {
        if (getText().trim().length() == 0 || getText().isEmpty()) {
            return true;
        }
        try {
            Double.parseDouble(getText());
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        if (getText() == null || getText().trim().isEmpty()) {
            return true;
        }
        return false;
    }
}
