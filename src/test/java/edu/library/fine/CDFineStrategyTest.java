package edu.library.fine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CDFineStrategyTest {

    @Test
    void testCalculateFine_zeroOverdue() {
        CDFineStrategy strategy = new CDFineStrategy();
        assertEquals(0, strategy.calculateFine(0), "Overdue 0 days should return 0 fine");
        assertEquals(0, strategy.calculateFine(-5), "Negative overdue days should return 0 fine");
    }

    @Test
    void testCalculateFine_positiveOverdue() {
        CDFineStrategy strategy = new CDFineStrategy();

        assertEquals(20, strategy.calculateFine(1), "1 day overdue should return 20 NIS");
        assertEquals(140, strategy.calculateFine(7), "7 days overdue should return 140 NIS");
        assertEquals(200, strategy.calculateFine(10), "10 days overdue should return 200 NIS");
    }
}
