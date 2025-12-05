package edu.library.fine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookFineStrategyTest {

    @Test
    void testCalculateFinePositiveDays() {
        BookFineStrategy strategy = new BookFineStrategy();

        assertEquals(10, strategy.calculateFine(1), "1 day overdue should be 10");
        assertEquals(20, strategy.calculateFine(2), "2 days overdue should be 20");
        assertEquals(100, strategy.calculateFine(10), "10 days overdue should be 100");
    }

    @Test
    void testCalculateFineZeroDays() {
        BookFineStrategy strategy = new BookFineStrategy();

        assertEquals(0, strategy.calculateFine(0), "0 overdue days should return 0");
    }

    @Test
    void testCalculateFineNegativeDays() {
        BookFineStrategy strategy = new BookFineStrategy();

        assertEquals(0, strategy.calculateFine(-1), "Negative overdue days should return 0");
        assertEquals(0, strategy.calculateFine(-100), "Negative overdue days should return 0");
    }

    @Test
    void testCalculateFineLargeNumber() {
        BookFineStrategy strategy = new BookFineStrategy();

        int largeDays = 1_000;
        assertEquals(largeDays * 10, strategy.calculateFine(largeDays), "Large number of overdue days should be calculated correctly");
    }
}
