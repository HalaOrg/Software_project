package edu.library.fine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookFineStrategyTest {

    @Test
    void testCalculateFine_zeroOrNegativeOverdue() {
        BookFineStrategy strategy = new BookFineStrategy();

        // الغرامة يجب أن تكون صفر إذا لم يكن هناك تأخير
        assertEquals(0, strategy.calculateFine(0), "Overdue 0 days should return 0 fine");
        assertEquals(0, strategy.calculateFine(-3), "Negative overdue days should return 0 fine");
    }

    @Test
    void testCalculateFine_positiveOverdue() {
        BookFineStrategy strategy = new BookFineStrategy();

        // الغرامة = عدد الأيام المتأخرة * 10
        assertEquals(10, strategy.calculateFine(1), "1 day overdue should return 10 NIS");
        assertEquals(70, strategy.calculateFine(7), "7 days overdue should return 70 NIS");
        assertEquals(100, strategy.calculateFine(10), "10 days overdue should return 100 NIS");
    }
}
