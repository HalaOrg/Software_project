package edu.library.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BorrowRecordTest {


    @Test
    void testConstructorNormal() {
        LocalDate due = LocalDate.now().plusDays(7);

        BorrowRecord r = new BorrowRecord(
                "areej",
                "ISBN123",
                due,
                false,
                null
        );

        assertEquals("areej", r.getUsername());
        assertEquals("ISBN123", r.getIsbn());
        assertEquals(due, r.getDueDate());
        assertFalse(r.isReturned());
        assertNull(r.getReturnDate());
    }

    @Test
    void testConstructorReturnedAlready() {
        LocalDate due = LocalDate.now().plusDays(3);
        LocalDate returnedDate = LocalDate.now().minusDays(1);

        BorrowRecord r = new BorrowRecord(
                "lama",
                "ISBN555",
                due,
                true,
                returnedDate
        );

        assertTrue(r.isReturned());
        assertEquals(returnedDate, r.getReturnDate());
        assertEquals("lama", r.getUsername());
    }

    @Test
    void testNullUsernameAllowed() {
        BorrowRecord r = new BorrowRecord(
                null,
                "I123",
                LocalDate.now(),
                false,
                null
        );
        assertNull(r.getUsername());
    }

    @Test
    void testNullIsbnAllowed() {
        BorrowRecord r = new BorrowRecord(
                "hadi",
                null,
                LocalDate.now(),
                false,
                null
        );
        assertNull(r.getIsbn());
    }

    @Test
    void testNullDueDateAllowed() {
        BorrowRecord r = new BorrowRecord(
                "ahmad",
                "111",
                null,
                false,
                null
        );
        assertNull(r.getDueDate());
    }

    @Test
    void testGetReturnDateDirect() {
        LocalDate returnDate = LocalDate.now();
        BorrowRecord r = new BorrowRecord("aya", "XYZ", LocalDate.now(), true, returnDate);

        assertEquals(returnDate, r.getReturnDate());
    }
    @Test
    void testMarkReturnedSingleCall() {
        BorrowRecord r = new BorrowRecord("sara", "ISBN1", LocalDate.now(), false, null);

        LocalDate returnDate = LocalDate.now().minusDays(1);
        r.markReturned(returnDate);

        assertTrue(r.isReturned());
        assertEquals(returnDate, r.getReturnDate());
    }
    @Test
    void testGetIsbnDirect() {
        BorrowRecord r = new BorrowRecord("lina", "ABC123", LocalDate.now(), false, null);

        assertEquals("ABC123", r.getIsbn());
    }

    @Test
    void testIsReturnedInitiallyFalse() {
        BorrowRecord r = new BorrowRecord("aya", "ISBN999", LocalDate.now(), false, null);
        assertFalse(r.isReturned());
    }

    @Test
    void testGetDueDateDirect() {
        LocalDate due = LocalDate.now().plusDays(20);
        BorrowRecord r = new BorrowRecord("reem", "ABC555", due, false, null);

        assertEquals(due, r.getDueDate());
    }

    @Test
    void testUsernameNotEmpty() {
        BorrowRecord r = new BorrowRecord("x", "y", LocalDate.now(), false, null);
        assertFalse(r.getUsername().isEmpty());
    }

    @Test
    void testISBNNotEmpty() {
        BorrowRecord r = new BorrowRecord("u", "123ABC", LocalDate.now(), false, null);
        assertEquals("123ABC", r.getIsbn());
    }

    @Test
    void testUsernameWithSpecialCharacters() {
        BorrowRecord r = new BorrowRecord("user_123", "ISBNX", LocalDate.now(), false, null);
        assertEquals("user_123", r.getUsername());
    }

    @Test
    void testIsbnWithSpecialCharacters() {
        BorrowRecord r = new BorrowRecord("moh", "ISBN-44-XYZ", LocalDate.now(), false, null);
        assertEquals("ISBN-44-XYZ", r.getIsbn());
    }



    @Test
    void testMarkReturnedNormal() {
        LocalDate due = LocalDate.now().plusDays(10);
        BorrowRecord r = new BorrowRecord("omar", "ISBN333", due, false, null);

        LocalDate returnDate = LocalDate.now();
        r.markReturned(returnDate);

        assertTrue(r.isReturned());
        assertEquals(returnDate, r.getReturnDate());
    }

    @Test
    void testMarkReturnedOverridePreviousReturnDate() {
        LocalDate due = LocalDate.now().plusDays(4);
        LocalDate firstDate = LocalDate.now().minusDays(2);

        BorrowRecord r = new BorrowRecord("sara", "ISBN000", due, true, firstDate);

        LocalDate newReturnDate = LocalDate.now();
        r.markReturned(newReturnDate);

        assertTrue(r.isReturned());
        assertEquals(newReturnDate, r.getReturnDate());
    }

    @Test
    void testMarkReturnedCalledTwiceKeepsLastDate() {
        LocalDate due = LocalDate.now().plusDays(3);

        BorrowRecord r = new BorrowRecord("noor", "ABCD", due, false, null);

        LocalDate firstDate = LocalDate.now().minusDays(3);
        LocalDate secondDate = LocalDate.now();

        r.markReturned(firstDate);
        r.markReturned(secondDate);

        assertEquals(secondDate, r.getReturnDate());
        assertTrue(r.isReturned());
    }

    @Test
    void testNullReturnDate() {
        LocalDate due = LocalDate.now().plusDays(5);

        BorrowRecord r = new BorrowRecord("nada", "ISBN888", due, false, null);

        r.markReturned(null);

        assertTrue(r.isReturned());
        assertNull(r.getReturnDate());
    }
}
