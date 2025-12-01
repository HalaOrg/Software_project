package edu.library.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolesTest {

    @Test
    void testConstructorWithUsernameAndPassword() {
        Roles r = new Roles("user1", "pass123");

        assertEquals("user1", r.getUsername());
        assertEquals("pass123", r.getPassword());
        assertEquals("MEMBER", r.getRoleName());   // role افتراضي
        assertEquals("", r.getEmail());            // email افتراضي
    }

    @Test
    void testConstructorWithRole() {
        Roles r = new Roles("admin", "123", "ADMIN");

        assertEquals("admin", r.getUsername());
        assertEquals("123", r.getPassword());
        assertEquals("ADMIN", r.getRoleName());
    }

    @Test
    void testConstructorWithNullRole() {
        Roles r = new Roles("test", "1234", null);

        assertEquals("UNKNOWN", r.getRoleName());
    }

    @Test
    void testConstructorWithEmail() {
        Roles r = new Roles("user", "pass", "MEMBER", "u@u.com");

        assertEquals("MEMBER", r.getRoleName());
        assertEquals("u@u.com", r.getEmail());
    }

    @Test
    void testRoleChecks() {
        Roles admin = new Roles("a", "1", "ADMIN");
        Roles librarian = new Roles("l", "2", "LIBRARIAN");
        Roles member = new Roles("m", "3", "MEMBER");

        assertTrue(admin.isAdmin());
        assertFalse(admin.isMember());
        assertTrue(librarian.isLibrarian());
        assertTrue(member.isMember());
    }

    @Test
    void testEqualsAndHashCode() {
        Roles r1 = new Roles("user", "pass");
        Roles r2 = new Roles("user", "pass");
        Roles r3 = new Roles("user", "different");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        assertNotEquals(r1, r3);
    }

    @Test
    void testToString() {
        Roles r = new Roles("x", "y", "ADMIN", "mail@mail.com");
        String str = r.toString();

        assertTrue(str.contains("ADMIN"));
        assertTrue(str.contains("x"));
        assertTrue(str.contains("mail@mail.com"));
    }
}
