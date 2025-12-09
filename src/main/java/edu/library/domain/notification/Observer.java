package edu.library.domain.notification;

import edu.library.domain.model.Roles;


public interface Observer {

    void notify(Roles user, String message);
}
