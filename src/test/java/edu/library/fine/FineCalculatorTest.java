package edu.library.fine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FineCalculatorTest {

    @Test
    void calculate_returnsZeroForUnknownMedia() {
        FineCalculator calculator = new FineCalculator();
        assertEquals(0, calculator.calculate("UNKNOWN", 5));
    }

    @Test
    void calculate_usesRegisteredStrategies() {
        FineCalculator calculator = new FineCalculator();
        assertEquals(30, calculator.calculate(FineCalculator.MEDIA_BOOK, 3));
        assertEquals(60, calculator.calculate(FineCalculator.MEDIA_CD, 3));
        assertEquals(45, calculator.calculate(FineCalculator.MEDIA_JOURNAL, 3));
    }
}
