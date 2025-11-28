package edu.library.notification;

public interface EmailServer {

    void sendEmail(String to, String message);
}
