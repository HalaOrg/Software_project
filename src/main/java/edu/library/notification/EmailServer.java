package edu.library.notification;

/**
 * Simple abstraction for sending email messages so it can be mocked in tests.
 */
public interface EmailServer {
    /**
     * Send an email to the provided address.
     *
     * @param to      recipient email address
     * @param message body content
     */
    void sendEmail(String to, String message);
}
