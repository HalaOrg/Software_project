package edu.library;

import edu.library.fine.FineCalculator;
import edu.library.model.Book;
import edu.library.service.AuthService;
import edu.library.service.MediaService;
import edu.library.service.BorrowRecordService;
import edu.library.service.FineService;
import edu.library.time.TimeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceRestrictionsTest {

    @TempDir
    Path tempDir;

    @Test
    void preventsBorrowWhenOverdueRecordExists() {
        LocalDate today = LocalDate.of(2024, 1, 30);
        TimeProvider timeProvider = () -> today;

        BorrowRecordService borrowRecordService = new BorrowRecordService(tempDir.resolve("borrow_records.txt").toString());
        FineService fineService = new FineService(tempDir.resolve("fines.txt").toString());
        MediaService bookService = new MediaService(
                tempDir.resolve("media.txt").toString(),
                borrowRecordService,
                fineService,
                timeProvider,
                new FineCalculator()
        );

        Book available = new Book("Clean Code", "Robert Martin", "ISBN-1001");
        bookService.addBook(available);

        borrowRecordService.recordBorrow("alice", "ISBN-OLD", today.minusDays(2));

        boolean borrowed = bookService.borrowBook(bookService.findBookByIsbn("ISBN-1001"), "alice");

        assertFalse(borrowed, "Borrow should be blocked while overdue loans exist.");
        assertEquals(1, bookService.findBookByIsbn("ISBN-1001").getAvailableCopies());
    }

    @Test
    void allowsBorrowWhenNoOverdueOrFines() {
        LocalDate today = LocalDate.of(2024, 2, 1);
        TimeProvider timeProvider = () -> today;

        BorrowRecordService borrowRecordService = new BorrowRecordService(tempDir.resolve("borrow_records.txt").toString());
        FineService fineService = new FineService(tempDir.resolve("fines.txt").toString());
        MediaService bookService = new MediaService(
                tempDir.resolve("media.txt").toString(),
                borrowRecordService,
                fineService,
                timeProvider,
                new FineCalculator()
        );

        Book available = new Book("Domain-Driven Design", "Eric Evans", "ISBN-1002");
        bookService.addBook(available);

        boolean borrowed = bookService.borrowBook(bookService.findBookByIsbn("ISBN-1002"), "bob");

        assertTrue(borrowed, "Borrow should succeed when there are no blocks.");
        assertEquals(0, bookService.findBookByIsbn("ISBN-1002").getAvailableCopies());
        assertEquals(today.plusDays(28), bookService.findBookByIsbn("ISBN-1002").getDueDate());
    }

    @Test
    void cannotUnregisterUserWithActiveLoansOrFines() {
        LocalDate today = LocalDate.of(2024, 3, 1);
        TimeProvider timeProvider = () -> today;

        BorrowRecordService borrowRecordService = new BorrowRecordService(tempDir.resolve("borrow_records.txt").toString());
        FineService fineService = new FineService(tempDir.resolve("fines.txt").toString());
        AuthService authService = new AuthService(tempDir.resolve("users.txt").toString(), fineService);

        authService.addUser("admin", "pass", "ADMIN", "admin@example.com");
        authService.addUser("member", "pass", "MEMBER", "member@example.com");
        authService.login("admin", "pass");

        borrowRecordService.recordBorrow("member", "ISBN-2001", today.plusDays(7));

        assertFalse(authService.removeUserWithRestrictions("member", borrowRecordService), "Active loans must block removal.");

        borrowRecordService.recordReturn("member", "ISBN-2001", today.plusDays(1));
        fineService.addFine("member", 20);

        assertFalse(authService.removeUserWithRestrictions("member", borrowRecordService), "Outstanding fines must block removal.");

        fineService.payFine("member", 20);

        assertTrue(authService.removeUserWithRestrictions("member", borrowRecordService), "User should be removable once clear.");
    }
}

