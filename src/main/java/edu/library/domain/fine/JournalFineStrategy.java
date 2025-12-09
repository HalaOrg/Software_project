package edu.library.domain.fine;

public class JournalFineStrategy implements FineStrategy {
    private static final int RATE_PER_DAY = 15;

    @Override
    public int calculateFine(int overdueDays) {
        if (overdueDays <= 0) {
            return 0;
        }
        return overdueDays * RATE_PER_DAY;
    }
}
