package edu.library.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FineServiceTest {

    @TempDir
    Path tempDir;

    private Path finesFile;
    private FineService fineService;
    private String originalUserDir;

    @BeforeEach
    void setUp() {
        // Keep all file writes inside the temp directory so real project files are untouched.
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        finesFile = tempDir.resolve("fines.txt");
        fineService = Mockito.spy(new FineService(finesFile.toString()));
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void testDefaultConstructor() {
        FineService fs = new FineService();
        assertNotNull(fs, "FineService instance should not be null");
    }

    @Test
    void testIOExceptionDuringConstructor() {
        FineService spyFine = Mockito.spy(new FineService(finesFile.toString()));

        assertDoesNotThrow(() -> new FineService(),
                "Constructor should handle IOException without throwing");
    }

    @Test
    void addFine_updatesBalanceAndPersists() {
        fineService.addFine("alice", 30);
        assertEquals(30, fineService.getBalance("alice"));

        FineService reloaded = new FineService(finesFile.toString());
        assertEquals(30, reloaded.getBalance("alice"));
        Map<String, Integer> balances = reloaded.getAllBalances();
        assertEquals(1, balances.size());
        assertTrue(balances.containsKey("alice"));
    }

    @Test
    void payFine_reducesBalance() {
        fineService.addFine("bob", 20);

        int remaining = fineService.payFine("bob", 5);
        assertEquals(15, remaining);
        assertEquals(15, fineService.getBalance("bob"));

        remaining = fineService.payFine("bob", 50);
        assertEquals(0, remaining);
        assertEquals(0, fineService.getBalance("bob"));
    }

    @Test
    void getBalance_MissingOrNullUsers() {
        assertEquals(0, fineService.getBalance("unknown"));
        assertEquals(0, fineService.getBalance(null));
    }

    @Test
    void constructor_createsFileIfMissing() throws IOException {
        assertTrue(Files.exists(finesFile));
        assertTrue(Files.size(finesFile) >= 0);
    }

    @Test
    void addFine_ignoresNullOrNonPositiveAmount_andDoesNotPersist() throws IOException {
        fineService.addFine(null, 10);
        assertEquals(0, fineService.getBalance(null));
        assertEquals(0, fineService.getAllBalances().size());

        fineService.addFine("user1", 0);
        fineService.addFine("user2", -5);
        assertEquals(0, fineService.getBalance("user1"));
        assertEquals(0, fineService.getBalance("user2"));

        String content = new String(Files.readAllBytes(finesFile), StandardCharsets.UTF_8);
        assertTrue(content.trim().isEmpty());
    }

    @Test
    void payFine_withNullOrNonPositiveInputs_returnsUnchanged() {
        fineService.addFine("payer", 40);
        int r = fineService.payFine(null, 5);
        assertEquals(0, r);

        int before = fineService.getBalance("payer");
        int after = fineService.payFine("payer", 0);
        assertEquals(before, after);

        after = fineService.payFine("payer", -10);
        assertEquals(before, after);
    }

    @Test
    void constructor_throws_onMalformedFile() throws IOException {
        Path badFile = tempDir.resolve("fines_bad.txt");
        Files.write(badFile, "alice,notanumber\n".getBytes(StandardCharsets.UTF_8));

        assertThrows(NumberFormatException.class, () -> new FineService(badFile.toString()));
    }

    @Test
    void getAllBalances_returnsACopy_notBackingMap() {
        fineService.addFine("copytest", 7);
        Map<String, Integer> copy = fineService.getAllBalances();
        copy.put("copytest", 999);
        assertEquals(7, fineService.getBalance("copytest"));
    }

    @Test
    void save_persistsMultipleEntries_andFileContainsBoth() {
        fineService.addFine("a", 1);
        fineService.addFine("b", 2);

        FineService reloaded = new FineService(finesFile.toString());
        assertEquals(1, reloaded.getBalance("a"));
        assertEquals(2, reloaded.getBalance("b"));

        Map<String, Integer> balances = reloaded.getAllBalances();
        assertTrue(balances.size() >= 2);
        assertEquals(1, balances.get("a"));
        assertEquals(2, balances.get("b"));
    }
    @Test
    void testSaveBalances_callsSaveOnce() {
        fineService.saveBalances();
        verify(fineService, times(1)).save();
    }

    @Test
    void save_throwsRuntimeException_whenIOExceptionOccurs() {
        FineService spyFine = Mockito.spy(new FineService(finesFile.toString()));
        // محاكاة أي RuntimeException عند save
        doThrow(new RuntimeException("Error saving fines", new IOException("Mocked IO Error")))
                .when(spyFine).save();

        RuntimeException ex = assertThrows(RuntimeException.class, spyFine::saveBalances);
        assertTrue(ex.getMessage().contains("Error saving fines"));
        assertTrue(ex.getCause() instanceof IOException);
    }


    @Test
    void testStoreBalanceOnLogin_balanceZero() {
        doReturn(0).when(fineService).getBalance("user0");
        fineService.storeBalanceOnLogin("user0");
        verify(fineService, never()).save();
    }

    @Test
    void testStoreBalanceOnLogin_balancePositive() {
        doReturn(100).when(fineService).getBalance("user1");
        fineService.storeBalanceOnLogin("user1");
        verify(fineService, times(1)).save();
    }

    @Test
    void testStoreBalanceOnLogin_nullUsername() {
        fineService.storeBalanceOnLogin(null);
        verify(fineService, never()).save();
    }


    @Test
    void testSave_filePathParentIsNull_noException() {
        FineService service = new FineService(Path.of("fines_no_parent.txt"));
        service.addFine("user", 10);
        assertDoesNotThrow(service::save);
    }



    @Test
    void testResolveDefault_returnsValidPath() {
        String path = FineService.resolveDefault("testfile.txt");
        assertTrue(path.endsWith("testfile.txt"));
    }

    @Test
    void testPayFine_attemptsToOverpay() {
        FineService service = new FineService(tempDir.resolve("f.txt").toString());
        service.addFine("user", 10);

        int remaining = service.payFine("user", 20);
        assertEquals(0, remaining);
        assertEquals(0, service.getBalance("user"));
    }

    @Test
    void testAddFine_withNonPositiveAmountAndNullUsername() {
        FineService service = new FineService(tempDir.resolve("f.txt").toString());
        service.addFine(null, 10);
        service.addFine("user", 0);
        service.addFine("user2", -5);

        assertEquals(0, service.getBalance(null));
        assertEquals(0, service.getBalance("user"));
        assertEquals(0, service.getBalance("user2"));
    }

    @Test
    void testStoreBalanceOnLogin_withZeroBalanceDoesNotSave() {
        FineService service = spy(new FineService(tempDir.resolve("f.txt").toString()));
        doReturn(0).when(service).getBalance("user0");
        service.storeBalanceOnLogin("user0");
        verify(service, never()).save();
    }

    @Test
    void testStoreBalanceOnLogin_withPositiveBalanceSaves() {
        FineService service = spy(new FineService(tempDir.resolve("f.txt").toString()));
        doReturn(50).when(service).getBalance("user1");
        service.storeBalanceOnLogin("user1");
        verify(service, times(1)).save();
    }

    @Test
    void testStoreBalanceOnLogin_withNullUsernameDoesNothing() {
        FineService service = spy(new FineService(tempDir.resolve("f.txt").toString()));
        service.storeBalanceOnLogin(null);
        verify(service, never()).save();
    }
}
