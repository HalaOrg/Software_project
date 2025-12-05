package edu.library.service;
import edu.library.model.BorrowRecord;
import edu.library.model.Roles;
import edu.library.notification.Observer;
import edu.library.time.TimeProvider;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

class ReminderServiceTest {
    @Test
    void sendReminderForUser_notifiesWhenOverdue() {
        LocalDate today = LocalDate.of(2024, 1, 10);
        TimeProvider timeProvider = () -> today;

        BorrowRecordService borrowRecordService = mock(BorrowRecordService.class);

        BorrowRecord overdueRecord = new BorrowRecord(
                "dana",
                "ISBN-123",
                today.minusDays(3),
                false,
                null
        );

        when(borrowRecordService.getRecords())
                .thenReturn(Arrays.asList(overdueRecord));

        AuthService authService = mock(AuthService.class);

        Roles user = new Roles("dana", "pw", "MEMBER", "dana@example.com");

        Observer observer = mock(Observer.class);

        ReminderService reminderService =
                new ReminderService(borrowRecordService, authService, timeProvider);
        reminderService.addObserver(observer);

        reminderService.sendReminderForUser(user);

        verify(observer).notify(user, "You have 1 overdue item(s).");
    }


    @Test
    void sendReminderForUser_skipsWhenNoOverdue() throws Exception {
        LocalDate today = LocalDate.of(2024, 1, 10);
        TimeProvider timeProvider = () -> today;

        Path tempDir = Files.createTempDirectory("reminders-none");
        Path borrowFile = tempDir.resolve("borrow_records.txt");
        BorrowRecordService borrowRecordService = new BorrowRecordService(borrowFile.toString());
        borrowRecordService.recordBorrow("erin", "ISBN-456", today.plusDays(2));

        Path usersFile = tempDir.resolve("users.txt");
        Files.writeString(usersFile, "erin,pw,MEMBER,erin@example.com\n");
        AuthService authService = new AuthService(usersFile.toString());
        Roles user = authService.getUsers().get(0);

        Observer observer = mock(Observer.class);
        ReminderService reminderService = new ReminderService(borrowRecordService, authService, timeProvider);
        reminderService.addObserver(observer);

        reminderService.sendReminderForUser(user);

        verify(observer, never()).notify(any(), any());
    }

}
