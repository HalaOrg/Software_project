package edu.library.service;

import edu.library.model.Roles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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
    void addUserPersistsToDisk() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService authService = new AuthService(usersFile.toString());

        assertNull(authService.login("librarian", "lib123"));
        authService.addUser("librarian", "lib123", "LIBRARIAN");

        AuthService reloaded = new AuthService(usersFile.toString());
        Roles librarian = reloaded.login("librarian", "lib123");
        assertNotNull(librarian);
        assertEquals("LIBRARIAN", librarian.getRoleName());
    }

    @Test
    void loginFailsForUnknownCredentials() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Arrays.asList("user,password,MEMBER"));
        AuthService authService = new AuthService(usersFile.toString());

        assertNull(authService.login("user", "wrong"));
        assertNull(authService.login("unknown", "password"));
    }
}