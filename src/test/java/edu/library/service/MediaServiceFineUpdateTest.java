package edu.library.service;

import edu.library.fine.FineCalculator;
import edu.library.model.Book;
import edu.library.model.BorrowRecord;
import edu.library.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MediaServiceFineUpdateTest {

    private MediaService mediaService;
    private FineService fineService;
    private BorrowRecordService borrowRecordService;
    private TimeProvider timeProvider;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Path mediaFile = tempDir.resolve("media.txt");
        Path borrowRecordsFile = tempDir.resolve("borrow_records.txt");
        Path finesFile = tempDir.resolve("fines.txt");

        fineService = new FineService(finesFile.toString());
        borrowRecordService = new BorrowRecordService(borrowRecordsFile.toString());
        timeProvider = mock(TimeProvider.class);

        mediaService = new MediaService(
                mediaFile.toString(),
                borrowRecordService,
                fineService,
                timeProvider,
                new FineCalculator()
        );
    }

    @Test
    void updateFinesOnStartupAddsMissingBalanceForOverdueRecords() {
        // Use in-memory files in the temp directory to avoid touching real data.
        Book overdueBook = new Book("Sample", "Author", "ISBN-123", 1, 1);
        mediaService.addMedia(overdueBook);

        BorrowRecord overdueRecord = new BorrowRecord(
                "bob",
                "ISBN-123",
                LocalDate.of(2024, 1, 1),
                false,
                null
        );
        borrowRecordService.addBorrowRecord(overdueRecord);
        fineService.addFine("bob", 5);

        when(timeProvider.today()).thenReturn(LocalDate.of(2024, 1, 3));

        mediaService.updateFinesOnStartup();

        // Overdue by 2 days => 20 total fine for books. Previously 5, so new balance should be 20.
        assertEquals(20, fineService.getBalance("bob"));
    }
}
