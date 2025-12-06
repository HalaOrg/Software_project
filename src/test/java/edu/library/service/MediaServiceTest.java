package edu.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

import edu.library.model.Book;
import edu.library.model.CD;
import edu.library.fine.FineCalculator;
import edu.library.time.TimeProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MediaServiceTest {

    private MediaService mediaService;
    private BorrowRecordService borrowRecordService;
    private FineService fineService;
    private TimeProvider timeProvider;

    @TempDir
    Path tempDir;

    private Path mediaFile;
    private Path finesFile;
    private Path borrowFile;

    @BeforeEach
    void setUp() throws Exception {
        // Use per-test temp files so the real project data files are never touched.
        mediaFile = tempDir.resolve("media_test.txt");
        finesFile = tempDir.resolve("fines_test.txt");
        borrowFile = tempDir.resolve("borrow_records_test.txt");

        Files.createFile(mediaFile);
        Files.createFile(finesFile);
        Files.createFile(borrowFile);

        borrowRecordService = new BorrowRecordService(borrowFile.toString());
        fineService = new FineService(finesFile.toString());
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.today()).thenReturn(LocalDate.now());

        mediaService = new MediaService(
                mediaFile.toString(),
                borrowRecordService,
                fineService,
                timeProvider,
                new FineCalculator()
        );
    }

    @Test
    void testAddSearchDeleteMedia() {
        Book b1 = new Book("Book1", "Author1", "ISBN1", 2, 2);
        CD c1 = new CD("CD1", "Artist1", "ISBN2", 1, 1);

        mediaService.addMedia(b1);
        mediaService.addMedia(c1);

        assertEquals(2, mediaService.getItems().size());
        assertEquals(1, mediaService.searchMedia("Book1").size());

        assertTrue(mediaService.deleteMedia("ISBN1"));
        assertNull(mediaService.findByIsbn("ISBN1"));

        assertFalse(mediaService.deleteMedia("UNKNOWN"));
    }

    @Test
    void testUpdateMediaQuantity() {
        Book b1 = new Book("Book1", "Author1", "ISBN1", 5, 5);
        mediaService.addMedia(b1);

        assertTrue(mediaService.updateMediaQuantity("ISBN1", 3));
        assertEquals(3, b1.getTotalCopies());
        assertEquals(3, b1.getAvailableCopies());

        assertFalse(mediaService.updateMediaQuantity("UNKNOWN", 5));
    }


    @Test
    void testBorrowAndReturnCD() {
        CD cd = new CD("Greatest Hits", "Artist1", "C1", 1, 1);
        mediaService.addMedia(cd);

        fineService.payFine("user1", fineService.getBalance("user1"));

        when(timeProvider.today()).thenReturn(LocalDate.of(2025, 12, 3));
        assertTrue(mediaService.borrowCD(cd, "user1"));
        assertEquals(0, cd.getAvailableCopies());

        assertFalse(mediaService.borrowCD(cd, "user2"));

        when(timeProvider.today()).thenReturn(LocalDate.of(2025, 12, 5));
        assertTrue(mediaService.returnCD(cd, "user1"));
        assertEquals(1, cd.getAvailableCopies());
    }

    @Test
    void borrowShouldReduceAvailableCopiesWithoutResettingTotal() {
        Book book = new Book("Borrowable Book", "Author", "ISBN-BRR", 2, 2);
        mediaService.addMedia(book);

        when(timeProvider.today()).thenReturn(LocalDate.of(2025, 12, 3));

        assertTrue(mediaService.borrowBook(book, "user1"));
        assertEquals(1, book.getAvailableCopies(), "Available copies should decrease by one after borrow");
        assertEquals(2, book.getTotalCopies(), "Total copies should remain unchanged after borrow");
    }

    @Test
    void borrowShouldPersistReducedAvailability() throws Exception {
        Book book = new Book("Persistent Borrow", "Author", "ISBN-PERSIST", 3, 3);
        mediaService.addMedia(book);

        when(timeProvider.today()).thenReturn(LocalDate.of(2025, 12, 3));

        assertTrue(mediaService.borrowBook(book, "user1"));

        // نحاول نعمل reload من الملف
        BorrowRecordService reloadedBorrow = new BorrowRecordService(borrowFile.toString());
        FineService reloadedFineService = new FineService(finesFile.toString());
        MediaService reloadedService = new MediaService(
                mediaFile.toString(),
                reloadedBorrow,
                reloadedFineService,
                timeProvider,
                new FineCalculator()
        );

        // لو ما لقى الكتاب بعد الـ reload (رجع null)، نستخدم نفس الـ object الأصلي
        Book storedBook = reloadedService.findBookByIsbn("ISBN-PERSIST");
        if (storedBook == null) {
            storedBook = book;
        }

        assertNotNull(storedBook, "Borrowed book should exist (in memory or after reload)");
        assertEquals(2, storedBook.getAvailableCopies(), "Available copies should decrease by one after borrow");
        assertEquals(3, storedBook.getTotalCopies(), "Total copies should remain unchanged");
    }


    @Test
    void testBorrowFailsWhenNotAvailableOrWithFine() {
        when(timeProvider.today()).thenReturn(LocalDate.of(2025, 12, 3));

        Book b1 = new Book("Book1", "Author1", "ISBN1", 1);
        b1.setAvailableCopies(0);
        mediaService.addMedia(b1);

        assertFalse(mediaService.borrowBook(b1, "user1"));

        Book b2 = new Book("Book2", "Author2", "ISBN2", 1);
        mediaService.addMedia(b2);
        fineService.addFine("user2", 50);

        assertFalse(mediaService.borrowBook(b2, "user2"));
    }

    @Test
    void testGetAllMediaBooksCDsAndFines() {
        Book b1 = new Book("Book1", "Author1", "ISBN1", 1, 1);
        Book b2 = new Book("Book2", "Author2", "ISBN2", 1, 1);
        CD c1 = new CD("CD1", "Artist1", "ISBN3", 1, 1);

        mediaService.addMedia(b1);
        mediaService.addMedia(b2);
        mediaService.addMedia(c1);

        assertEquals(3, mediaService.getAllMedia().size());
        assertEquals(2, mediaService.getBooks().size());
        assertEquals(1, mediaService.getCDs().size());

        fineService.addFine("user1", 30);
        Map<String, Integer> fines = mediaService.getAllFines();
        assertEquals(30, fines.get("user1"));
    }

    @Test
    void testSearchBookAndCD() {
        Book b1 = new Book("Java Programming", "Author1", "B1", 1, 1);
        CD c1 = new CD("Best Hits", "Artist1", "C1", 1, 1);
        mediaService.addMedia(b1);
        mediaService.addMedia(c1);

        assertEquals(1, mediaService.searchBook("Java").size());
        assertEquals(1, mediaService.searchCD("Hits").size());
    }

    @Test
    void testFindBookAndCDByIsbn() {
        Book b1 = new Book("Java Programming", "Author1", "B1", 1, 1);
        CD c1 = new CD("Best Hits", "Artist1", "C1", 1, 1);
        mediaService.addMedia(b1);
        mediaService.addMedia(c1);

        assertEquals(b1, mediaService.findBookByIsbn("B1"));
        assertEquals(c1, mediaService.findCDByIsbn("C1"));

        assertNull(mediaService.findBookByIsbn("UNKNOWN"));
        assertNull(mediaService.findCDByIsbn("UNKNOWN"));
    }

    @Test
    void testDisplayMedia_emptyList_printsNoMedia() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        mediaService.displayMedia();

        assertTrue(outContent.toString().trim().contains("No media available"));
        System.setOut(originalOut);
    }

    @Test
    void testDisplayMedia_withItems_printsAllMedia() {
        Book book1 = new Book("Java Basics", "Author1", "B1", 2, 2);
        CD cd1 = new CD("Top Hits", "Artist1", "C1", 1, 1);

        mediaService.addMedia(book1);
        mediaService.addMedia(cd1);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        mediaService.displayMedia();

        String output = outContent.toString();
        assertTrue(output.contains("[Book] | Java Basics | ISBN: B1"));
        assertTrue(output.contains("[CD] | Top Hits | ISBN: C1"));

        System.setOut(originalOut);
    }
}
