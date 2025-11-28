package edu.library.fine;

/**
 * Strategy for calculating fines based on overdue days.
 */
public interface FineStrategy {
    /**
     * Calculates the fine for a given number of overdue days.
     *
     * @param overdueDays number of days the item is overdue
     * @return fine amount (non-negative)
     */
    int calculateFine(int overdueDays);
}
