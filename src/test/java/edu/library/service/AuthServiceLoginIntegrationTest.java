package edu.library.service;

import edu.library.model.Roles;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceLoginIntegrationTest {

    @Test
    void login_persistsOutstandingFinesImmediately() throws Exception {
        Path tempDir = Files.createTempDirectory("library-auth");
        Path usersFile = tempDir.resolve("users.txt");
        Files.writeString(usersFile, "alice,pw,MEMBER,alice@example.com\n");

        Path finesFile = tempDir.resolve("fines.txt");
        FineService fineService = new FineService(finesFile.toString());
        fineService.addFine("alice", 25);
        Files.deleteIfExists(finesFile);

        AuthService authService = new AuthService(usersFile.toString(), fineService);

        Roles user = authService.login("alice", "pw");

        assertNotNull(user);
        assertTrue(Files.exists(finesFile));
        String content = Files.readString(finesFile);
        assertTrue(content.contains("alice,25"));
    }
}
