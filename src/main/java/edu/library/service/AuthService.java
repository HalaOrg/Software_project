package edu.library.service;

import edu.library.model.Roles;

public class AuthService {
    private static final String DEFAULT_USERNAME = "Hala";
    private static final String DEFAULT_PASSWORD = "1234";

    private Roles currentAdmin;

    public Roles login(String username, String password) {
        if (username == null || password == null) return null;
        if (DEFAULT_USERNAME.equals(username) && DEFAULT_PASSWORD.equals(password)) {
            currentAdmin = new Roles(username, password);
            return currentAdmin;
        }
        return null;
    }


    public boolean logout() {
        if (currentAdmin == null) return false;
        currentAdmin = null;
        return true;
    }

    /**
     * Get the currently logged-in role (formerly admin), or null if none.
     */
    public Roles getCurrentAdmin() {
        return currentAdmin;
    }
}
