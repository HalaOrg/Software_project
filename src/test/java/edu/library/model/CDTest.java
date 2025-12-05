package edu.library.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CDTest {

    @Test
    void testConstructorBasic() {
        CD cd = new CD("Music", "Artist", "111");
        assertEquals("Music", cd.getTitle());
        assertEquals("Artist", cd.getAuthor());
        assertEquals("111", cd.getIsbn());
        assertEquals(1, cd.getTotalCopies());
        assertEquals(1, cd.getAvailableCopies());
    }

    @Test
    void testConstructorWithQuantity() {
        CD cd = new CD("Album", "Singer", "222", 5);
        assertEquals(5, cd.getTotalCopies());
        assertEquals(5, cd.getAvailableCopies());
    }

    @Test
    void testConstructorWithAvailabilityAndDate() {
        LocalDate date = LocalDate.now().plusDays(7);
        CD cd = new CD("Hits", "DJ", "333", true, date, 3);

        assertEquals("Hits", cd.getTitle());
        assertEquals("DJ", cd.getAuthor());
        assertEquals("333", cd.getIsbn());
        assertTrue(cd.isAvailable());
        assertEquals(3, cd.getTotalCopies());
        assertEquals(date, cd.getDueDate());
    }

    @Test
    void testConstructorWithAvailableAndTotal() {
        CD cd = new CD("Mix", "Producer", "444", 2, 10);
        assertEquals(10, cd.getTotalCopies());
        assertEquals(2, cd.getAvailableCopies());
    }

    @Test
    void testBorrowDurationIsSevenDays() {
        CD cd = new CD("Track", "Band", "555");
        assertEquals(7, cd.getBorrowDurationDays());
    }

    @Test
    void testDailyFineIsTwenty() {
        CD cd = new CD("Track", "Band", "666");
        assertEquals(20, cd.getDailyFine());
    }

    @Test
    void testBorrowReducesAvailableCopies() {
        CD cd = new CD("Album", "Singer", "777", 2);
        cd.borrowOne(); // inherited from Media

        assertEquals(1, cd.getAvailableCopies());
        assertTrue(cd.isAvailable());
    }

    @Test
    void testReturnIncreasesAvailableCopies() {
        CD cd = new CD("Album", "Singer", "888", 1, 3);
        cd.returnOne();

        assertEquals(2, cd.getAvailableCopies());
    }

    @Test
    void testAvailabilityWhenZeroCopies() {
        CD cd = new CD("Sound", "Artist", "999", 0, 2);
        assertFalse(cd.isAvailable());
    }
}
