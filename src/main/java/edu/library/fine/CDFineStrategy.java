package edu.library.fine;

/**
 * Fine calculation strategy for CDs.
 */
public class CDFineStrategy implements FineStrategy {
    private static final int RATE_PER_DAY = 20;

    @Override
    public int calculateFine(int overdueDays) {
        if (overdueDays <= 0) {
            return 0;
        }
        return overdueDays * RATE_PER_DAY;
    }
}
