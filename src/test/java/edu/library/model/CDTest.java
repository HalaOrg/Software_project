package edu.library.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CDTest {

    @Test
    void testCDCreationAndGetters() {
        CD cd = new CD("Best Hits", "Famous Artist", "CD123", 3);

        assertEquals("Best Hits", cd.getTitle());
        assertEquals("Famous Artist", cd.getAuthor());
        assertEquals("CD123", cd.getIsbn());
        assertEquals(3, cd.getTotalCopies());
        assertEquals(3, cd.getAvailableCopies());
        assertNull(cd.getDueDate());
    }

    @Test
    void testBorrowAndReturn() {
        CD cd = new CD("Best Hits", "Famous Artist", "CD123", 2);

        assertTrue(cd.isAvailable());
        cd.borrowOne();
        assertEquals(1, cd.getAvailableCopies());

        cd.returnOne();
        assertEquals(2, cd.getAvailableCopies());
    }

    @Test
    void testOverdue() {
        CD cd = new CD("Best Hits", "Famous Artist", "CD123", false, LocalDate.now().minusDays(1), 2);
        assertTrue(cd.isOverdue());

        cd.setDueDate(LocalDate.now().plusDays(1));
        assertFalse(cd.isOverdue());
    }

    @Test
    void testBorrowDurationAndDailyFine() {
        CD cd = new CD("Best Hits", "Famous Artist", "CD123", 2);
        assertEquals(7, cd.getBorrowDurationDays());
        assertEquals(20, cd.getDailyFine());
    }

    @Test
    void testSetters() {
        CD cd = new CD("Best Hits", "Famous Artist", "CD123", 3);
        cd.setTotalCopies(5);
        assertEquals(5, cd.getTotalCopies());

        cd.setAvailableCopies(4);
        assertEquals(4, cd.getAvailableCopies());
    }
}
