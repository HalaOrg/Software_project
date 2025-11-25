package edu.library.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolesTest {

    @Test
    void defaultsToAdminRole() {
        Roles role = new Roles("Hala", "1234");

        assertEquals("Hala", role.getUsername());
        assertEquals("1234", role.getPassword());
        assertEquals("ADMIN", role.getRoleName());
        assertTrue(role.isAdmin());
    }

    @Test
    void supportsCustomRoleNames() {
        Roles librarian = new Roles("librarian", "pass", "LIBRARIAN");

        assertEquals("librarian", librarian.getUsername());
        assertEquals("pass", librarian.getPassword());
        assertEquals("LIBRARIAN", librarian.getRoleName());
        assertFalse(librarian.isAdmin());
    }

    @Test
    void nullRoleNameFallsBackToUnknown() {
        Roles unknown = new Roles("user", "pw", null);

        assertEquals("UNKNOWN", unknown.getRoleName());
        assertFalse(unknown.isAdmin());
    }

    @Test
    void toStringContainsUsernameAndRole() {
        Roles member = new Roles("member", "pw", "USER");

        String text = member.toString();

        assertTrue(text.contains("member"));
        assertTrue(text.contains("USER"));
    }
}