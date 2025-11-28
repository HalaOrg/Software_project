package edu.library.service;

import edu.library.model.Roles;
import edu.library.notification.Observer;
import edu.library.time.TimeProvider;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReminderServiceTest {

    @Test
    void sendReminderForUser_notifiesWhenOverdue() throws Exception {
        LocalDate today = LocalDate.of(2024, 1, 10);
        TimeProvider timeProvider = () -> today;

        Path tempDir = Files.createTempDirectory("reminders");
        Path borrowFile = tempDir.resolve("borrow_records.txt");
        BorrowRecordService borrowRecordService = new BorrowRecordService(borrowFile.toString());
        borrowRecordService.recordBorrow("dana", "ISBN-123", today.minusDays(3));

        Path usersFile = tempDir.resolve("users.txt");
        Files.writeString(usersFile, "dana,pw,MEMBER,dana@example.com\n");
        AuthService authService = new AuthService(usersFile.toString());
        Roles user = authService.getUsers().get(0);

        Observer observer = mock(Observer.class);
        ReminderService reminderService = new ReminderService(borrowRecordService, authService, timeProvider);
        reminderService.addObserver(observer);

        reminderService.sendReminderForUser(user);

        verify(observer).notify(user, "You have 1 overdue book(s).");
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
