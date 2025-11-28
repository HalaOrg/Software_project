package edu.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FineServiceTest {

    @TempDir
    Path tempDir;

    private Path finesFile;
    private FineService fineService;

    @BeforeEach
    void setup() {
        finesFile = tempDir.resolve("fines.txt");
        fineService = new FineService(finesFile.toString());
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

    // --- New tests to improve branch coverage ---

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
        // نضيف غرامتين
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

}
