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

import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
        String subject = "Booking confirmation #" + booking.getId();
        String body = buildBookingBody(booking);

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

    private String buildBookingBody(Booking b) {
        String trainNumber = b.getSchedule() != null && b.getSchedule().getTrain() != null
                ? b.getSchedule().getTrain().getTrainNumber()
                : "—";
        String depTime = b.getSchedule() == null || b.getSchedule().getDepartureTime() == null
                ? "—"
                : b.getSchedule().getDepartureTime().format(FMT);
        String arrTime = b.getSchedule() == null || b.getSchedule().getArrivalTime() == null
                ? "—"
                : b.getSchedule().getArrivalTime().format(FMT);
        String bookingDate = b.getBookingDate() == null ? "—" : b.getBookingDate().format(FMT);

        return String.format(
                "Hello %s,%n%n" +
                        "Your booking has been confirmed.%n%n" +
                        "Booking ID:   %d%n" +
                        "Train:        %s%n" +
                        "From:         %s · %s%n" +
                        "Departure:    %s%n" +
                        "To:           %s · %s%n" +
                        "Arrival:      %s%n" +
                        "Seats:        %d%n" +
                        "Booked on:    %s%n%n" +
                        "Have a pleasant journey.%n",
                b.getUser() == null ? "passenger" : b.getUser().getUsername(),
                b.getId(),
                trainNumber,
                b.getStartStation().getStationCity(), b.getStartStation().getStationName(),
                depTime,
                b.getEndStation().getStationCity(), b.getEndStation().getStationName(),
                arrTime,
                b.getSeatsReserved(),
                bookingDate
        );
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
