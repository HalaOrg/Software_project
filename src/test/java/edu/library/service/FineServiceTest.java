package edu.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
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
    void payFine_reducesBalance_andDoesNotGoNegative() {
        fineService.addFine("bob", 20);

        int remaining = fineService.payFine("bob", 5);
        assertEquals(15, remaining);
        assertEquals(15, fineService.getBalance("bob"));

        remaining = fineService.payFine("bob", 50);
        assertEquals(0, remaining);
        assertEquals(0, fineService.getBalance("bob"));
    }

    @Test
    void getBalance_handlesMissingOrNullUsers() {
        assertEquals(0, fineService.getBalance("unknown"));
        assertEquals(0, fineService.getBalance(null));
    }

    @Test
    void constructor_createsFileIfMissing() throws IOException {
        assertTrue(Files.exists(finesFile));
        assertTrue(Files.size(finesFile) >= 0);
    }
}
