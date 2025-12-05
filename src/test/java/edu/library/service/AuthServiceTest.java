package edu.library.service;

import edu.library.model.BorrowRecord;
import edu.library.model.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;
    private FineService fineService;
    private BorrowRecordService mockBorrow;

    @TempDir
    Path tempDir;
    private Path usersFile;

    @BeforeEach
    void setUp() throws IOException {
        fineService = mock(FineService.class);
        mockBorrow = mock(BorrowRecordService.class);

        usersFile = tempDir.resolve("users.txt");
        Files.createFile(usersFile);

        authService = new AuthService(usersFile.toString(), fineService);

        authService.addUser("admin", "adminpwd", "ADMIN", "admin@example.com");
        authService.addUser("member1", "pwd1", "MEMBER", "member1@example.com");
        authService.addUser("member2", "pwd2", "MEMBER", "member2@example.com");
    }

    @Test
    void testRemoveUserWithRestrictions() {
        authService.login("admin", "adminpwd");

        authService.logout();
        assertFalse(authService.removeUserWithRestrictions("member1", mockBorrow));
        authService.login("admin", "adminpwd");

        assertFalse(authService.removeUserWithRestrictions(null, mockBorrow));
        assertFalse(authService.removeUserWithRestrictions("admin", mockBorrow));

        when(fineService.getBalance("member1")).thenReturn(50);
        assertFalse(authService.removeUserWithRestrictions("member1", mockBorrow));

        when(fineService.getBalance("member2")).thenReturn(0);
        BorrowRecord fakeRecord = mock(BorrowRecord.class);
        when(mockBorrow.getActiveBorrowRecordsForUser("member2"))
                .thenReturn(Collections.singletonList(fakeRecord));
        assertFalse(authService.removeUserWithRestrictions("member2", mockBorrow));

        authService.addUser("member3", "pwd3", "MEMBER", "member3@example.com");
        when(fineService.getBalance("member3")).thenReturn(0);
        when(mockBorrow.getActiveBorrowRecordsForUser("member3"))
                .thenReturn(Collections.emptyList());

        assertTrue(authService.removeUserWithRestrictions("member3", mockBorrow));
        assertFalse(authService.userExists("member3"));
    }

    @Test
    void testDefaultConstructor() {
        String oldDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            AuthService defaultAuth = new AuthService();
            assertNotNull(defaultAuth);
            assertTrue(Files.exists(tempDir.resolve("users.txt")));
        } finally {
            System.setProperty("user.dir", oldDir);
        }
    }

    @Test
    void testConstructorWithFineService() {
        String oldDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            AuthService auth = new AuthService(fineService);
            assertNotNull(auth);
            assertTrue(Files.exists(tempDir.resolve("users.txt")));
        } finally {
            System.setProperty("user.dir", oldDir);
        }
    }

    @Test
    void testAddUserOverload() {
        Roles role = authService.addUser("user1", "pass1", "MEMBER", "user1@example.com");
        assertNotNull(role);

        assertEquals("user1", role.getUsername());
        assertEquals("MEMBER", role.getRoleName());
        assertEquals("user1@example.com", role.getEmail());
    }

    @Test
    void testResolveDefaultViaConstructor() throws IOException {
        Path fileInsideTemp = tempDir.resolve("users_resolve.txt");
        assertFalse(Files.exists(fileInsideTemp));
        AuthService auth = new AuthService(fileInsideTemp.toString());
        assertTrue(Files.exists(fileInsideTemp));
    }

    @Test
    void testAddUserAndCatchIOExceptions() {
        Path badPath = tempDir.resolve("invalid_dir/users.txt");
        AuthService auth = new AuthService(badPath.toString());
        assertDoesNotThrow(() -> auth.addUser("userX", "pwdX", "MEMBER"));
    }

}
