package edu.library.notification;

import edu.library.model.Roles;

/**
 * Observer implementation that delegates to an {@link EmailServer}.
 */
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
