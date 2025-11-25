package edu.library.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolesTest {

    @Test
    void defaultsToAdminRole() {
        Roles roles = new Roles("Hala", "1234");

        assertEquals("Hala", roles.getUsername());
        assertEquals("1234", roles.getPassword());
        assertEquals("MEMBER", roles.getRoleName());
        assertEquals("", roles.getEmail());
        assertTrue(roles.isMember());
        assertFalse(roles.isAdmin());
        assertFalse(roles.isLibrarian());
    }

    @Test
    void constructorWithRole_shouldSetGivenRoleAndEmptyEmail() {
        Roles roles = new Roles("Sara", "pass", "ADMIN");

        assertEquals("Sara", roles.getUsername());
        assertEquals("pass", roles.getPassword());
        assertEquals("ADMIN", roles.getRoleName());
        assertEquals("", roles.getEmail());
        assertTrue(roles.isAdmin());
        assertFalse(roles.isMember());
        assertFalse(roles.isLibrarian());
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
    void fullConstructor_shouldSetAllFieldsAndHandleNulls() {
        // case 1: normal values
        Roles r1 = new Roles("Lana", "0000", "LIBRARIAN", "lana@example.com");
        assertEquals("Lana", r1.getUsername());
        assertEquals("0000", r1.getPassword());
        assertEquals("LIBRARIAN", r1.getRoleName());
        assertEquals("lana@example.com", r1.getEmail());
        assertTrue(r1.isLibrarian());

        // case 2: null roleName
        Roles r2 = new Roles("User", "1111", null, "u@example.com");
        assertEquals("UNKNOWN", r2.getRoleName());

        // case 3: null email
        Roles r3 = new Roles("User2", "2222", "MEMBER", null);
        assertEquals("", r3.getEmail());
    }

    @Test
    void nullRoleNameFallsBackToUnknown() {
        Roles unknown = new Roles("user", "pw", null);

        assertEquals("UNKNOWN", unknown.getRoleName());
        assertFalse(unknown.isAdmin());
    }

    @Test
    void toStringContainsUsernameRoleAndEmail() {
        Roles r = new Roles("Lana", "1111", "LIBRARIAN", "lana@example.com");

        String text = r.toString();

        assertTrue(text.contains("Lana"));
        assertTrue(text.contains("LIBRARIAN"));
        assertTrue(text.contains("lana@example.com"));
    }

    @Test
    void equalsAndHashCode() {
        Roles r1 = new Roles("Hala", "1234", "ADMIN", "a@a.com");
        Roles r2 = new Roles("Hala", "1234", "MEMBER", "b@b.com");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        Roles r3 = new Roles("Hala", "abcd", "ADMIN", "a@a.com");
        assertNotEquals(r1, r3);

        Roles r4 = new Roles("OtherUser", "1234", "ADMIN", "a@a.com");
        assertNotEquals(r1, r4);
    }

}