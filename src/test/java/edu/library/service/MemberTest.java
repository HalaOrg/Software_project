package edu.library.service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import edu.library.model.Book;
import edu.library.model.CD;
import edu.library.fine.FineCalculator;
import edu.library.time.TimeProvider;
import edu.library.model.BorrowRecord;
import edu.library.model.Roles;
import org.junit.jupiter.api.*;
import java.io.*;


class MemberTest {

    private MediaService service;
    private AuthService auth;
    private Roles user;

    @TempDir
    Path tempDir;          // فولدر مؤقت لكل تيست

    private Path fineFile;         // ملف الغرامات المؤقت
    private Path borrowFile;       // ملف الـ borrow records المؤقت

    @BeforeEach
    void setup() throws IOException {
        // --- تجهيز فايلات داخل tempDir ---
        Path mediaFile = tempDir.resolve("media_test.txt");
        Files.createFile(mediaFile);

        borrowFile = tempDir.resolve("borrow_records_test.txt");
        Files.createFile(borrowFile);

        fineFile = tempDir.resolve("fines.txt");
        Files.createFile(fineFile);

        // --- BorrowRecordService/FineService/TimeProvider على فايلات مؤقتة ---
        BorrowRecordService borrowRecordService =
                new BorrowRecordService(borrowFile.toString());   // مهم: مسار مؤقت

        FineService fineService = new FineService();
        TimeProvider timeProvider = mock(TimeProvider.class);
        when(timeProvider.today()).thenReturn(LocalDate.of(2025, 12, 3));

        service = new MediaService(
                mediaFile.toString(),
                borrowRecordService,
                fineService,
                timeProvider,
                new FineCalculator()
        );

        // --- AuthService + المستخدم ---
        auth = mock(AuthService.class);
        user = new Roles("testuser", "Test User", "test@example.com", "Member");
        when(auth.logout()).thenReturn(true);

        // توجيه Member ليستخدم ملف الغرامات المؤقت
        Member.fineFilePath = fineFile.toString();

        // --- إضافة كتب و CDs ---
        service.addMedia(new Book("Java Programming", "Author A", "ISBN001", 1));
        service.addMedia(new Book("Python Basics", "Author B", "ISBN002", 1));
        service.addMedia(new CD("Java CD", "Author A", "CD001", 1));
        service.addMedia(new CD("Python CD", "Author B", "CD002", 1));
    }

    @AfterEach
    void cleanup() throws IOException {
        // مش ضروري بس زيادة أمان
        if (fineFile != null) Files.deleteIfExists(fineFile);
        if (borrowFile != null) Files.deleteIfExists(borrowFile);
    }

    // ----------- Helpers ----------------
    private int runMemberWithInput(String inputStr) {
        return Member.handle(
                new java.util.Scanner(new ByteArrayInputStream(inputStr.getBytes())),
                service,
                auth,
                user
        );
    }

    private void writeFine(String username, int amount) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fineFile.toFile()))) {
            bw.write(username + "," + amount);
        }
    }

    // ----------------------------- //
    // Test Search Book
    // ----------------------------- //
    @Test
    void testSearchBookFoundAndNotFound() {
        assertEquals(0, runMemberWithInput("1\nJava\n"));
        assertEquals(0, runMemberWithInput("1\nNonExisting\n"));
    }

    // ----------------------------- //
    // Borrow Book
    // ----------------------------- //
    @Test
    void testBorrowBook() {
        // borrow success
        assertEquals(0, runMemberWithInput("2\nISBN001\n"));
        assertFalse(service.findBookByIsbn("ISBN001").isAvailable());

        // borrow not found
        assertEquals(0, runMemberWithInput("2\nINVALID\n"));

        // borrow not available
        assertEquals(0, runMemberWithInput("2\nISBN001\n"));
    }

    // ----------------------------- //
    // Display All Books
    // ----------------------------- //
    @Test
    void testDisplayAllBooks() {
        assertEquals(0, runMemberWithInput("4\n"));
    }

    // ----------------------------- //
    // Pay Fines
    // ----------------------------- //
    @Test
    void testPayFines() throws IOException {
        // بدون غرامات
        assertEquals(0, runMemberWithInput("5\n"));

        // مع غرامات
        writeFine("testuser", 50);
        String input = "5\n30\n";
        assertEquals(0, runMemberWithInput(input));
    }

    // ----------------------------- //
    // Remaining Time for Books
    // ----------------------------- //
    @Test
    void testRemainingTimeBooks() {
        service.borrowBook(service.findBookByIsbn("ISBN001"), user.getUsername());
        assertEquals(0, runMemberWithInput("6\n"));
    }

    // ----------------------------- //
    // Search CD
    // ----------------------------- //
    @Test
    void testSearchCD() {
        assertEquals(0, runMemberWithInput("7\nJava\n"));
        assertEquals(0, runMemberWithInput("7\nNonExist\n"));
    }

    // ----------------------------- //
    // Borrow CD
    // ----------------------------- //
    @Test
    void testBorrowCD() {
        // borrow success
        assertEquals(0, runMemberWithInput("8\nCD001\n"));
        assertFalse(service.findCDByIsbn("CD001").isAvailable());

        // borrow not found
        assertEquals(0, runMemberWithInput("8\nINVALID\n"));

        // borrow not available
        assertEquals(0, runMemberWithInput("8\nCD001\n"));
    }

    // ----------------------------- //
    // Display All CDs
    // ----------------------------- //
    @Test
    void testDisplayAllCDs() {
        assertEquals(0, runMemberWithInput("10\n"));
    }

    // ----------------------------- //
    // Remaining Time for CDs
    // ----------------------------- //
    @Test
    void testRemainingTimeCDs() {
        service.borrowCD(service.findCDByIsbn("CD001"), user.getUsername());
        assertEquals(0, runMemberWithInput("11\n"));
    }

    // ----------------------------- //
    // Logout
    // ----------------------------- //
    @Test
    void testLogout() {
        assertEquals(1, runMemberWithInput("12\n"));
        verify(auth, times(1)).logout();
    }

    // ----------------------------- //
    // Exit
    // ----------------------------- //
    @Test
    void testExit() {
        assertEquals(2, runMemberWithInput("13\n"));
    }

    // ----------------------------- //
    // Invalid Inputs
    // ----------------------------- //
    @Test
    void testInvalidOptionAndNonNumber() {
        assertEquals(0, runMemberWithInput("99\n"));
        assertEquals(0, runMemberWithInput("abc\n"));
    }
}