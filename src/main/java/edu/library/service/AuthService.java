package edu.library.service;

import edu.library.model.Admin;

public class AuthService {
    // Simple hardcoded admin credentials for demo purposes
    private static final String DEFAULT_USERNAME = "Hala";
    private static final String DEFAULT_PASSWORD = "1234";

    // Track the currently logged-in admin (if any)
    private Admin currentAdmin;

    public Admin login(String username, String password) {
        if (username == null || password == null) return null;
        if (DEFAULT_USERNAME.equals(username) && DEFAULT_PASSWORD.equals(password)) {
            currentAdmin = new Admin(username, password);
            return currentAdmin;
        }
        return null;
    }

    /**
     * Logout the current admin. Returns true if there was an admin logged in and was logged out.
     */
    public boolean logout() {
        if (currentAdmin == null) return false;
        currentAdmin = null;
        return true;
    }

    /**
     * Get the currently logged-in admin, or null if none.
     */
    public Admin getCurrentAdmin() {
        return currentAdmin;
    }
}
