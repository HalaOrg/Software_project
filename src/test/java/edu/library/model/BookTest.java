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
        Book b = new Book("Title", "A", "222");
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


    @Test
    void testSecondConstructorAndGetters() {
        LocalDate due = LocalDate.of(2024, 12, 31);
        // replace 5-arg constructor with setters
        Book b = new Book("Sec", "Auth", "555");
        b.setAvailable(false);
        b.setDueDate(due);
        assertEquals("Sec", b.getTitle());
        assertEquals("Auth", b.getAuthor());
        assertEquals("555", b.getIsbn());
        assertFalse(b.isAvailable());
        assertEquals(due, b.getDueDate());
    }

    @Test
    void testSettersWork() {
        Book b = new Book("Old", "A", "101");
        b.setTitle("New Title");
        b.setAuthor("New Author");
        b.setIsbn("202");
        assertEquals("New Title", b.getTitle());
        assertEquals("New Author", b.getAuthor());
        assertEquals("202", b.getIsbn());
    }

    @Test
    void testIsOverdue_whenAvailableTrue_returnsFalse() {
        Book b = new Book("B", "A", "303");
        // even if due date is in the past, availability should short-circuit to not overdue
        b.setDueDate(LocalDate.now().minusDays(10));
        b.setAvailable(true);
        assertFalse(b.isOverdue());
    }

    @Test
    void testIsOverdue_dueDateNull_returnsFalse() {
        Book b = new Book("NoDue", "A", "404");
        b.setAvailable(false);
        b.setDueDate(null);
        assertFalse(b.isOverdue());
    }


}
