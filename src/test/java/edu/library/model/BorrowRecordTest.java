package edu.library.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BorrowRecordTest {

    @Test
    void testBorrowRecordCreation() {
        LocalDate dueDate = LocalDate.of(2025, 12, 10);
        LocalDate returnDate = null;

        BorrowRecord record = new BorrowRecord("john_doe", "ISBN123", dueDate, false, returnDate);

        assertEquals("john_doe", record.getUsername());
        assertEquals("ISBN123", record.getIsbn());
        assertEquals(dueDate, record.getDueDate());
        assertFalse(record.isReturned());
        assertNull(record.getReturnDate());
    }

    @Test
    void testMarkReturned() {
        LocalDate dueDate = LocalDate.of(2025, 12, 10);
        BorrowRecord record = new BorrowRecord("john_doe", "ISBN123", dueDate, false, null);

        LocalDate returnDate = LocalDate.of(2025, 12, 12);
        record.markReturned(returnDate);

        assertTrue(record.isReturned());
        assertEquals(returnDate, record.getReturnDate());
    }

    @Test
    void testMultipleReturns() {
        LocalDate dueDate = LocalDate.of(2025, 12, 10);
        BorrowRecord record = new BorrowRecord("john_doe", "ISBN123", dueDate, false, null);

        LocalDate firstReturn = LocalDate.of(2025, 12, 12);
        record.markReturned(firstReturn);

        // محاولة تحديث الإرجاع مرة أخرى
        LocalDate secondReturn = LocalDate.of(2025, 12, 13);
        record.markReturned(secondReturn);

        // يجب أن يتم تحديث التاريخ الأخير
        assertEquals(secondReturn, record.getReturnDate());
        assertTrue(record.isReturned());
    }
}
