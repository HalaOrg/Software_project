package edu.library.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

public class BookTest {

    @Test
    void testConstructorWithQuantity() {
        Book book = new Book("Java Programming", "Alice", "ISBN123", 5);

        assertEquals("Java Programming", book.getTitle());
        assertEquals("Alice", book.getAuthor());
        assertEquals("ISBN123", book.getIsbn());
        assertEquals(5, book.getTotalCopies());
        assertEquals(5, book.getAvailableCopies());
    }

    @Test
    void testConstructorWithAvailabilityAndDueDate() {
        LocalDate due = LocalDate.of(2025, 1, 1);

        Book book = new Book("Data Structures", "Bob", "ISBN999",
                true, due, 3);

        assertEquals("Data Structures", book.getTitle());
        assertEquals("Bob", book.getAuthor());
        assertEquals("ISBN999", book.getIsbn());
        assertTrue(book.isAvailable());
        assertEquals(due, book.getDueDate());
        assertEquals(3, book.getTotalCopies());
    }

    @Test
    void testBorrowDurationDays() {
        Book book = new Book("Test", "A", "123");
        assertEquals(28, book.getBorrowDurationDays(),
                "Borrow duration for books must be 28 days");
    }

    @Test
    void testDailyFine() {
        Book book = new Book("Test", "A", "123");
        assertEquals(10, book.getDailyFine(),
                "Daily fine for books must be 10");
    }

    @Test
    void testAvailabilityAfterBorrowAndReturn() {
        Book book = new Book("Test", "A", "123", 2);

        // Borrow one copy
        book.borrowOne();
        assertEquals(1, book.getAvailableCopies());

        // Return one copy
        book.returnOne();
        assertEquals(2, book.getAvailableCopies());
    }
}
