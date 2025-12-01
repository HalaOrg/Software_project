package edu.library.service;

import edu.library.model.*;
import edu.library.fine.FineCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MediaServiceTest {

    private MediaService mediaService;
    private String username = "testUser";

    @BeforeEach
    void setUp() {
        // إنشاء MediaService بدون ملف حقيقي
        mediaService = new MediaService("test_media.txt",
                new BorrowRecordService(),
                new FineService(),
                () -> LocalDate.of(2025, 12, 1), // ثابت للتواريخ
                new FineCalculator());

        // إضافة بعض الكتب وCDs
        mediaService.addMedia(new Book("Java Programming", "Author A", "B001", 2, 2));
        mediaService.addMedia(new Book("Python Basics", "Author B", "B002", 1, 1));
        mediaService.addMedia(new CD("Best of 2020", "DJ C", "C001", 1, 1));
        mediaService.addMedia(new CD("Top Hits", "DJ D", "C002", 2, 2));
    }

    @Test
    void testSearchBook() {
        List<Book> books = mediaService.searchBook("java");
        assertEquals(1, books.size());
        assertEquals("B001", books.get(0).getIsbn());

        books = mediaService.searchBook("Author B");
        assertEquals(1, books.size());
        assertEquals("B002", books.get(0).getIsbn());

        books = mediaService.searchBook("NotExist");
        assertTrue(books.isEmpty());
    }

    @Test
    void testSearchCD() {
        List<CD> cds = mediaService.searchCD("best");
        assertEquals(1, cds.size());
        assertEquals("C001", cds.get(0).getIsbn());

        cds = mediaService.searchCD("DJ D");
        assertEquals(1, cds.size());
        assertEquals("C002", cds.get(0).getIsbn());

        cds = mediaService.searchCD("None");
        assertTrue(cds.isEmpty());
    }

    @Test
    void testBorrowAndReturnBook() {
        Book book = mediaService.findBookByIsbn("B001");
        assertTrue(mediaService.borrowBook(book, username));
        assertEquals(1, book.getAvailableCopies());
        assertNotNull(book.getDueDate());

        assertTrue(mediaService.returnBook(book, username));
        assertEquals(2, book.getAvailableCopies());
        assertNull(book.getDueDate());
    }

    @Test
    void testBorrowAndReturnCD() {
        CD cd = mediaService.findCDByIsbn("C002");
        assertTrue(mediaService.borrowCD(cd, username));
        assertEquals(1, cd.getAvailableCopies());
        assertNotNull(cd.getDueDate());

        assertTrue(mediaService.returnCD(cd, username));
        assertEquals(2, cd.getAvailableCopies());
        assertNull(cd.getDueDate());
    }

    @Test
    void testGetActiveBorrowRecords() {
        Book book = mediaService.findBookByIsbn("B001");
        CD cd = mediaService.findCDByIsbn("C001");

        mediaService.borrowBook(book, username);
        mediaService.borrowCD(cd, username);

        List<BorrowRecord> activeRecords = mediaService.getActiveBorrowRecordsForUser(username);
        assertEquals(2, activeRecords.size());
    }

    @Test
    void testOutstandingFineAfterLateReturn() {
        Book book = mediaService.findBookByIsbn("B002");

        mediaService.borrowBook(book, username);

        // محاكاة إرجاع بعد مدة التأخير (15 يوم)
        BorrowRecord record = mediaService.getActiveBorrowRecordsForUser(username).get(0);
        LocalDate lateReturn = record.getDueDate().plusDays(15);
        mediaService.returnBook(book, username);

        int fine = mediaService.getOutstandingFine(username);
        assertEquals(150, fine); // 15 يوم * 10 NIS/يوم للكتاب
    }

    @Test
    void testGetBooksAndCDs() {
        List<Book> books = mediaService.getBooks();
        assertEquals(2, books.size());

        List<CD> cds = mediaService.getCDs();
        assertEquals(2, cds.size());
    }
}
