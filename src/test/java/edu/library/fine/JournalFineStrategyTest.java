package edu.library.fine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JournalFineStrategyTest {

    @Test
    void testCalculateFine_zeroOrNegativeOverdue() {
        JournalFineStrategy strategy = new JournalFineStrategy();

        // Fine should be 0 if overdue days are 0 or negative
        assertEquals(0, strategy.calculateFine(0), "Overdue 0 days should return 0 fine");
        assertEquals(0, strategy.calculateFine(-5), "Negative overdue days should return 0 fine");
    }

    @Test
    void testCalculateFine_positiveOverdue() {
        JournalFineStrategy strategy = new JournalFineStrategy();

        // Fine = overdue days * 15
        assertEquals(15, strategy.calculateFine(1), "1 day overdue should return 15 NIS");
        assertEquals(75, strategy.calculateFine(5), "5 days overdue should return 75 NIS");
        assertEquals(150, strategy.calculateFine(10), "10 days overdue should return 150 NIS");
    }
}
