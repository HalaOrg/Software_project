package edu.library.notification;

import edu.library.model.Roles;
import java.util.logging.Logger;

public class EmailNotifier implements Observer {

    private static final Logger LOGGER =
            Logger.getLogger(EmailNotifier.class.getName());

    private final EmailServer emailServer;

    public EmailNotifier(EmailServer emailServer) {
        this.emailServer = emailServer;
    }

    @Override
    public void notify(Roles user, String message) {
        if (user == null || message == null) {
            LOGGER.warning("Attempted to send notification to null user or with null message.");
            return;
        }

        LOGGER.info("=== Reminder Email ===");
        LOGGER.info("To user   : " + user.getUsername());
        LOGGER.info("Email     : " + user.getEmail());
        LOGGER.info("Message   : " + message);
        LOGGER.info("======================");

        emailServer.sendEmail(user.getEmail(), message);
    }
}

