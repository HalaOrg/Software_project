package edu.library.SystemTimeProvider;
import edu.library.time.SystemTimeProvider;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SystemTimeProviderYTest {

    @Test
    void testTodayReturnsCurrentDate() {
        SystemTimeProvider timeProvider = new SystemTimeProvider();

        LocalDate expected = LocalDate.now();
        LocalDate actual = timeProvider.today();

        assertEquals(expected, actual,
                "today() should return the current system date");
    }
}
