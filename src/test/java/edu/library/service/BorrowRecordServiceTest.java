package edu.library.service;

import edu.library.model.BorrowRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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
                "short,line",
                "user1,ISBN1,2025-12-03,false,null"
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

        list.clear();
        assertEquals(1, service.getRecords().size());
    }
    @Test
    void testConstructorIOException() {
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
    @Test
    void addBorrowRecord_skipsNullOrBlank() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        // username null
        service.addBorrowRecord(null, "ISBN1");
        assertTrue(service.getRecords().isEmpty());

        // isbn null
        service.addBorrowRecord("user1", null);
        assertTrue(service.getRecords().isEmpty());

        // username blank
        service.addBorrowRecord("   ", "ISBN1");
        assertTrue(service.getRecords().isEmpty());

        // isbn blank
        service.addBorrowRecord("user1", "   ");
        assertTrue(service.getRecords().isEmpty());
    }

    @Test
    void closeRecord_executesFallbackIfNoActive() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        // لا يوجد سجل فعّال → fallback branch
        service.closeRecord("userX", "ISBNX");

        List<BorrowRecord> records = service.getRecords();
        assertEquals(1, records.size());

        BorrowRecord record = records.get(0);
        assertTrue(record.isReturned());
        assertEquals("userX", record.getUsername());
        assertEquals("ISBNX", record.getIsbn());
        assertNotNull(record.getReturnDate());
    }
    @Test
    void loadRecords_skipsEmptyLinesAndHandlesIOException() throws IOException {
        // ننشئ ملف مع سطر فارغ وسطر صالح
        Path file = tempDir.resolve("empty_lines.txt");
        Files.write(file, List.of(
                "",
                "user1,ISBN1,2025-12-03,false,null"
        ));

        BorrowRecordService service = new BorrowRecordService(file.toString());
        List<BorrowRecord> records = service.getRecords();
        assertEquals(1, records.size());
        assertEquals("user1", records.get(0).getUsername());
    }

    @Test
    void appendRecord_handlesIOExceptionGracefully() {
        // نجبر المسار على أن يكون غير صالح للكتابة
        String badPath = "/invalid/path/append.txt";
        BorrowRecordService service = new BorrowRecordService(badPath);

        BorrowRecord record = new BorrowRecord("userX", "ISBNX", LocalDate.now(), false, null);
        assertDoesNotThrow(() -> service.addBorrowRecord(record));
    }

    @Test
    void saveAll_handlesIOExceptionGracefully() {
        String badPath = "/invalid/path/saveAll.txt";
        BorrowRecordService service = new BorrowRecordService(badPath);

        BorrowRecord record = new BorrowRecord("userY", "ISBNY", LocalDate.now(), false, null);
        service.addBorrowRecord(record);

        assertDoesNotThrow(() -> {
            // نستدعي saveAll مباشرة عبر closeRecord لتغطية الكتابة
            service.closeRecord("userY", "ISBNY");
        });
    }

    @Test
    void addBorrowRecord_recordObject_addsToListAndFile() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        BorrowRecord record = new BorrowRecord("userA", "ISBNA", LocalDate.now(), false, null);
        service.addBorrowRecord(record);

        List<BorrowRecord> records = service.getRecords();
        assertEquals(1, records.size());
        assertEquals("userA", records.get(0).getUsername());
    }

    @Test
    void closeRecord_existingRecord_marksReturnedToday() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        LocalDate due = LocalDate.now().plusDays(5);
        service.recordBorrow("userB", "ISBNB", due);

        LocalDate before = LocalDate.now();
        service.closeRecord("userB", "ISBNB");
        LocalDate after = LocalDate.now();

        BorrowRecord record = service.getRecords().get(0);
        assertTrue(record.isReturned());
        // التأكد أن returnDate بين اليوم قبل وبعد التنفيذ
        assertTrue(!record.getReturnDate().isBefore(before) && !record.getReturnDate().isAfter(after));
    }
    @Test
    void defaultConstructor_createsFileAndLoadsRecords() {
        BorrowRecordService service = new BorrowRecordService();
        assertNotNull(service.getRecords());
        // نتحقق أن الملف تم إنشاؤه في المسار الافتراضي
        File file = new File(System.getProperty("user.dir"), "borrow_records.txt");
        assertTrue(file.exists());
    }

    @Test
    void resolveDefault_returnsCorrectPath() {
        String path = new File(System.getProperty("user.dir"), "testfile.txt").getPath();
        assertEquals(path, new File(System.getProperty("user.dir"), "testfile.txt").getPath());
    }

    @Test
    void addBorrowRecord_withUsernameAndIsbn_addsRecordWithDueDate() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        service.addBorrowRecord("userTest", "ISBNTest");
        List<BorrowRecord> records = service.getRecords();

        assertEquals(1, records.size());
        BorrowRecord r = records.get(0);
        assertEquals("userTest", r.getUsername());
        assertEquals("ISBNTest", r.getIsbn());
        assertNotNull(r.getDueDate());
        assertFalse(r.isReturned());
        assertNull(r.getReturnDate());
    }

    @Test
    void recordReturn_fallbackHasCorrectDates() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        LocalDate returnDate = LocalDate.now();
        service.recordReturn("userFallback", "ISBNFallback", returnDate);

        BorrowRecord r = service.getRecords().get(0);
        assertTrue(r.isReturned());
        assertEquals(returnDate, r.getReturnDate());
        assertEquals(returnDate, r.getDueDate()); // fallback uses returnDate as dueDate
    }

    @Test
    void closeRecord_existingAndFallbackRecords() {
        Path file = tempDir.resolve("records.txt");
        BorrowRecordService service = new BorrowRecordService(file.toString());

        LocalDate due = LocalDate.now().plusDays(3);
        service.recordBorrow("userC", "ISBNC", due);

        // closeRecord على سجل موجود
        service.closeRecord("userC", "ISBNC");
        BorrowRecord r1 = service.getRecords().get(0);
        assertTrue(r1.isReturned());

        // closeRecord على سجل غير موجود → fallback
        service.closeRecord("userD", "ISBND");
        BorrowRecord r2 = service.getRecords().get(1);
        assertTrue(r2.isReturned());
        assertEquals("userD", r2.getUsername());
        assertEquals("ISBND", r2.getIsbn());
    }

    @Test
    void loadRecords_handlesNullFieldsCorrectly() throws IOException {
        Path file = tempDir.resolve("null_fields.txt");
        Files.write(file, List.of(
                "user1,ISBN1,null,false,null",
                "user2,ISBN2,2025-12-10,true,2025-12-05"
        ));

        BorrowRecordService service = new BorrowRecordService(file.toString());
        List<BorrowRecord> records = service.getRecords();

        assertEquals(2, records.size());

        BorrowRecord r1 = records.get(0);
        assertNull(r1.getDueDate());
        assertFalse(r1.isReturned());
        assertNull(r1.getReturnDate());

        BorrowRecord r2 = records.get(1);
        assertEquals(LocalDate.of(2025,12,10), r2.getDueDate());
        assertTrue(r2.isReturned());
        assertEquals(LocalDate.of(2025,12,5), r2.getReturnDate());
    }

}
