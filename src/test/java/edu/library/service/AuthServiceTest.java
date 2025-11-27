package edu.library.service;

import edu.library.model.Roles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void loginReadsUsersFromFile() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Arrays.asList(
                "admin,admin123,ADMIN",
                "member,member123,MEMBER"
        ));

        AuthService authService = new AuthService(usersFile.toString());

        Roles admin = authService.login("admin", "admin123");
        assertNotNull(admin);
        assertTrue(admin.isAdmin());
        assertEquals("ADMIN", admin.getRoleName());

        Roles member = authService.login("member", "member123");
        assertNotNull(member);
        assertEquals("MEMBER", member.getRoleName());
    }

    @Test
    void addUserPersistsToDisk() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService authService = new AuthService(usersFile.toString());

        assertNull(authService.login("librarian", "lib123"));
        authService.addUser("librarian", "lib123", "LIBRARIAN", "librarian@example.com");

        AuthService reloaded = new AuthService(usersFile.toString());
        Roles librarian = reloaded.login("librarian", "lib123");
        assertNotNull(librarian);
        assertEquals("LIBRARIAN", librarian.getRoleName());
    }

    @Test
    void loginFailsForUnknownCredentials() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("user,password,MEMBER"));
        AuthService authService = new AuthService(usersFile.toString());

        assertNull(authService.login("user", "wrong"));
        assertNull(authService.login("unknown", "password"));
    }

    @Test
    void login_success() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("alice,alicepwd,MEMBER"));
        AuthService auth = new AuthService(usersFile.toString());

        Roles r = auth.login("alice", "alicepwd");
        assertNotNull(r);
        assertEquals("alice", r.getUsername());
        assertEquals("MEMBER", r.getRoleName());
    }

    @Test
    void login_wrongPassword() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("bob,bobpwd,MEMBER"));
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.login("bob", "wrongpwd"));
    }

    @Test
    void login_wrongUsername() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("carol,carolpwd,MEMBER"));
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.login("notcarol", "carolpwd"));
    }

    @Test
    void login_nullInputs() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("dave,davepwd,ADMIN"));
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.login(null, "davepwd"));
        assertNull(auth.login("dave", null));
        assertNull(auth.login(null, null));
    }

    @Test
    void login_afterSuccessAndFailure() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Arrays.asList(
                "eve,evepwd,MEMBER",
                "frank,frankpwd,LIBRARIAN"
        ));
        AuthService auth = new AuthService(usersFile.toString());

        // failed attempt
        assertNull(auth.login("eve", "wrong"));
        // successful attempt
        Roles eve = auth.login("eve", "evepwd");
        assertNotNull(eve);
        assertEquals("eve", auth.getCurrentUser().getUsername());

        // login as another user should replace currentUser
        Roles frank = auth.login("frank", "frankpwd");
        assertNotNull(frank);
        assertEquals("frank", auth.getCurrentUser().getUsername());
    }

    @Test
    void addUser_tests() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        Roles added = auth.addUser("greg", "gregpwd", "MEMBER", "greg@example.com");
        assertNotNull(added);
        assertTrue(auth.userExists("greg"));

        AuthService reloaded = new AuthService(usersFile.toString());
        assertNotNull(reloaded.login("greg", "gregpwd"));
    }

    @Test
    void removeUser_tests() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("harry", "harrypwd", "MEMBER", "harry@example.com");
        assertTrue(auth.userExists("harry"));

        assertTrue(auth.removeUser("harry"));
        assertFalse(auth.userExists("harry"));

        AuthService reloaded = new AuthService(usersFile.toString());
        assertNull(reloaded.login("harry", "harrypwd"));

        // removing non-existent user
        assertFalse(auth.removeUser("noone"));
    }

    @Test
    void userExists_tests() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        assertFalse(auth.userExists("ivy"));
        auth.addUser("ivy", "ivypwd", "MEMBER", "ivy@example.com");
        assertTrue(auth.userExists("ivy"));
    }

    @Test
    void logout_tests() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("john,johnpwd,MEMBER"));
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.getCurrentUser());
        Roles john = auth.login("john", "johnpwd");
        assertNotNull(john);
        assertTrue(auth.logout());
        assertNull(auth.getCurrentUser());

        // logging out when nobody is logged in
        assertFalse(auth.logout());
    }

    @Test
    void addUser_withEmailPersists() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        Roles r = auth.addUser("lee", "leepwd", "MEMBER", "lee@example.com");
        assertNotNull(r);
        assertEquals("lee@example.com", r.getEmail());

        AuthService reloaded = new AuthService(usersFile.toString());
        Roles loaded = reloaded.login("lee", "leepwd");
        assertNotNull(loaded);
        assertEquals("lee@example.com", loaded.getEmail());
    }

    @Test
    void addUser_nullParams_returnsNull() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.addUser(null, "pwd", "MEMBER", "x@example.com"));
        assertNull(auth.addUser("u", null, "MEMBER", "x@example.com"));
        assertNull(auth.addUser("u", "pwd", null, "x@example.com"));
        assertNull(auth.addUser("u", "pwd", "MEMBER", null));
        assertNull(auth.addUser("u", "pwd", "MEMBER", "   "));
    }

    @Test
    void userExists_caseInsensitive() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("CaseUser", "pwd", "MEMBER", "case@example.com");
        assertTrue(auth.userExists("caseuser"));
        assertTrue(auth.userExists("CASEUSER"));
    }

    @Test
    void removeUser_caseInsensitive() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("Sam", "sampwd", "MEMBER", "sam@example.com");
        assertTrue(auth.userExists("sam"));
        assertTrue(auth.removeUser("sAm"));
        assertFalse(auth.userExists("sam"));
    }

    @Test
    void getUsers_returnsCopy() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("copyUser", "pwd", "MEMBER", "copy@example.com");
        java.util.List<Roles> list = auth.getUsers();
        assertFalse(list.isEmpty());
        // modify returned list
        list.clear();
        // underlying users in auth should remain
        assertTrue(auth.userExists("copyUser"));
    }

    @Test
    void removeUser_clearsCurrentUser_whenRemovingLoggedInUser() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("tom", "tompwd", "MEMBER", "tom@example.com");
        Roles tom = auth.login("tom", "tompwd");
        assertNotNull(tom);
        assertEquals("tom", auth.getCurrentUser().getUsername());

        assertTrue(auth.removeUser("tom"));
        assertNull(auth.getCurrentUser());

        AuthService reloaded = new AuthService(usersFile.toString());
        assertNull(reloaded.login("tom", "tompwd"));
    }

    @Test
    void loadUsers_createsFile_whenNotExists() {
        Path usersFile = tempDir.resolve("missing_users.txt");
        // do not create the file manually
        assertFalse(Files.exists(usersFile));

        // instantiate the service to trigger file creation (no local variable needed)
        new AuthService(usersFile.toString());
        // after construction the service should have created the file
        assertTrue(Files.exists(usersFile));
    }

    @Test
    void login_caseSensitiveUsername() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("CaseUser,pass,MEMBER"));
        AuthService auth = new AuthService(usersFile.toString());

        // login is case-sensitive for username
        assertNull(auth.login("caseuser", "pass"));
        assertNotNull(auth.login("CaseUser", "pass"));
    }

    @Test
    void addUser_duplicateUsernames_allowed() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("dupe", "pwd1", "MEMBER", "dupe1@example.com");
        auth.addUser("dupe", "pwd2", "MEMBER", "dupe2@example.com");

        // both passwords should authenticate because both entries were added
        assertNotNull(auth.login("dupe", "pwd1"));
        assertNotNull(auth.login("dupe", "pwd2"));

        AuthService reloaded = new AuthService(usersFile.toString());
        assertNotNull(reloaded.login("dupe", "pwd1"));
        assertNotNull(reloaded.login("dupe", "pwd2"));
    }

    @Test
    void getCurrentAdmin_afterAdminLogin_andLogout() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("admin,adminpwd,ADMIN,admin@example.com"));
        AuthService auth = new AuthService(usersFile.toString());

        // before login there should be no current admin
        assertNull(auth.getCurrentAdmin());

        Roles admin = auth.login("admin", "adminpwd");
        assertNotNull(admin);
        // getCurrentAdmin should return the logged-in user
        Roles current = auth.getCurrentAdmin();
        assertNotNull(current);
        assertTrue(current.isAdmin());
        assertEquals("admin", current.getUsername());

        // logging out clears the current admin
        assertTrue(auth.logout());
        assertNull(auth.getCurrentAdmin());
    }

    @Test
    void getCurrentAdmin_isSameAsGetCurrentUser_afterLogin() throws IOException {
        Path usersFile = tempDir.resolve("users2.txt");
        Files.write(usersFile, Collections.singletonList("u1,pwd1,MEMBER,u1@example.com"));
        AuthService auth = new AuthService(usersFile.toString());

        Roles r = auth.login("u1", "pwd1");
        assertNotNull(r);
        // getCurrentAdmin currently returns the same currentUser instance
        assertSame(auth.getCurrentUser(), auth.getCurrentAdmin());
    }
}