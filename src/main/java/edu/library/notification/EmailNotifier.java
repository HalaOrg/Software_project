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

        System.out.println("=== Reminder Email ===");
        System.out.println("To user   : " + user.getUsername());
        System.out.println("Email     : " + user.getEmail());
        System.out.println("Message   : " + message);
        System.out.println("======================");

        emailServer.sendEmail(user.getEmail(), message);
    }
}
