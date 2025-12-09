package edu.library.domain.fine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JournalFineStrategyTest {

    @Test
    void testCalculateFineZeroDays() {
        FineStrategy strategy = new JournalFineStrategy();
        int fine = strategy.calculateFine(0);
        assertEquals(0, fine, "Fine should be 0 when overdueDays is 0");
    }

    @Test
    void testCalculateFineNegativeDays() {
        FineStrategy strategy = new JournalFineStrategy();
        int fine = strategy.calculateFine(-5);
        assertEquals(0, fine, "Fine should be 0 when overdueDays is negative");
    }

    @Test
    void testCalculateFinePositiveDays() {
        FineStrategy strategy = new JournalFineStrategy();
        int fine = strategy.calculateFine(3);
        assertEquals(45, fine, "Fine should be 15 * 3 = 45 for 3 overdue days");

        fine = strategy.calculateFine(1);
        assertEquals(15, fine, "Fine should be 15 for 1 overdue day");

        fine = strategy.calculateFine(10);
        assertEquals(150, fine, "Fine should be 15 * 10 = 150 for 10 overdue days");
    }

    @Test
    void testLargeNumberOfDays() {
        FineStrategy strategy = new JournalFineStrategy();
        int fine = strategy.calculateFine(1000);
        assertEquals(15000, fine, "Fine should scale correctly for large overdue days");
    }
}
