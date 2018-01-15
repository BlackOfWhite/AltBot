package org.ui.frames;

import org.apache.log4j.Logger;
import org.logic.transactions.model.stoploss.StopLossCondition;
import org.logic.validators.PatternValidator;
import org.ui.Constants;
import org.ui.frames.util.SingleInstanceFrame;
import org.ui.views.textfield.HintTextField;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class StopLossFrame extends SingleInstanceFrame {

    private JLabel labelMarketName, labelRate;
    private HintTextField jtfMarketName, jtfRate;
    private JButton jbSubmit;
    private JComboBox<String> jComboBoxMode;
    private String[] ARR_MODES = Arrays.toString(StopLossCondition.values()).replaceAll("^.|.$", "").split(", ");

    private Logger logger = Logger.getLogger(StopLossFrame.class);

    public StopLossFrame() {
        jComboBoxMode = new JComboBox<>(ARR_MODES);
        init();
    }

    private void init() {
        setTitle("Classic Transaction Creator");

        Container cp = getContentPane();
        JPanel pMain = new JPanel();
        pMain.setBorder(new TitledBorder(new EtchedBorder()));
        pMain.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        pMain.setLayout(new GridLayout(0, 2));
        pMain.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        labelMarketName = new JLabel("Market name:");
        jtfMarketName = new HintTextField("BTC-ETH");
        pMain.add(labelMarketName);
        pMain.add(jtfMarketName);

        labelRate = new JLabel("Rate:");
        jtfRate = new HintTextField("0.0");
        pMain.add(labelRate);
        pMain.add(jtfRate);

        jbSubmit = new JButton("Submit");
        jbSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submit();
            }
        });

        // Bottom panel
        JPanel pBottom = new JPanel();
        pBottom.setBorder(new TitledBorder(new EtchedBorder()));
        pBottom.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        pBottom.setLayout(new GridLayout(0, 1));
        pBottom.add(jbSubmit);

        jComboBoxMode = new JComboBox<>(ARR_MODES);
        pBottom.add(jComboBoxMode);

        cp.add(pMain, BorderLayout.CENTER);
        cp.add(pBottom, BorderLayout.SOUTH);

        setSize(Constants.FRAME_WIDTH, Constants.CLASSIC_TRANSACTION_FRAME_HEIGHT);
        setVisible(true);

        logger.debug("StopLossFrame initialized");
    }

    private void submit() {
        String marketName = jtfMarketName.getText();
        String condition = jComboBoxMode.getSelectedItem().toString();
        if (jtfRate.isValidDoubleOrEmpty() && new PatternValidator().isMarketNameValid(marketName)) {
            double rate = jtfRate.getAsDouble();
            if (rate > 0.0d) {
//               CancelOption cancelOption = new CancelOption(marketName, rate, condition);
//               CancelOptionManager.getInstance().addOption(cancelOption);
//               message += " New stop-loss option added for order: " + uuid + " (if drops below " + cancelAt + ").";
            } else {
                logger.debug("Value is too low.");
            }
        } else {
            logger.debug("Market name is invalid.");
        }
    }
}
