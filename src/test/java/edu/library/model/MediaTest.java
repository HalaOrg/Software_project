package edu.library.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MediaTest {

    // كلاس فرعي وهمي لاختبار abstract Media
    private static class TestMedia extends Media {
        public TestMedia(String title, String author, String isbn, int available, int total) {
            super(title, author, isbn, available, total);
        }

        @Override
        public int getBorrowDurationDays() {
            return 5;
        }

        @Override
        public int getDailyFine() {
            return 15;
        }
    }

    @Test
    void testCreationAndGetters() {
        Media media = new TestMedia("Test Title", "Author Name", "ISBN123", 3, 5);

        assertEquals("Test Title", media.getTitle());
        assertEquals("Author Name", media.getAuthor());
        assertEquals("ISBN123", media.getIsbn());
        assertEquals(5, media.getTotalCopies());
        assertEquals(3, media.getAvailableCopies());
        assertNull(media.getDueDate());
        assertTrue(media.isAvailable());
    }

    @Test
    void testSetters() {
        Media media = new TestMedia("Title", "Author", "ISBN", 2, 5);

        media.setTotalCopies(4);
        assertEquals(4, media.getTotalCopies());

        media.setAvailableCopies(3);
        assertEquals(3, media.getAvailableCopies());

        media.setAvailableCopies(10); // يجب أن لا تتجاوز totalCopies
        assertEquals(4, media.getAvailableCopies());
    }

    @Test
    void testBorrowAndReturn() {
        Media media = new TestMedia("Title", "Author", "ISBN", 2, 2);

        media.borrowOne();
        assertEquals(1, media.getAvailableCopies());
        assertTrue(media.isAvailable());

        media.borrowOne();
        assertEquals(0, media.getAvailableCopies());
        assertFalse(media.isAvailable());

        media.returnOne();
        assertEquals(1, media.getAvailableCopies());
        assertTrue(media.isAvailable());

        media.returnOne();
        assertEquals(2, media.getAvailableCopies());
    }

    @Test
    void testOverdue() {
        Media media = new TestMedia("Title", "Author", "ISBN", 0, 1);
        media.setDueDate(LocalDate.now().minusDays(1));

        assertTrue(media.isOverdue());

        media.setDueDate(LocalDate.now().plusDays(1));
        assertFalse(media.isOverdue());
    }

    @Test
    void testBorrowDurationAndDailyFine() {
        Media media = new TestMedia("Title", "Author", "ISBN", 1, 1);
        assertEquals(5, media.getBorrowDurationDays());
        assertEquals(15, media.getDailyFine());
    }

    @Test
    void testToString() {
        Media media = new TestMedia("Title", "Author", "ISBN", 2, 5);
        String str = media.toString();
        assertTrue(str.contains("Title: Title"));
        assertTrue(str.contains("Author: Author"));
        assertTrue(str.contains("ISBN: ISBN"));
        assertTrue(str.contains("Available: 2/5"));
    }
}
