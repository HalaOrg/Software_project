package edu.library.domain.notification;

public interface EmailServer {

    void sendEmail(String to, String message);
}
