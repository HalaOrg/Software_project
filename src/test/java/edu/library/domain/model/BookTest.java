package edu.library.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    private Book book;

    @Test
    public void testConstructorWithQuantity() {
        Book b = new Book("AI", "Andrew", "444", 7);

        assertEquals("AI", b.getTitle());
        assertEquals("Andrew", b.getAuthor());
        assertEquals("444", b.getIsbn());
        assertEquals(7, b.getTotalCopies());
        assertEquals(7, b.getAvailableCopies());
        assertTrue(b.isAvailable());
        assertNull(b.getDueDate());
    }

    @Test
    public void testConstructorWithAvailabilityAndDueDate() {
        LocalDate d = LocalDate.of(2026, 1, 15);

        Book b = new Book("Networks", "Kurose", "555", false, d, 4);

        assertEquals("Networks", b.getTitle());
        assertEquals("Kurose", b.getAuthor());
        assertEquals("555", b.getIsbn());
        assertEquals(4, b.getTotalCopies());

        // available = false => availableCopies = 0
        assertEquals(0, b.getAvailableCopies());
        assertFalse(b.isAvailable());

        assertEquals(d, b.getDueDate());
    }

    @Test
    public void testConstructor1() {
        Book b = new Book("Java", "James", "111");

        assertEquals("Java", b.getTitle());
        assertEquals("James", b.getAuthor());
        assertEquals("111", b.getIsbn());
        assertEquals(1, b.getTotalCopies());
        assertEquals(1, b.getAvailableCopies());
    }

    @Test
    public void testConstructor2() {
        Book b = new Book("OOP", "Robert", "222", 5);

        assertEquals("OOP", b.getTitle());
        assertEquals("Robert", b.getAuthor());
        assertEquals("222", b.getIsbn());
        assertEquals(5, b.getTotalCopies());
        assertEquals(5, b.getAvailableCopies());
    }

    @Test
    public void testConstructor3() {
        LocalDate date = LocalDate.of(2025, 12, 30);
        Book b = new Book("DSA", "Mark", "333", false, date, 3);

        assertEquals("DSA", b.getTitle());
        assertEquals("Mark", b.getAuthor());
        assertEquals("333", b.getIsbn());
        assertEquals(3, b.getTotalCopies());
        assertEquals(0, b.getAvailableCopies());   // بما أنه available = false

        assertEquals(date, b.getDueDate());
        assertFalse(b.isAvailable());
    }

    @BeforeEach
    void setUp() {
        book = new Book("Java Programming", "John Doe", "ISBN123", 5);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("Java Programming", book.getTitle());
        assertEquals("John Doe", book.getAuthor());
        assertEquals("ISBN123", book.getIsbn());
        assertEquals(5, book.getTotalCopies());
        assertEquals(5, book.getAvailableCopies());
    }

    @Test
    void testAvailableAndBorrowReturn() {
        assertTrue(book.isAvailable());

        book.borrowOne();
        assertEquals(4, book.getAvailableCopies());
        assertTrue(book.isAvailable());

        book.borrowOne();
        book.borrowOne();
        book.borrowOne();
        book.borrowOne();
        assertEquals(0, book.getAvailableCopies());
        assertFalse(book.isAvailable());

        book.returnOne();
        assertEquals(1, book.getAvailableCopies());
        assertTrue(book.isAvailable());
    }

    @Test
    void testSetAvailableCopiesAndTotalCopies() {
        book.setTotalCopies(10);
        assertEquals(10, book.getTotalCopies());

        book.setAvailableCopies(7);
        assertEquals(7, book.getAvailableCopies());

        book.setAvailableCopies(15);
        assertEquals(10, book.getAvailableCopies());
    }


    @Test
    void testDueDateAndOverdue() {
        assertNull(book.getDueDate());
        assertFalse(book.isOverdue());

        LocalDate pastDate = LocalDate.now().minusDays(5);
        book.setDueDate(pastDate);
        book.setAvailable(false);
        book.setAvailableCopies(0);
        assertTrue(book.isOverdue());

        book.returnOne();
        assertFalse(book.isOverdue());
    }


    @Test
    void testAbstractMethods() {
        assertEquals(28, book.getBorrowDurationDays());
        assertEquals(10, book.getDailyFine());
        assertEquals(28, book.getBorrowDuration());
    }
    @Test
    public void testConstructorAvailableTotal() {

        String title = "Clean Code";
        String author = "Robert Martin";
        String isbn = "111-222";
        int available = 3;
        int total = 5;


        Book book = new Book(title, author, isbn, available, total);


        assertEquals(title, book.getTitle());
        assertEquals(author, book.getAuthor());
        assertEquals(isbn, book.getIsbn());

        assertEquals(available, book.getAvailableCopies());
        assertEquals(total, book.getTotalCopies());
    }


    @Test
    public void testAvailableCopiesNotNegative() {
        Book b = new Book("X", "Y", "Z", 3);

        b.setAvailableCopies(-5);
        assertEquals(0, b.getAvailableCopies());
    }
    @Test
    public void testNotOverdueWhenAvailableCopiesAboveZero() {
        Book b = new Book("Test", "A", "B", 3);
        b.setDueDate(LocalDate.now().minusDays(10));
        b.setAvailableCopies(1);

        assertFalse(b.isOverdue());
    }

    @Test
    public void testToStringFull() {
        Book b = new Book("TT", "AA", "999");
        String s = b.toString();

        assertTrue(s.contains("TT"));
        assertTrue(s.contains("AA"));
        assertTrue(s.contains("999"));
    }


    @Test
    public void testBorrowDurationDaysDirect() {
        Book b = new Book("Math", "Tom", "666");
        assertEquals(28, b.getBorrowDurationDays(), "Book borrow duration must be 28 days");
    }

    @Test
    public void testDailyFineDirect() {
        Book b = new Book("Physics", "Albert", "777");
        assertEquals(10, b.getDailyFine(), "Book daily fine must be 10");
    }

    @Test
    public void testBorrowDurationDays() {
        Book b = new Book("Java", "James", "111");
        assertEquals(28, b.getBorrowDurationDays());
    }

    @Test
    public void testDailyFine() {
        Book b = new Book("Java", "James", "111");
        assertEquals(10, b.getDailyFine());
    }
}
