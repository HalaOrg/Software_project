package edu.library.service;

import edu.library.model.BorrowRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BorrowRecordServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void recordBorrow_createsNewRecord() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        LocalDate dueDate = LocalDate.now().plusDays(7);
        service.recordBorrow("user1", "ISBN123", dueDate);

        List<BorrowRecord> records = service.getRecords();
        assertEquals(1, records.size());

        BorrowRecord record = records.get(0);
        assertEquals("user1", record.getUsername());
        assertEquals("ISBN123", record.getIsbn());
        assertEquals(dueDate, record.getDueDate());
        assertFalse(record.isReturned());
        assertNull(record.getReturnDate());
    }

    @Test
    void recordReturn_marksRecordReturned() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        LocalDate dueDate = LocalDate.now().plusDays(7);
        service.recordBorrow("user1", "ISBN123", dueDate);

        LocalDate returnDate = LocalDate.now();
        service.recordReturn("user1", "ISBN123", returnDate);

        BorrowRecord record = service.getRecords().get(0);
        assertTrue(record.isReturned());
        assertEquals(returnDate, record.getReturnDate());
    }

    @Test
    void recordReturn_addsFallbackIfNoActiveBorrow() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        LocalDate returnDate = LocalDate.now();
        service.recordReturn("user2", "ISBN999", returnDate);

        BorrowRecord record = service.getRecords().get(0);
        assertTrue(record.isReturned());
        assertEquals(returnDate, record.getReturnDate());
        assertEquals("user2", record.getUsername());
        assertEquals("ISBN999", record.getIsbn());
    }

    @Test
    void findActiveBorrowRecord_findsCorrectRecord() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        LocalDate dueDate = LocalDate.now().plusDays(5);
        service.recordBorrow("user1", "ISBN1", dueDate);
        service.recordBorrow("user1", "ISBN2", dueDate);

        BorrowRecord r = service.findActiveBorrowRecord("user1", "ISBN2");
        assertNotNull(r);
        assertEquals("ISBN2", r.getIsbn());

        service.recordReturn("user1", "ISBN2", LocalDate.now());
        assertNull(service.findActiveBorrowRecord("user1", "ISBN2"));
    }

    @Test
    void getActiveBorrowRecordsForUser_returnsOnlyActive() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        service.recordBorrow("user1", "ISBN1", LocalDate.now());
        service.recordBorrow("user1", "ISBN2", LocalDate.now());
        service.recordBorrow("user2", "ISBN3", LocalDate.now());

        service.recordReturn("user1", "ISBN1", LocalDate.now());

        List<BorrowRecord> active = service.getActiveBorrowRecordsForUser("user1");
        assertEquals(1, active.size());
        assertEquals("ISBN2", active.get(0).getIsbn());
    }

    @Test
    void hasActiveBorrows_returnsCorrectly() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        assertFalse(service.hasActiveBorrows("user1"));

        service.recordBorrow("user1", "ISBN1", LocalDate.now());
        assertTrue(service.hasActiveBorrows("user1"));

        service.recordReturn("user1", "ISBN1", LocalDate.now());
        assertFalse(service.hasActiveBorrows("user1"));
    }

    @Test
    void loadRecords_createsFileIfMissing() {
        Path file = tempDir.resolve("missing.txt");
        assertFalse(Files.exists(file));

        BorrowRecordService service = new BorrowRecordService(file.toString());
        assertTrue(Files.exists(file));
        assertTrue(service.getRecords().isEmpty());
    }

    @Test
    void loadRecords_skipsMalformedLines() throws IOException {
        Path file = tempDir.resolve("malformed.txt");
        Files.write(file, List.of(
                "short,line", // معيب
                "user1,ISBN1,2025-12-03,false,null" // صحيح
        ));

        BorrowRecordService service = new BorrowRecordService(file.toString());
        List<BorrowRecord> records = service.getRecords();
        assertEquals(1, records.size());
        assertEquals("user1", records.get(0).getUsername());
    }

    @Test
    void appendAndSaveAll_persistsRecordsToFile() throws IOException {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        LocalDate dueDate = LocalDate.now();
        service.recordBorrow("user1", "ISBN1", dueDate);

        // reload
        BorrowRecordService reloaded = new BorrowRecordService(file.toString());
        List<BorrowRecord> records = reloaded.getRecords();
        assertEquals(1, records.size());
        assertEquals("user1", records.get(0).getUsername());
    }

    @Test
    void getAllRecords_returnsCopy() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        service.recordBorrow("user1", "ISBN1", LocalDate.now());
        List<BorrowRecord> list = service.getAllRecords();
        assertEquals(1, list.size());

        list.clear(); // تعديل النسخة لا يؤثر على الأصلي
        assertEquals(1, service.getRecords().size());
    }
    @Test
    void testConstructorIOException() {
        // مسار مستحيل تمُرّره للكونستركتور
        String badPath = "/invalid/path/records.txt";

        assertDoesNotThrow(() -> {
            new BorrowRecordService(badPath);
        });
    }
    @Test
    void testLoadRecordsIOException() {
        String badPath = "/invalid/path/records.txt";
        BorrowRecordService service = new BorrowRecordService(badPath);

        assertDoesNotThrow(() -> {
            service.loadRecords();
        });
    }

}
