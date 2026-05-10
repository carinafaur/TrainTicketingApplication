package service;

import domain.Booking;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final String fromAddress;
    private final Session smtpSession;

    public EmailService(EmailConfig config) {
        if (!config.isEnabled()) {
            throw new IllegalStateException(
                    "EmailService: email is disabled in email.properties. Set email.enabled=true.");
        }
        this.fromAddress = config.getFromAddress();
        this.smtpSession = buildSmtpSession(config);
        System.out.println("EmailService: SMTP ready — host=" + config.getHost()
                + ", port=" + config.getPort() + ", from=" + fromAddress);
    }

    public void sendBookingConfirmation(String to, Booking booking) {
        send(to,
                "Booking confirmation #" + booking.getId(),
                EmailBodyBuilder.forBookingConfirmation(booking));
    }

    public void sendDelayNotification(String to, String trainNumber, int delayMinutes, Booking booking) {
        send(to,
                "Train " + trainNumber + " delayed by " + delayMinutes + " min",
                EmailBodyBuilder.forDelayNotification(trainNumber, delayMinutes, booking));
    }

    public void sendCancellationNotification(String to, String trainNumber, Booking booking) {
        send(to,
                "Train " + trainNumber + " cancelled",
                EmailBodyBuilder.forCancellationNotification(trainNumber, booking));
    }

    private void send(String to, String subject, String body) {
        try {
            MimeMessage msg = new MimeMessage(smtpSession);
            msg.setFrom(new InternetAddress(fromAddress));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setText(body, "UTF-8");
            Transport.send(msg);
            System.out.println("EmailService: sent '" + subject + "' to " + to);
        } catch (MessagingException e) {
            System.err.println("EmailService: SMTP delivery failed for " + to
                    + " (" + subject + "): " + e.getMessage());
        }
    }

    private static Session buildSmtpSession(EmailConfig cfg) {
        Properties props = new Properties();
        props.put("mail.smtp.host", cfg.getHost());
        props.put("mail.smtp.port", String.valueOf(cfg.getPort()));
        props.put("mail.smtp.auth", String.valueOf(cfg.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(cfg.isStarttls()));
        if (cfg.getPort() == 465) {
            props.put("mail.smtp.ssl.enable", "true");
        }

        if (cfg.isAuth()) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(cfg.getUsername(), cfg.getPassword());
                }
            });
        }
        return Session.getInstance(props);
    }
}
