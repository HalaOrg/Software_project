package edu.library.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RolesTest {

    @Test
    void testConstructorWithUsernamePassword() {
        Roles r = new Roles("areej", "1234");
        assertEquals("areej", r.getUsername());
        assertEquals("1234", r.getPassword());
        assertEquals("MEMBER", r.getRoleName());
        assertEquals("", r.getEmail());
        assertTrue(r.isMember());
        assertFalse(r.isAdmin());
        assertFalse(r.isLibrarian());
    }

    @Test
    void testConstructorWithRole() {
        Roles r = new Roles("lama", "pass", "ADMIN");
        assertEquals("ADMIN", r.getRoleName());
        assertTrue(r.isAdmin());
        assertFalse(r.isMember());
        assertFalse(r.isLibrarian());
    }

    @Test
    void testConstructorWithRoleAndEmail() {
        Roles r = new Roles("ali", "pass", "LIBRARIAN", "ali@test.com");
        assertEquals("LIBRARIAN", r.getRoleName());
        assertEquals("ali@test.com", r.getEmail());
        assertTrue(r.isLibrarian());
    }

    @Test
    void testNullRoleReplacedWithUnknown() {
        Roles r = new Roles("x", "y", null, "email@test");
        assertEquals("UNKNOWN", r.getRoleName());
    }

    @Test
    void testNullEmailReplacedWithEmpty() {
        Roles r = new Roles("x", "y", "ADMIN", null);
        assertEquals("", r.getEmail());
    }

    @Test
    void testIsAdminCaseInsensitive() {
        Roles r = new Roles("user", "pass", "AdMiN");
        assertTrue(r.isAdmin());
    }

    @Test
    void testIsMemberCaseInsensitive() {
        Roles r = new Roles("user", "pass", "MeMbEr");
        assertTrue(r.isMember());
    }

    @Test
    void testIsLibrarianCaseInsensitive() {
        Roles r = new Roles("user", "pass", "LiBrArIaN");
        assertTrue(r.isLibrarian());
    }

    @Test
    void testToStringNotNull() {
        Roles r = new Roles("a", "b", "ADMIN", "a@test.com");
        assertNotNull(r.toString());
        assertTrue(r.toString().contains("username='a'"));
    }

    @Test
    void testEqualsSameObject() {
        Roles r = new Roles("a", "b");
        assertEquals(r, r);
    }

    @Test
    void testEqualsDifferentObjectSameData() {
        Roles r1 = new Roles("a", "b");
        Roles r2 = new Roles("a", "b");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testEqualsDifferentUsername() {
        Roles r1 = new Roles("a", "b");
        Roles r2 = new Roles("x", "b");
        assertNotEquals(r1, r2);
    }

    @Test
    void testEqualsDifferentPassword() {
        Roles r1 = new Roles("a", "b");
        Roles r2 = new Roles("a", "c");
        assertNotEquals(r1, r2);
    }

    @Test
    void testEqualsWithNull() {
        Roles r1 = new Roles("a", "b");
        assertNotEquals(r1, null);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Roles r1 = new Roles("a", "b");
        assertNotEquals(r1, "string");
    }
    @Test
    void testConstructorWithUsernameOnly() {
        String username = "user1";
        Roles role = new Roles(username);

        // تحقق من اسم المستخدم
        assertEquals(username, role.getUsername());

        // تحقق من أن كلمة المرور فارغة
        assertEquals("", role.getPassword());

        // تحقق من أن roleName تم تعيينها للقيمة الافتراضية
        assertEquals(Roles.ROLE_MEMBER, role.getRoleName());

        // تحقق من أن البريد الالكتروني فارغ
        assertEquals("", role.getEmail());
    }
}
