package edu.library.notification;

import edu.library.model.Roles;


public interface Observer {

    void notify(Roles user, String message);
}
