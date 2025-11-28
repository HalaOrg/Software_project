package edu.library.notification;

import edu.library.model.Roles;


public class EmailNotifier implements Observer {
    private final EmailServer emailServer;

    public EmailNotifier(EmailServer emailServer) {
        this.emailServer = emailServer;
    }

    @Override
    public void notify(Roles user, String message) {
        if (user == null || message == null) {
            return;
        }
        emailServer.sendEmail(user.getEmail(), message);
    }
}
