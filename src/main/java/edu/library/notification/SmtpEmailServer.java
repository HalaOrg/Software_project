package edu.library.notification;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Objects;
import java.util.Properties;

/**
 * SMTP-backed implementation of {@link EmailServer} using Jakarta Mail.
 */
public class SmtpEmailServer implements EmailServer {
    private final String host;
    private final int port;
    private final boolean startTls;
    private final String username;
    private final String password;
    private final String fromAddress;

    public SmtpEmailServer() {
        this(new SmtpEmailSettings());
    }

    public SmtpEmailServer(SmtpEmailSettings settings) {
        this.host = Objects.requireNonNullElse(settings.host, "");
        this.port = settings.port;
        this.startTls = settings.startTls;
        this.username = Objects.requireNonNullElse(settings.username, "");
        this.password = Objects.requireNonNullElse(settings.password, "");
        this.fromAddress = settings.fromAddress == null || settings.fromAddress.isBlank()
                ? this.username
                : settings.fromAddress;
    }

    @Override
    public void sendEmail(String to, String message) {
        if (to == null || to.isBlank() || message == null) {
            return;
        }
        if (!isConfigured()) {
            System.out.println("Email configuration is incomplete; skipping send.");
            return;
        }
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", Boolean.toString(startTls));
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", Integer.toString(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(fromAddress));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            mimeMessage.setSubject("Overdue Book Reminder");
            mimeMessage.setText(message);
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private boolean isConfigured() {
        return !host.isBlank() && !username.isBlank() && !password.isBlank();
    }

    /**
     * Immutable SMTP configuration.
     */
    public static class SmtpEmailSettings {
        private final String host;
        private final int port;
        private final boolean startTls;
        private final String username;
        private final String password;
        private final String fromAddress;

        public SmtpEmailSettings() {
            this(
                    System.getProperty("smtp.host", System.getenv().getOrDefault("SMTP_HOST", "smtp.gmail.com")),
                    Integer.parseInt(System.getProperty("smtp.port", System.getenv().getOrDefault("SMTP_PORT", "587"))),
                    Boolean.parseBoolean(System.getProperty("smtp.starttls", System.getenv().getOrDefault("SMTP_STARTTLS", "true"))),
                    System.getProperty("smtp.username", System.getenv("SMTP_USERNAME")),
                    System.getProperty("smtp.password", System.getenv("SMTP_PASSWORD")),
                    System.getProperty("smtp.from", System.getenv("SMTP_FROM"))
            );
        }

        public SmtpEmailSettings(String host, int port, boolean startTls, String username, String password, String fromAddress) {
            this.host = host;
            this.port = port;
            this.startTls = startTls;
            this.username = username;
            this.password = password;
            this.fromAddress = fromAddress;
        }
    }
}
