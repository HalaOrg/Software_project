package edu.library.service;

import edu.library.fine.FineCalculator;
import edu.library.model.Book;
import edu.library.model.BorrowRecord;
import edu.library.service.BorrowRecordService;
import edu.library.service.FineService;
import edu.library.service.MediaService;
import edu.library.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookServiceRestrictionsTest {

    private MediaService mediaService;
    private BorrowRecordService borrowRecordService;
    private TimeProvider timeProvider;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        Path mediaFile = tempDir.resolve("media.txt");
        Path borrowRecordsFile = tempDir.resolve("borrow_records.txt");
        Path finesFile = tempDir.resolve("fines.txt");

        borrowRecordService = new BorrowRecordService(borrowRecordsFile.toString());
        FineService fineService = new FineService(finesFile.toString());
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
    void borrowFailsWhenUserHasOverdueRecord() {
        Book book = new Book("Overdue Book", "Author", "B-1", 1, 1);
        mediaService.addMedia(book);

        BorrowRecord overdue = new BorrowRecord("alice", book.getIsbn(), LocalDate.of(2024, 1, 1), false, null);
        borrowRecordService.addBorrowRecord(overdue);

        when(timeProvider.today()).thenReturn(LocalDate.of(2024, 2, 1));

        assertFalse(mediaService.borrowBook(book, "alice"));
        assertEquals(1, book.getAvailableCopies());
    }
}