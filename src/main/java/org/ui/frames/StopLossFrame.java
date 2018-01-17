package org.ui.frames;

import org.apache.log4j.Logger;
import org.logic.exceptions.EntryExistsException;
import org.logic.transactions.model.stoploss.StopLossOption;
import org.logic.transactions.model.stoploss.StopLossOptionManager;
import org.logic.transactions.model.stoploss.StopLossCondition;
import org.logic.validators.PatternValidator;
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
import java.io.IOException;

public class StopLossFrame extends SingleInstanceFrame {

    private JLabel labelMarketName, labelRate;
    private HintTextField jtfMarketName, jtfRate;
    private JButton jbSubmit;
    private JComboBox<StopLossCondition> jComboBoxMode;
    private StopLossCondition[] ARR_MODES = {StopLossCondition.BELOW, StopLossCondition.ABOVE};//Arrays.toString(StopLossCondition.values()).replaceAll("^.|.$", "").split(", ");
    private JCheckBox jCheckBox;

    private Logger logger = Logger.getLogger(StopLossFrame.class);

    public StopLossFrame() {
        jComboBoxMode = new JComboBox<>(ARR_MODES);
        init();
    }

    private void init() {
        setTitle("Stop-loss Transaction Creator");

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

        jCheckBox = new JCheckBox("Stop-loss ALL");
        jCheckBox.setSelected(false);
        pMain.add(jCheckBox);
        pMain.add(new JLabel());
        jCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jCheckBox.isSelected()) {
                    jtfMarketName.setText("ALL");
                    jtfMarketName.setEditable(false);
                    jtfMarketName.setFocusable(false);
                } else {
                    jtfMarketName.setText("");
                    jtfMarketName.setEditable(true);
                    jtfMarketName.setFocusable(true);
                }
            }
        });

        // Bottom panel
        JPanel pBottom = new JPanel();
        pBottom.setBorder(new TitledBorder(new EtchedBorder()));
        pBottom.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        pBottom.setLayout(new GridLayout(0, 1));
        jbSubmit = new JButton("Submit");
        jbSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateEntries();
            }
        });
        pBottom.add(jbSubmit);

        jComboBoxMode = new JComboBox<>(ARR_MODES);
        pBottom.add(jComboBoxMode);

        cp.add(pMain, BorderLayout.CENTER);
        cp.add(pBottom, BorderLayout.SOUTH);

        setSize(Constants.SETUP_FRAME_WIDTH, Constants.SETUP_FRAME_HEIGHT);
        setVisible(true);

        logger.debug("StopLossFrame initialized");
    }

    private void validateEntries() {
        String marketName = jtfMarketName.getText();
        StopLossCondition condition = (StopLossCondition) jComboBoxMode.getSelectedItem();
        boolean selectAll = jCheckBox.isSelected();
        if (jtfRate.isValidDoubleOrEmpty()) {
            if (!selectAll && !new PatternValidator().isMarketNameValid(marketName)) {
                logger.debug("Market name is invalid.");
                new InfoDialog("Market name is invalid.");
            }
            double rate = jtfRate.getAsDouble();
            if (rate > 0.0d) {
                try {
                    execute(marketName, rate, condition, selectAll);
                } catch (IOException e) {
                    logger.error("Failed to register new stop-loss transaction.");
                    new InfoDialog("Failed to register new stop-loss transaction.");
                } catch (EntryExistsException e) {
                    logger.error(e.getMessage());
                    new InfoDialog(e.getMessage());
                }
            } else {
                logger.debug("Stop-loss value must be greater than zero.");
                new InfoDialog("Stop-loss value must be greater than zero.");
            }
        } else {
            logger.debug("Stop-loss value must be greater than zero.");
            new InfoDialog("Stop-loss value must be greater than zero.");
        }
    }

    private void execute(String marketName, double rate, StopLossCondition condition, boolean sellAll) throws IOException, EntryExistsException {
        StopLossOption stopLossOption = new StopLossOption(marketName, rate, condition, sellAll);
        StopLossOptionManager.getInstance().addOption(stopLossOption);
        logger.debug("New stop-loss option {" + condition.toString() + "} added for market " + marketName +
                " Rate was set to " + rate + ".");
        new InfoDialog("New stop-loss option " + condition.toString() + " added for market " + marketName.toUpperCase() +
                ". Rate was set to " + rate + ".");
    }
}
