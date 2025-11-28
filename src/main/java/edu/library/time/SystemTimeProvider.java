package edu.library.time;

import java.time.LocalDate;

/**
 * Production implementation of {@link TimeProvider} that delegates to {@link LocalDate#now()}.
 */
public class SystemTimeProvider implements TimeProvider {
    @Override
    public LocalDate today() {
        return LocalDate.now();
    }
}
