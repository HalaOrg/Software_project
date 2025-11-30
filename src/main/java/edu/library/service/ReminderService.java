package edu.library.service;

import edu.library.model.BorrowRecord;
import edu.library.model.Roles;
import edu.library.notification.EmailNotifier;
import edu.library.notification.EmailServer;
import edu.library.notification.Observer;
import edu.library.notification.SmtpEmailServer;
import edu.library.time.TimeProvider;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReminderService {
    private final BorrowRecordService borrowRecordService;
    private final AuthService authService;
    private final TimeProvider timeProvider;
    private final List<Observer> observers = new ArrayList<>();

    public ReminderService(BorrowRecordService borrowRecordService, AuthService authService, TimeProvider timeProvider) {
        this.borrowRecordService = borrowRecordService;
        this.authService = authService;
        this.timeProvider = timeProvider;
    }

    public void addObserver(Observer observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public void sendReminders() {
        Map<String, Long> overdueCounts = calculateOverdueCounts();
        for (Roles user : authService.getUsers()) {
            sendIfOverdue(user, overdueCounts.getOrDefault(user.getUsername(), 0L));
        }
    }


    public void sendReminderForUser(Roles user) {
        if (user == null) return;
        long overdueCount = countOverdueRecordsForUser(user.getUsername());
        sendIfOverdue(user, overdueCount);
    }

    private Map<String, Long> calculateOverdueCounts() {
        return borrowRecordService.getRecords().stream()
                .filter(record -> getOverdueDays(record) > 0)
                .collect(Collectors.groupingBy(BorrowRecord::getUsername, Collectors.counting()));
    }

    private long countOverdueRecordsForUser(String username) {
        if (username == null) return 0;
        return borrowRecordService.getRecords().stream()
                .filter(record -> username.equals(record.getUsername()))
                .filter(record -> getOverdueDays(record) > 0)
                .count();
    }

    private void sendIfOverdue(Roles user, long overdueCount) {
        if (user == null || overdueCount <= 0) {
            return;
        }

        String message = "You have " + overdueCount + " overdue book(s).";

        if (observers.isEmpty()) {
            Observer defaultNotifier = new EmailNotifier(createDefaultEmailServer());
            observers.add(defaultNotifier);
        }

        for (Observer observer : observers) {
            observer.notify(user, message);
        }
    }


    private EmailServer createDefaultEmailServer() {
        return new SmtpEmailServer();
    }

    public long getOverdueDays(BorrowRecord record) {
        if (record == null || record.getDueDate() == null || record.isReturned()) {
            return 0;
        }
        LocalDate today = timeProvider.today();
        if (!today.isAfter(record.getDueDate())) {
            return 0;
        }
        return ChronoUnit.DAYS.between(record.getDueDate(), today);
    }
}
