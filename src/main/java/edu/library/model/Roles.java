package edu.library.model;

import java.util.Objects;

public class Roles {

    // 🔥 ثابت بدل التكرار
    public static final String ROLE_MEMBER = "MEMBER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_LIBRARIAN = "LIBRARIAN";

    private final String username;
    private final String password;
    private final String roleName;
    private final String email;

    public Roles(String username, String password) {
        this(username, password, ROLE_MEMBER, null);
    }

    public Roles(String username, String password, String roleName) {
        this(username, password, roleName, null);
    }

    public Roles(String username, String password, String roleName, String email) {
        this.username = username;
        this.password = password;
        this.roleName = roleName == null ? "UNKNOWN" : roleName;
        this.email = email == null ? "" : email;
    }

    public Roles(String username) {
        this.username = username;
        this.password = "";
        this.roleName = ROLE_MEMBER;
        this.email = "";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(roleName);
    }

    public boolean isMember() {
        return ROLE_MEMBER.equalsIgnoreCase(roleName);
    }

    public boolean isLibrarian() {
        return ROLE_LIBRARIAN.equalsIgnoreCase(roleName);
    }

    @Override
    public String toString() {
        return "Roles{" +
                "username='" + username + '\'' +
                ", roleName='" + roleName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Roles roles = (Roles) o;
        return Objects.equals(username, roles.username)
                && Objects.equals(password, roles.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
}
