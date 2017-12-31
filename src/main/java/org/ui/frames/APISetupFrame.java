package org.ui.frames;

import org.apache.log4j.Logger;
import org.logic.schedulers.MarketMonitor;
import org.preferences.managers.PreferenceManager;
import org.ui.Constants;
import org.ui.frames.util.SingleInstanceFrame;
import org.ui.views.dialog.box.InfoDialog;
import org.ui.views.textfield.HintTextField;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class APISetupFrame extends SingleInstanceFrame {

    private JLabel labelAPIKey, labelAPISecret;
    private HintTextField jtfAPIKey, jtfAPISecret;
    private JButton jbSubmit;
    private Logger logger = Logger.getLogger(APISetupFrame.class);

    public APISetupFrame() {
        init();
    }

    private void init() {
        setTitle("API Setup");

        Container cp = getContentPane();
        JPanel pMain = new JPanel();
        pMain.setBorder(new TitledBorder(new EtchedBorder()));
        pMain.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        pMain.setLayout(new GridLayout(0, 2));

        labelAPIKey = new JLabel("API key:");
        jtfAPIKey = new HintTextField("d80edf6142ca43e6abcdef6b3980b596");
        pMain.add(labelAPIKey);
        pMain.add(jtfAPIKey);

        labelAPISecret = new JLabel("API secret key:");
        jtfAPISecret = new HintTextField("bd4d657675754795a4029df9350f5d2c");
        pMain.add(labelAPISecret);
        pMain.add(jtfAPISecret);
        cp.add(pMain, BorderLayout.NORTH);

        jbSubmit = new JButton("Submit");
        jbSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submit();
            }
        });
        cp.add(jbSubmit);

        setSize(Constants.EMAIL_SETUP_FRAME_WIDTH, Constants.EMAIL_SETUP_FRAME_HEIGHT);
        setVisible(true);
        logger.debug("APISetupFrame initialized");
    }

    private void submit() {
        String apiKey = jtfAPIKey.getText();
        String apiSecret = jtfAPISecret.getText();
        logger.debug(apiKey + " " + apiSecret);
        if (apiKey != null && !apiKey.isEmpty() && apiSecret != null && !apiSecret.isEmpty()) {
            PreferenceManager.setApiKey(apiKey, true);
            PreferenceManager.setApiSecretKey(apiSecret, true);
            MarketMonitor.COUNTER = -1;
        } else {
            new InfoDialog("Fields cannot be empty!");
        }
    }
}

