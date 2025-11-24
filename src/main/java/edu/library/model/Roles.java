package edu.library.model;

/**
 * Represents a role-based account that can authenticate within the library system.
 * It currently stores the username, password, and a role name to differentiate
 * between potential user types (e.g., admin, librarian, member).
 */
public class Roles {
    private final String username;
    private final String password;
    private final String roleName;

    /**
     * Constructs a role defaulting to {@code ADMIN} for backward compatibility with
     * the existing authentication flow.
     *
     * @param username account username
     * @param password account password
     */
    public Roles(String username, String password) {
        this(username, password, "ADMIN");
    }

    /**
     * Constructs a role with an explicit role name.
     *
     * @param username account username
     * @param password account password
     * @param roleName logical role name (e.g., ADMIN, USER)
     */
    public Roles(String username, String password, String roleName) {
        this.username = username;
        this.password = password;
        this.roleName = roleName == null ? "UNKNOWN" : roleName;
    }

    /**
     * @return the username associated with this role
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password associated with this role
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the logical role name (e.g., ADMIN, USER)
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Helper to check whether this role is an administrator.
     *
     * @return {@code true} if the role name equals "ADMIN" (case-insensitive)
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(roleName);
    }

    @Override
    public String toString() {
        return "Roles{" +
                "username='" + username + '\'' +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}
