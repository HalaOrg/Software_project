package edu.library.notification;

import edu.library.model.Roles;

/**
 * Observer for sending notifications to users.
 */
public interface Observer {
    /**
     * Notify a user with a message.
     *
     * @param user    target user
     * @param message message to deliver
     */
    void notify(Roles user, String message);
}
