package edu.library.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MediaTest {

    // Dummy subclass لتغطية الـabstract methods
    private static class DummyMedia extends Media {
        public DummyMedia(String title, String author, String isbn, int quantity) {
            super(title, author, isbn, quantity);
        }

        public DummyMedia(String title, String author, String isbn, boolean available, LocalDate dueDate, int quantity) {
            super(title, author, isbn, available, dueDate, quantity);
        }

        public DummyMedia(String title, String author, String isbn, int available, int total) {
            super(title, author, isbn, available, total);
        }

        @Override
        public int getBorrowDurationDays() {
            return 10;
        }

        @Override
        public int getDailyFine() {
            return 5;
        }
    }

    @Test
    void testConstructorsAndGettersSetters() {
        LocalDate due = LocalDate.now().plusDays(5);

        // Constructor 1
        DummyMedia m1 = new DummyMedia("T1", "A1", "ISBN1", 3);
        assertEquals("T1", m1.getTitle());
        assertEquals("A1", m1.getAuthor());
        assertEquals("ISBN1", m1.getIsbn());
        assertEquals(3, m1.getAvailableCopies());
        assertEquals(3, m1.getTotalCopies());
        assertNull(m1.getDueDate());

        // Constructor 2
        DummyMedia m2 = new DummyMedia("T2", "A2", "ISBN2", true, due, 5);
        assertEquals("T2", m2.getTitle());
        assertTrue(m2.isAvailable());
        assertEquals(due, m2.getDueDate());
        assertEquals(5, m2.getAvailableCopies());
        assertEquals(5, m2.getTotalCopies());

        // Constructor 3
        DummyMedia m3 = new DummyMedia("T3", "A3", "ISBN3", 2, 10);
        assertEquals(2, m3.getAvailableCopies());
        assertEquals(10, m3.getTotalCopies());

        // Setter / Getter
        m3.setTotalCopies(8);
        assertEquals(8, m3.getTotalCopies());
        m3.setAvailableCopies(5);
        assertEquals(5, m3.getAvailableCopies());

        m3.setAvailable(true);
        assertTrue(m3.getAvailableCopies() > 0);

        m3.setAvailable(false);
        assertTrue(m3.getAvailableCopies() >= 0);

        m3.setDueDate(due);
        assertEquals(due, m3.getDueDate());
    }
    @Test
    void testConstructorEdgeCases() {
        LocalDate due = LocalDate.now();

        // quantity = 0 و quantity < 0
        DummyMedia m0 = new DummyMedia("Zero", "Author", "ISBN0", 0);
        DummyMedia mNeg = new DummyMedia("Neg", "Author", "ISBNNeg", -5);
        assertEquals(0, m0.getTotalCopies());
        assertEquals(0, mNeg.getTotalCopies());

        // available = true / false
        DummyMedia mAvail = new DummyMedia("Avail", "Author", "ISBNA", true, due, 3);
        DummyMedia mNotAvail = new DummyMedia("NotAvail", "Author", "ISBNNA", false, due, 3);
        assertEquals(3, mAvail.getAvailableCopies());
        assertEquals(0, mNotAvail.getAvailableCopies());
    }

    @Test
    void testSetTotalCopiesBranch() {
        DummyMedia m = new DummyMedia("Test", "Author", "ISBN", 5, 3);
        m.setTotalCopies(2); // availableCopies > totalCopies branch
        assertEquals(2, m.getTotalCopies());
        assertEquals(2, m.getAvailableCopies());
    }


    @Test
    void testBorrowReturnEdgeCases() {
        DummyMedia m = new DummyMedia("Book", "Author", "ISBN", 1, 3);

        m.borrowOne(); // يصبح 0
        assertEquals(0, m.getAvailableCopies());
        m.borrowOne(); // لا ينقص أكثر من صفر
        assertEquals(0, m.getAvailableCopies());

        m.returnOne();
        assertEquals(1, m.getAvailableCopies());
        m.returnOne();
        m.returnOne();
        m.returnOne(); // لا يزيد عن totalCopies
        assertEquals(3, m.getAvailableCopies());
    }

    @Test
    void testIsOverdueBranches() {
        LocalDate past = LocalDate.now().minusDays(1);
        LocalDate future = LocalDate.now().plusDays(1);

        // overdue = true
        DummyMedia m1 = new DummyMedia("Book1", "Author", "ISBN1", false, past, 1);
        m1.setAvailableCopies(0);
        assertTrue(m1.isOverdue());

        // overdue = false, dueDate future
        DummyMedia m2 = new DummyMedia("Book2", "Author", "ISBN2", false, future, 1);
        m2.setAvailableCopies(0);
        assertFalse(m2.isOverdue());

        // overdue = false, dueDate null
        DummyMedia m3 = new DummyMedia("Book3", "Author", "ISBN3", 2);
        assertFalse(m3.isOverdue());

        // overdue = false, availableCopies > 0
        DummyMedia m4 = new DummyMedia("Book4", "Author", "ISBN4", true, past, 1);
        assertFalse(m4.isOverdue());
    }
    @Test
    void testIsOverdueFullCoverage() {
        LocalDate past = LocalDate.now().minusDays(1);
        LocalDate future = LocalDate.now().plusDays(1);

        // 1️⃣ الكتاب متوفر → false
        DummyMedia m1 = new DummyMedia("Book1", "Author", "ISBN1", 2, 2);
        assertFalse(m1.isOverdue());

        // 2️⃣ الكتاب غير متوفر + dueDate = null → false
        DummyMedia m2 = new DummyMedia("Book2", "Author", "ISBN2", 0, 2);
        m2.setDueDate(null);
        assertFalse(m2.isOverdue());

        // 3️⃣ الكتاب غير متوفر + dueDate في المستقبل → false
        DummyMedia m3 = new DummyMedia("Book3", "Author", "ISBN3", 0, 2);
        m3.setDueDate(future);
        assertFalse(m3.isOverdue());

        // 4️⃣ الكتاب غير متوفر + dueDate في الماضي → true
        DummyMedia m4 = new DummyMedia("Book4", "Author", "ISBN4", 0, 2);
        m4.setDueDate(past);
        assertTrue(m4.isOverdue());
    }

    @Test
    void testBorrowReturnIsAvailable() {
        DummyMedia m = new DummyMedia("Book", "Author", "ISBN", 3);

        int initialAvailable = m.getAvailableCopies();
        m.borrowOne();
        assertEquals(initialAvailable - 1, m.getAvailableCopies());

        m.returnOne();
        assertEquals(initialAvailable, m.getAvailableCopies());

        assertTrue(m.isAvailable());
        m.setAvailable(false);
        assertTrue(m.getAvailableCopies() <= initialAvailable);
        m.setAvailable(true);
        assertTrue(m.getAvailableCopies() >= 1);
    }

    @Test
    void testOverdue() {
        DummyMedia m = new DummyMedia("Book", "Author", "ISBN", true, LocalDate.now().minusDays(1), 1);

        // ↓↓↓ ضروري تخلي الكتاب غير متوفر (مستعار)
        m.setAvailableCopies(0);

        assertTrue(m.isOverdue());

        DummyMedia m2 = new DummyMedia("Book2", "Author2", "ISBN2", true, LocalDate.now().plusDays(1), 1);
        m2.setAvailableCopies(0); // مش ضروري لكن لتأكيد نفس المنطق
        assertFalse(m2.isOverdue());

        DummyMedia m3 = new DummyMedia("Book3", "Author3", "ISBN3", 2);
        assertFalse(m3.isOverdue()); // no due date
    }


    @Test
    void testAbstractMethods() {
        DummyMedia m = new DummyMedia("Book", "Author", "ISBN", 1);
        assertEquals(10, m.getBorrowDurationDays());
        assertEquals(5, m.getDailyFine());
        assertEquals(10, m.getBorrowDuration()); // استخدام getBorrowDuration()
    }

    @Test
    void testToString() {
        DummyMedia m = new DummyMedia("Book", "Author", "ISBN", 2);
        String s = m.toString();
        assertTrue(s.contains("Book"));
        assertTrue(s.contains("Author"));
        assertTrue(s.contains("ISBN"));
    }
}
