package edu.library.service;

import edu.library.model.BorrowRecord;
import edu.library.notification.EmailNotifier;
import edu.library.notification.EmailServer;
import edu.library.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ReminderServiceTest {

    @TempDir
    Path tempDir;

    private BorrowRecordService borrowRecordService;
    private AuthService authService;
    private TimeProvider timeProvider;
    private EmailServer emailServer;

    @BeforeEach
    void setup() {
        borrowRecordService = new BorrowRecordService(tempDir.resolve("borrow_records.txt").toString());
        authService = new AuthService(tempDir.resolve("users.txt").toString());
        authService.addUser("alice", "pw", "MEMBER", "alice@example.com");
        timeProvider = mock(TimeProvider.class);
        emailServer = mock(EmailServer.class);
    }

    @Test
    void sendReminders_notifiesObserversWithOverdueCount() {
        LocalDate today = LocalDate.of(2024, 5, 10);
        when(timeProvider.today()).thenReturn(today);
        borrowRecordService.recordBorrow("alice", "isbn-1", today.minusDays(2));
        ReminderService reminderService = new ReminderService(borrowRecordService, authService, timeProvider);
        reminderService.addObserver(new EmailNotifier(emailServer));

        reminderService.sendReminders();

        verify(emailServer).sendEmail("alice@example.com", "You have 1 overdue book(s).");
    }

    @Test
    void getOverdueDays_usesTimeProvider() {
        LocalDate dueDate = LocalDate.of(2024, 1, 1);
        BorrowRecord record = new BorrowRecord("alice", "isbn-2", dueDate, false, null);
        when(timeProvider.today()).thenReturn(LocalDate.of(2024, 1, 4));
        ReminderService reminderService = new ReminderService(borrowRecordService, authService, timeProvider);

        assertEquals(3, reminderService.getOverdueDays(record));
        verify(timeProvider, times(1)).today();
    }
}
