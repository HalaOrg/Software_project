package edu.library.fine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FineCalculatorTest {

    @Test
    void testDefaultStrategies() {
        FineCalculator calculator = new FineCalculator();

        // نفترض أن الغرامات لكل نوع هي كما حددت في الاستراتيجيات:
        // BookFineStrategy: 10 لكل يوم، CDFineStrategy: 5 لكل يوم، JournalFineStrategy: 3 لكل يوم
        assertEquals(50, calculator.calculate(FineCalculator.MEDIA_BOOK, 5));
        assertEquals(25, calculator.calculate(FineCalculator.MEDIA_CD, 5));
        assertEquals(15, calculator.calculate(FineCalculator.MEDIA_JOURNAL, 5));

        // نوع غير معروف يجب أن يرجع 0
        assertEquals(0, calculator.calculate("MAGAZINE", 5));

        // النوع null يجب أن يرجع 0
        assertEquals(0, calculator.calculate(null, 5));
    }

    @Test
    void testRegisterCustomStrategy() {
        FineCalculator calculator = new FineCalculator();

        // إضافة استراتيجية جديدة
        calculator.registerStrategy("MAGAZINE", overdueDays -> overdueDays * 2);
        assertEquals(10, calculator.calculate("MAGAZINE", 5));

        // التأكد أن الحروف الكبيرة والصغيرة تعمل
        assertEquals(10, calculator.calculate("magazine", 5));
    }

    @Test
    void testRegisterNullStrategy() {
        FineCalculator calculator = new FineCalculator();

        // لا يجب أن يضيف أي شيء عند إرسال null
        calculator.registerStrategy(null, null);

        assertEquals(0, calculator.calculate(null, 5));
    }
}
