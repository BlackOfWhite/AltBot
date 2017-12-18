package org.swing.ui.model.frames;

import org.apache.log4j.Logger;
import org.logic.transactions.ClassicTransaction;
import org.logic.validators.NamePatternValidator;
import org.logic.validators.TransactionValidator;
import org.swing.Constants;
import org.swing.ui.model.HintTextField;
import org.swing.ui.model.views.dialog.box.InfoDialog;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClassicTransactionFrame extends SingleInstanceFrame {

    private JFrame parent;
    private JComboBox jComboBoxMode;
    private JLabel labelMarketName, labelAmount, labelRate, labelCancelAt;
    private HintTextField jtfMarketName, jtfAmount, jtfRate, jtfCancelAt;

    private JButton jbCreate;

    private Logger logger = Logger.getLogger(ClassicTransactionFrame.class);

    private final static String[] ARR_MODES = {"Buy", "Sell"};

    public ClassicTransactionFrame(JFrame parent) {
        this.parent = parent;
        init();
    }

    private void init() {
        setTitle("Classic Transaction Creator");

        Container cp = getContentPane();
        JPanel pMain = new JPanel();
        pMain.setBorder(new TitledBorder(new EtchedBorder()));
        pMain.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        pMain.setLayout(new GridLayout(0, 2));

        jComboBoxMode = new JComboBox(ARR_MODES);

        labelMarketName = new JLabel("Market name:");
        jtfMarketName = new HintTextField("BTC-ETH");
        pMain.add(labelMarketName);
        pMain.add(jtfMarketName);

        labelAmount = new JLabel("Amount:");
        jtfAmount = new HintTextField("0.0");
        pMain.add(labelAmount);
        pMain.add(jtfAmount);

        labelRate = new JLabel("Rate:");
        jtfRate = new HintTextField("0.0");
        pMain.add(labelRate);
        pMain.add(jtfRate);

        labelCancelAt = new JLabel("Stop loss:");
        jtfCancelAt = new HintTextField("Leave empty to do not enable stop loss");
        pMain.add(labelCancelAt);
        pMain.add(jtfCancelAt);

        jbCreate = new JButton("Create transaction");
        jbCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateInput();
            }
        });

        // Bottom panel
        JPanel pBottom = new JPanel();
        pBottom.setBorder(new TitledBorder(new EtchedBorder()));
        pBottom.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        pBottom.setLayout(new GridLayout(0, 1));
        pBottom.add(jbCreate);

        cp.add(jComboBoxMode, BorderLayout.NORTH);
        cp.add(pMain, BorderLayout.CENTER);
        cp.add(pBottom, BorderLayout.SOUTH);

        setSize(Constants.FRAME_WIDTH, Constants.CLASSIC_TRANSACTION_FRAME_HEIGHT);
        setVisible(true);

        logger.debug("ClassicTransactionFrame initialized");
    }

    private boolean validateInput() {
        boolean success = false;
        String message = "";
        if (jtfRate.isValidDouble() && jtfAmount.isValidDouble() && jtfCancelAt.isValidDoubleOrEmpty()) {
            final double rate = jtfRate.getAsDouble();
            final double amount = jtfAmount.getAsDouble();
            // can be omitted
            final double cancelAt = jtfCancelAt.getAsDouble();
            if (!jtfMarketName.isEmpty()) {
                String marketName = jtfMarketName.getText();
                marketName = marketName.toUpperCase().trim();
                if (new NamePatternValidator().isMarketNameValid(marketName)) {
                    logger.debug("Market name: " + marketName + " is valid.");
                    if (new TransactionValidator(marketName).isMarketValid()) {
                        ClassicTransaction classicTransaction = new ClassicTransaction(marketName, amount, rate, cancelAt);
                        message = classicTransaction.createClassicTransaction();
                    } else {
                        message = "There is already at least one order for this market. Please close all open orders to create a new order for market " + marketName;
                    }
                } else {
                    message = "Invalid market name: " + marketName;
                }
            } else {
                message = "Market name cannot be empty.";
            }
        } else {
            if (jtfRate.isEmpty() || jtfAmount.isEmpty()) {
                message = "Rate and amount cannot be empty. Only stop-loss field can be empty.";
            } else {
                message = "Value of a rate, amount or stop-loss is invalid.";
            }
        }
        new InfoDialog(message);
        return success;
    }
}
