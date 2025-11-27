package edu.library.service;

import edu.library.model.BorrowRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BorrowRecordServiceTest {

    @TempDir
    Path tempDir;

    private Path recordsFile;
    private BorrowRecordService service;

    @BeforeEach
    void setup() {
        recordsFile = tempDir.resolve("borrow_records.txt");
        service = new BorrowRecordService(recordsFile.toString());
    }

    @Test
    void recordBorrow_addsActiveRecordAndPersists() throws IOException {
        LocalDate dueDate = LocalDate.now().plusDays(10);
        service.recordBorrow("user1", "ISBN-1", dueDate);

        List<BorrowRecord> records = service.getRecords();
        assertEquals(1, records.size());
        BorrowRecord record = records.get(0);
        assertEquals("user1", record.getUsername());
        assertEquals("ISBN-1", record.getIsbn());
        assertEquals(dueDate, record.getDueDate());
        assertFalse(record.isReturned());

        List<String> lines = Files.readAllLines(recordsFile);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("user1"));
        assertTrue(lines.get(0).contains("ISBN-1"));
    }

    @Test
    void findActiveBorrowRecord_andRecordReturn_updatesStateAndSaves() {
        LocalDate dueDate = LocalDate.now().plusDays(5);
        service.recordBorrow("user1", "ISBN-2", dueDate);

        BorrowRecord active = service.findActiveBorrowRecord("user1", "ISBN-2");
        assertNotNull(active);
        assertFalse(active.isReturned());

        LocalDate returnDate = LocalDate.now();
        service.recordReturn("user1", "ISBN-2", returnDate);

        BorrowRecord updated = service.findActiveBorrowRecord("user1", "ISBN-2");
        assertNull(updated);
        List<BorrowRecord> all = service.getRecords();
        assertEquals(1, all.size());
        assertTrue(all.get(0).isReturned());
        assertEquals(returnDate, all.get(0).getReturnDate());

        BorrowRecordService reloaded = new BorrowRecordService(recordsFile.toString());
        assertTrue(reloaded.getRecords().get(0).isReturned());
    }

    @Test
    void recordReturn_withoutExistingBorrow_createsReturnedRecord() {
        LocalDate returnDate = LocalDate.now();
        service.recordReturn("user2", "ISBN-3", returnDate);

        List<BorrowRecord> records = service.getRecords();
        assertEquals(1, records.size());
        BorrowRecord record = records.get(0);
        assertEquals("user2", record.getUsername());
        assertEquals("ISBN-3", record.getIsbn());
        assertTrue(record.isReturned());
        assertEquals(returnDate, record.getReturnDate());
    }

    @Test
    void getActiveBorrowRecordsForUser_filtersReturnedOnes() {
        LocalDate now = LocalDate.now();
        service.recordBorrow("user3", "ISBN-A", now.plusDays(7));
        service.recordBorrow("user3", "ISBN-B", now.plusDays(1));
        service.recordReturn("user3", "ISBN-A", now.plusDays(2));

        List<BorrowRecord> active = service.getActiveBorrowRecordsForUser("user3");
        assertEquals(1, active.size());
        assertEquals("ISBN-B", active.get(0).getIsbn());
    }

    @Test
    void constructor_createsFileIfMissing() {
        assertTrue(Files.exists(recordsFile));
    }
}
