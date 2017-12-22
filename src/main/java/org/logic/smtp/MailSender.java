package org.logic.smtp;

import org.apache.log4j.Logger;
import org.preferences.managers.PreferenceManager;
import org.ui.views.dialog.box.InfoDialog;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailSender {

    private Logger logger = Logger.getLogger(MailSender.class);

    public void sendEmailNotification(String subject, final String messageText) throws MessagingException {
        final String addressee = PreferenceManager.getEmailAddress();
        final String password = PreferenceManager.getEmailPassword();
        sendEmailNotification(addressee, password, subject, messageText);
    }

    public void sendEmailNotification(String address, String password, String subject, final String messageText) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");

        Session session = Session.getDefaultInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(address, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("AltBot"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(address));
            message.setSubject(subject);
            try {
                message.setText(messageText);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            Transport.send(message);
            logger.debug("Mail sent to: " + address);
        } catch (MessagingException e) {
            new InfoDialog("Failed to authenticate email " + address);
            throw new RuntimeException(e);
        }
    }
}
