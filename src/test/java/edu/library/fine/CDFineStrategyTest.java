package edu.library.fine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CDFineStrategyTest {

    @Test
    void testCalculateFinePositiveDays() {
        CDFineStrategy strategy = new CDFineStrategy();

        assertEquals(20, strategy.calculateFine(1), "1 day overdue should be 20");
        assertEquals(40, strategy.calculateFine(2), "2 days overdue should be 40");
        assertEquals(200, strategy.calculateFine(10), "10 days overdue should be 200");
    }

    @Test
    void testCalculateFineZeroDays() {
        CDFineStrategy strategy = new CDFineStrategy();

        assertEquals(0, strategy.calculateFine(0), "0 overdue days should return 0");
    }

    @Test
    void testCalculateFineNegativeDays() {
        CDFineStrategy strategy = new CDFineStrategy();

        assertEquals(0, strategy.calculateFine(-1), "Negative overdue days should return 0");
        assertEquals(0, strategy.calculateFine(-10), "Negative overdue days should return 0");
    }

    @Test
    void testCalculateFineLargeNumber() {
        CDFineStrategy strategy = new CDFineStrategy();

        int largeDays = 1_000;
        assertEquals(largeDays * 20, strategy.calculateFine(largeDays), "Large number of overdue days");
    }
}
