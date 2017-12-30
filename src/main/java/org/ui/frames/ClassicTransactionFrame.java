package org.ui.frames;

import org.apache.log4j.Logger;
import org.logic.models.responses.MarketBalanceResponse;
import org.logic.models.responses.MarketSummaryResponse;
import org.logic.transactions.ClassicTransaction;
import org.logic.utils.ModelBuilder;
import org.logic.validators.PatternValidator;
import org.ui.Constants;
import org.ui.frames.util.SingleInstanceFrame;
import org.ui.views.dialog.box.InfoDialog;
import org.ui.views.textfield.HintTextField;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.preferences.Constants.MAX_INPUT_VALUE;

public class ClassicTransactionFrame extends SingleInstanceFrame {

    private final static String[] ARR_MODES = {"Buy", "Sell"};
    private JComboBox jComboBoxMode;
    private JLabel labelMarketName, labelAmount, labelRate, labelCancelAt, labelSlider;
    private HintTextField jtfMarketName, jtfAmount, jtfRate, jtfCancelAt;
    private JButton jbCreate;
    private JSlider jSliderThreshold;
    private Logger logger = Logger.getLogger(ClassicTransactionFrame.class);

    public ClassicTransactionFrame() {
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

        labelCancelAt = new JLabel("<html>Stop loss. This option will cancel this order and place new sell order immediately if coin's price drops below chosen value.</html>");
        jtfCancelAt = new HintTextField("Leave empty to do not enable stop-loss.");
        pMain.add(labelCancelAt);
        pMain.add(jtfCancelAt);

        jSliderThreshold = new JSlider(JSlider.HORIZONTAL, 5, 95, 95);
        jSliderThreshold.setMinorTickSpacing(5);
        jSliderThreshold.setMajorTickSpacing(5);
        jSliderThreshold.setPaintTicks(true);
        jSliderThreshold.setPaintLabels(true);
        jSliderThreshold.setLabelTable(jSliderThreshold.createStandardLabels(10));
        labelSlider = new JLabel("<html>This allows stop-loss to be executed only, " +
                "if the coin's last price was below stop-loss value and above given % of stop-loss value." +
                " Useful in case coin's value drops dramatically.</html>");
        pMain.add(labelSlider);
        pMain.add(jSliderThreshold);

        jbCreate = new JButton("Create transaction");
        jbCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeOrder();
            }
        });

        jtfMarketName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                loadHint();
            }

            public void removeUpdate(DocumentEvent e) {
                loadHint();
            }

            public void insertUpdate(DocumentEvent e) {
                loadHint();
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

    private void loadHint() {
        final String marketName = jtfMarketName.getText();
        if (marketName.length() > 6) {
            if (marketName.substring(0, 4).equalsIgnoreCase("BTC-") || marketName.substring(0, 5).equalsIgnoreCase("USDT-")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MarketSummaryResponse marketSummaryResponse = ModelBuilder.buildMarketSummary(marketName);
                        if (marketSummaryResponse != null && marketSummaryResponse.isSuccess()) {
                            labelRate.setText("Rate: (Last: " + marketSummaryResponse.getResult().get(0).getLast() + ")");
                        }
                        String coin = marketName.substring(4,marketName.length());
                        MarketBalanceResponse marketBalanceResponse = ModelBuilder.buildMarketBalance(coin);
                        if (marketBalanceResponse != null && marketBalanceResponse.isSuccess()) {
                            labelAmount.setText("Amount: (Available: " + marketBalanceResponse.getResult().getAvailable() + ")");
                        }
                    }
                }).start();
            }
        }
    }

    private boolean placeOrder() {
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
                if (new PatternValidator().isMarketNameValid(marketName)) {
                    logger.debug("Market name: " + marketName + " is valid.");
                    int threshold = jSliderThreshold.getValue();
                    ClassicTransaction classicTransaction = new ClassicTransaction(marketName, amount, rate, cancelAt,
                            jComboBoxMode.getSelectedIndex() == 0, threshold);
                    message = classicTransaction.createClassicTransaction();
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
                message = "Value of a rate, amount or stop-loss is invalid. Maximum value is " + MAX_INPUT_VALUE + ".";
            }
        }
        new InfoDialog(message);
        return success;
    }
}
