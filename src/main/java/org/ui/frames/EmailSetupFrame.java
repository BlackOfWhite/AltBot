package org.ui.frames;

import org.apache.log4j.Logger;
import org.logic.smtp.MailSender;
import org.logic.validators.PatternValidator;
import org.preferences.managers.PreferenceManager;
import org.ui.Constants;
import org.ui.frames.util.SingleInstanceFrame;
import org.ui.views.dialog.box.InfoDialog;
import org.ui.views.textfield.HintTextField;

import javax.mail.MessagingException;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EmailSetupFrame extends SingleInstanceFrame {

    private JLabel labelEmailAddress, labelPassword, labelEnabled;
    private HintTextField jtfEmailAddress;
    private JPasswordField jtfPassword;
    private JButton jbSubmit;
    private JCheckBox jcbEnabled;
    private Logger logger = Logger.getLogger(EmailSetupFrame.class);

    public EmailSetupFrame() {
        init();
    }

    private void init() {
        setTitle("Email Setup");

        Container cp = getContentPane();
        JPanel pMain = new JPanel();
        pMain.setBorder(new TitledBorder(new EtchedBorder()));
        pMain.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        pMain.setLayout(new GridLayout(0, 2));

        labelEmailAddress = new JLabel("Email address:");
        jtfEmailAddress = new HintTextField("my-email@gmail.com");
        pMain.add(labelEmailAddress);
        pMain.add(jtfEmailAddress);

        labelPassword = new JLabel("Password:");
        jtfPassword = new JPasswordField("");
        pMain.add(labelPassword);
        pMain.add(jtfPassword);
        cp.add(pMain, BorderLayout.NORTH);

        labelEnabled = new JLabel("Enable notifications:");
        jcbEnabled = new JCheckBox();

        cp.add(labelEnabled);
        cp.add(jcbEnabled);

        jbSubmit = new JButton("Submit");
        jbSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submit();
            }
        });
        cp.add(jbSubmit);

        setSize(Constants.SETUP_FRAME_WIDTH, Constants.SETUP_FRAME_HEIGHT);
        setVisible(true);
        logger.debug("EmailSetupFrame initialized");
    }

    private void submit() {
        String address = jtfEmailAddress.getText();
        String password = String.valueOf(jtfPassword.getPassword());
        PatternValidator patternValidator = new PatternValidator();
        if (patternValidator.isEmailValid(address)) {
            if (password != null && !password.isEmpty()) {
                MailSender mailSender = new MailSender();
                try {
                    mailSender.sendEmailNotification(address, password, "Mail sender - Test", "This is just a verification email. Please do not reply.");
                    logger.debug("New email and password setup correctly.");
                    PreferenceManager.setEmailAddress(address);
                    PreferenceManager.setEmailPassword(password, true);
                    new InfoDialog(this, "Authentication successful. Please check your mailbox for the test email.");
                } catch (MessagingException e) {
                    e.printStackTrace();
                    logger.debug(e.getMessage() + " " + e.getStackTrace().toString());
                    new InfoDialog(this, "Authentication error!");
                }
            } else {
                new InfoDialog(this, "Please enter password.");
            }
        } else {
            new InfoDialog(this, "Invalid email address!");
        }
    }
}

