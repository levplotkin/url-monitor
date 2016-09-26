package com.url.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static java.text.MessageFormat.format;

class MailClient {

    private static Log log = LogFactory.getLog(MailClient.class);

    static void sendMessage(int port, String from, String subject, String body, String to) {
        try {
            Properties mailProps = getMailProperties(port);
            Session session = Session.getInstance(mailProps, null);

            MimeMessage msg = createMessage(session, from, to, subject, body);
            Transport.send(msg);
        } catch (Exception e) {
            log.error(format("Unexpected exception: {0}", e.getMessage()));
        }
    }

    private static Properties getMailProperties(int port) {
        Properties mailProps = new Properties();
        mailProps.setProperty("mail.smtp.host", "localhost");
        mailProps.setProperty("mail.smtp.port", "" + port);
        mailProps.setProperty("mail.smtp.sendpartial", "true");
        return mailProps;
    }

    private static MimeMessage createMessage(Session session, String from, String to, String subject, String body) throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setText(body);
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        return msg;
    }
}
