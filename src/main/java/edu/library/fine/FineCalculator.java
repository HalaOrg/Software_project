package edu.library.fine;

import java.util.HashMap;
import java.util.Map;

/**
 * Configurable calculator that delegates fine calculations to registered strategies.
 */
public class FineCalculator {
    public static final String MEDIA_BOOK = "BOOK";
    public static final String MEDIA_CD = "CD";
    public static final String MEDIA_JOURNAL = "JOURNAL";

    private final Map<String, FineStrategy> strategies = new HashMap<>();

    public FineCalculator() {
        registerStrategy(MEDIA_BOOK, new BookFineStrategy());
        registerStrategy(MEDIA_CD, new CDFineStrategy());
        registerStrategy(MEDIA_JOURNAL, new JournalFineStrategy());
    }

    public void registerStrategy(String mediaType, FineStrategy strategy) {
        if (mediaType == null || strategy == null) {
            return;
        }
        strategies.put(mediaType.toUpperCase(), strategy);
    }

    /**
     * Calculate a fine for a given media type.
     *
     * @param mediaType   logical media type (e.g. BOOK)
     * @param overdueDays days overdue
     * @return fine amount using the corresponding strategy, or 0 if none registered
     */
    public int calculate(String mediaType, int overdueDays) {
        if (mediaType == null) {
            return 0;
        }
        FineStrategy strategy = strategies.get(mediaType.toUpperCase());
        if (strategy == null) {
            return 0;
        }
        return strategy.calculateFine(overdueDays);
    }
}
