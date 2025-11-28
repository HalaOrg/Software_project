package edu.library.service;

import edu.library.model.Roles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceLoginFineTest {

    @TempDir
    Path tempDir;

    @Test
    void loginWithOutstandingFines_persistsBalancesImmediately() throws Exception {
        Path finesFile = tempDir.resolve("fines_login.txt");
        FineService fineService = new FineService(finesFile.toString());
        Path usersFile = tempDir.resolve("users_login.txt");
        AuthService authService = new AuthService(usersFile.toString(), fineService);
        authService.addUser("member", "pass", "MEMBER", "m@example.com");

        fineService.addFine("member", 40);
        Files.deleteIfExists(finesFile);

        Roles loggedIn = authService.login("member", "pass");
        assertNotNull(loggedIn);
        assertTrue(Files.exists(finesFile));
        String persisted = Files.readString(finesFile);
        assertTrue(persisted.contains("member,40"));
    }
}
