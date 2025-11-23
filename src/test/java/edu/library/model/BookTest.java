package edu.library.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    @Test
    void testBookCreation() {
        Book b = new Book("Test Book", "Author", "111");
        assertTrue(b.isAvailable());
        assertEquals("Test Book", b.getTitle());
        assertEquals("Author", b.getAuthor());
        assertNull(b.getDueDate());
    }

    @Test
    void testSetAvailabilityAndDueDate() {
        Book b = new Book("Title", "A", "222", false, null);
        b.setAvailable(true);
        b.setDueDate(LocalDate.now().plusDays(5));
        assertTrue(b.isAvailable());
        assertNotNull(b.getDueDate());
    }

    @Test
    void testIsOverdue() {
        Book b = new Book("Old Book", "Writer", "333");
        b.setAvailable(false);
        b.setDueDate(LocalDate.now().minusDays(2));
        assertTrue(b.isOverdue());

        b.setDueDate(LocalDate.now().plusDays(2));
        assertFalse(b.isOverdue());
    }

    @Test
    void testToStringFormat() {
        Book b = new Book("Clean Code", "Uncle Bob", "999");
        String text = b.toString();
        assertTrue(text.contains("Clean Code"));
        assertTrue(text.contains("Available"));
    }
}
