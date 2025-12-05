package edu.library.service;

import edu.library.model.Book;
import edu.library.model.CD;
import edu.library.model.Media;
import edu.library.fine.FineCalculator;
import edu.library.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MediaServiceTest {

    private MediaService mediaService;
    private BorrowRecordService borrowRecordService;
    private FineService fineService;
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() throws Exception {
        borrowRecordService = new BorrowRecordService();
        fineService = new FineService(); // جديد لكل اختبار
        timeProvider = mock(TimeProvider.class);

        File tempFile = File.createTempFile("media_test", ".txt");
        tempFile.deleteOnExit();

        mediaService = new MediaService(
                tempFile.getAbsolutePath(),
                borrowRecordService,
                fineService,
                timeProvider,
                new FineCalculator()
        );
    }

    // ---------------------------------------------------------
    //                 TEST: ADD / SEARCH / DELETE
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    //                TEST: UPDATE QUANTITY
    // ---------------------------------------------------------
    @Test
    void testUpdateMediaQuantity() {
        Book b1 = new Book("Book1", "Author1", "ISBN1", 5, 5);
        mediaService.addMedia(b1);

        assertTrue(mediaService.updateMediaQuantity("ISBN1", 3));
        assertEquals(3, b1.getTotalCopies());
        assertEquals(3, b1.getAvailableCopies());

        assertFalse(mediaService.updateMediaQuantity("UNKNOWN", 5));
    }

    // ---------------------------------------------------------
    //                TEST: BORROW / RETURN BOOK
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    //                TEST: BORROW / RETURN CD
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    //           TEST: BORROW FAILS (NO COPIES / HAS FINE)
    // ---------------------------------------------------------
    @Test
    void testBorrowFailsWhenNotAvailableOrWithFine() {
        when(timeProvider.today()).thenReturn(LocalDate.of(2025, 12, 3));

        Book b1 = new Book("Book1", "Author1", "ISBN1", 1);
        b1.setAvailableCopies(0); // نجبره يكون مش متوفر
        mediaService.addMedia(b1);

        assertFalse(mediaService.borrowBook(b1, "user1"));

        Book b2 = new Book("Book2", "Author2", "ISBN2", 1);
        mediaService.addMedia(b2);
        fineService.addFine("user2", 50);

        assertFalse(mediaService.borrowBook(b2, "user2"));
    }

    // ---------------------------------------------------------
    //         TEST: GET ALL MEDIA / BOOKS / CDS / FINES
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    //             TEST: SEARCH BOOK / CD
    // ---------------------------------------------------------
    @Test
    void testSearchBookAndCD() {
        Book b1 = new Book("Java Programming", "Author1", "B1", 1, 1);
        CD c1 = new CD("Best Hits", "Artist1", "C1", 1, 1);
        mediaService.addMedia(b1);
        mediaService.addMedia(c1);

        assertEquals(1, mediaService.searchBook("Java").size());
        assertEquals(1, mediaService.searchCD("Hits").size());
    }

    // ---------------------------------------------------------
    //           TEST: FIND BY ISBN (BOOK + CD)
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    //           TEST: DISPLAY MEDIA (EMPTY)
    // ---------------------------------------------------------
    @Test
    void testDisplayMedia_emptyList_printsNoMedia() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        mediaService.displayMedia();

        assertTrue(outContent.toString().trim().contains("No media available"));
        System.setOut(originalOut);
    }

    // ---------------------------------------------------------
    //           TEST: DISPLAY MEDIA (FILLED)
    // ---------------------------------------------------------
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
