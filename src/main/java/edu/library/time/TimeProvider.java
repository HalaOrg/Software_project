package edu.library.time;

import java.time.LocalDate;

/**
 * Abstraction for retrieving the current date to make time-based logic testable.
 */
public interface TimeProvider {
    /**
     * @return the current date according to the provider implementation.
     */
    LocalDate today();
}
