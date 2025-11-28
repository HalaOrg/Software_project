package edu.library.service;

import edu.library.model.BorrowRecord;
import edu.library.model.Roles;
import edu.library.notification.Observer;
import edu.library.time.TimeProvider;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for identifying overdue records and notifying observers.
 */
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

    /**
     * Send reminder messages to all users who currently have overdue borrow records.
     */
    public void sendReminders() {
        Map<String, Long> overdueCounts = calculateOverdueCounts();
        for (Roles user : authService.getUsers()) {
            sendIfOverdue(user, overdueCounts.getOrDefault(user.getUsername(), 0L));
        }
    }

    /**
     * Send a reminder for a single user (e.g., immediately after they log in).
     *
     * @param user target member
     */
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
        String message = String.format("You have %d overdue book(s).", overdueCount);
        for (Observer observer : observers) {
            observer.notify(user, message);
        }
    }

    /**
     * Calculate the overdue days for a particular record using the configured time provider.
     *
     * @param record borrow record to evaluate
     * @return overdue days (0 when not overdue)
     */
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
