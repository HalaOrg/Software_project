package edu.library.domain.fine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FineCalculatorTest {

    @Test
    void testCalculateForRegisteredMedia() {
        FineCalculator calculator = new FineCalculator();

        int bookFine = calculator.calculate(FineCalculator.MEDIA_BOOK, 3);
        assertEquals(30, bookFine, "Book fine should be 10*3=30");

        int bookFineZero = calculator.calculate(FineCalculator.MEDIA_BOOK, 0);
        assertEquals(0, bookFineZero, "Book fine should be 0 for 0 overdue days");

        int bookFineNegative = calculator.calculate(FineCalculator.MEDIA_BOOK, -5);
        assertEquals(0, bookFineNegative, "Book fine should be 0 for negative overdue days");

        int cdFine = calculator.calculate(FineCalculator.MEDIA_CD, 2);
        assertEquals(40, cdFine, "CD fine should be 20*2=40");

        int journalFine = calculator.calculate(FineCalculator.MEDIA_JOURNAL, 4);
        assertEquals(60, journalFine, "Journal fine should be 15*4=60");
    }

    @Test
    void testCalculateWithNullOrUnknownMediaType() {
        FineCalculator calculator = new FineCalculator();

        assertEquals(0, calculator.calculate(null, 5), "Null media type should return 0");
        assertEquals(0, calculator.calculate("UNKNOWN_TYPE", 5), "Unknown media type should return 0");
    }

    @Test
    void testRegisterNewStrategy() {
        FineCalculator calculator = new FineCalculator();

        calculator.registerStrategy("MAGAZINE", overdueDays -> overdueDays * 50);

        int magazineFine = calculator.calculate("MAGAZINE", 2);
        assertEquals(100, magazineFine, "Magazine fine should be 50*2=100");

        calculator.registerStrategy(null, null);
    }
    @Test
    void testRegisterStrategyBranchCoverage() {
        FineCalculator calculator = new FineCalculator();

        calculator.registerStrategy(null, overdueDays -> overdueDays * 5);

        calculator.registerStrategy("BOOK", null);

        calculator.registerStrategy("BOOK", overdueDays -> overdueDays * 5);
    }


    @Test
    void testCalculateEdgeCases() {
        FineCalculator calculator = new FineCalculator();

        int fineLowerCase = calculator.calculate("book", 1);
        int fineMixedCase = calculator.calculate("BoOk", 1);
        assertEquals(10, fineLowerCase);
        assertEquals(10, fineMixedCase);
    }
}
