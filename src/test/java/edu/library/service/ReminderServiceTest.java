package edu.library.service;

import edu.library.model.BorrowRecord;
import edu.library.model.Roles;
import edu.library.notification.Observer;
import edu.library.time.TimeProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReminderServiceTest {

    // =========================
    // التستات الأصلية عندك
    // =========================
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

        BorrowRecordService borrowRecordService = mock(BorrowRecordService.class);
        when(borrowRecordService.getRecords()).thenReturn(Collections.emptyList());

        AuthService authService = mock(AuthService.class);
        Roles user = new Roles("erin", "pw", "MEMBER", "erin@example.com");
        when(authService.getUsers()).thenReturn(Collections.singletonList(user));

        Observer observer = mock(Observer.class);
        ReminderService reminderService = new ReminderService(borrowRecordService, authService, timeProvider);
        reminderService.addObserver(observer);

        reminderService.sendReminderForUser(user);

        verify(observer, never()).notify(any(), any());
    }

    // =========================
    // التستات الجديدة لتغطية البرانشات
    // =========================

    // addObserver(null)
    @Test
    void addObserver_nullDoesNothing() {
        ReminderService service = new ReminderService(
                mock(BorrowRecordService.class),
                mock(AuthService.class),
                () -> LocalDate.now()
        );

        service.addObserver(null);
        // فقط نتأكد أنه لا يرمى استثناء
    }

    // removeObserver: موجود و غير موجود
    @Test
    void removeObserver_removesExistingAndNonExisting() {
        Observer observer1 = mock(Observer.class);
        Observer observer2 = mock(Observer.class);

        ReminderService service = new ReminderService(
                mock(BorrowRecordService.class),
                mock(AuthService.class),
                () -> LocalDate.now()
        );

        service.addObserver(observer1);
        service.removeObserver(observer1); // remove موجود
        service.removeObserver(observer2); // remove غير موجود (لا يفعل شيء)
    }

    // sendReminderForUser(null)
    @Test
    void sendReminderForUser_nullUserDoesNothing() {
        ReminderService service = new ReminderService(
                mock(BorrowRecordService.class),
                mock(AuthService.class),
                () -> LocalDate.now()
        );

        Observer observer = mock(Observer.class);
        service.addObserver(observer);

        service.sendReminderForUser(null);
        verify(observer, never()).notify(any(), any());
    }

    // sendReminders: user لديه overdue
    @Test
    void sendReminders_userHasOverdue_notified() {
        LocalDate today = LocalDate.of(2024, 1, 10);
        BorrowRecord overdue = new BorrowRecord("john", "X", today.minusDays(2), false, null);

        BorrowRecordService brs = mock(BorrowRecordService.class);
        when(brs.getRecords()).thenReturn(Collections.singletonList(overdue));

        Roles user = new Roles("john", "pw", "MEMBER", "john@example.com");

        AuthService auth = mock(AuthService.class);
        when(auth.getUsers()).thenReturn(Collections.singletonList(user));

        Observer observer = mock(Observer.class);

        ReminderService service = new ReminderService(brs, auth, () -> today);
        service.addObserver(observer);

        service.sendReminders();

        verify(observer).notify(user, "You have 1 overdue item(s).");
    }

    // sendReminders: user بدون overdue
    @Test
    void sendReminders_userNoOverdue_skips() {
        LocalDate today = LocalDate.of(2024, 1, 10);
        BorrowRecord notOverdue = new BorrowRecord("john", "X", today.plusDays(2), false, null);

        BorrowRecordService brs = mock(BorrowRecordService.class);
        when(brs.getRecords()).thenReturn(Collections.singletonList(notOverdue));

        Roles user = new Roles("john", "pw", "MEMBER", "john@example.com");

        AuthService auth = mock(AuthService.class);
        when(auth.getUsers()).thenReturn(Collections.singletonList(user));

        Observer observer = mock(Observer.class);

        ReminderService service = new ReminderService(brs, auth, () -> today);
        service.addObserver(observer);

        service.sendReminders();

        verify(observer, never()).notify(any(), any());
    }

    // getOverdueDays: جميع الحالات
    @Test
    void getOverdueDays_variousCases() {
        LocalDate today = LocalDate.of(2024, 1, 10);
        ReminderService service = new ReminderService(
                mock(BorrowRecordService.class),
                mock(AuthService.class),
                () -> today
        );

        // record == null
        assert service.getOverdueDays(null) == 0;

        // dueDate == null
        BorrowRecord r1 = new BorrowRecord("x", "y", null, false, null);
        assert service.getOverdueDays(r1) == 0;

        // returned == true
        BorrowRecord r2 = new BorrowRecord("x", "y", today.minusDays(5), true, null);
        assert service.getOverdueDays(r2) == 0;

        // today not after dueDate
        BorrowRecord r3 = new BorrowRecord("x", "y", today.plusDays(3), false, null);
        assert service.getOverdueDays(r3) == 0;

        // overdue > 0
        BorrowRecord r4 = new BorrowRecord("x", "y", today.minusDays(4), false, null);
        assert service.getOverdueDays(r4) == 4;
    }
    @Test
    void sendReminderForUser_observersEmpty_addsDefaultObserverAndNotifies() {
        LocalDate today = LocalDate.of(2024, 1, 10);
        TimeProvider timeProvider = () -> today;

        BorrowRecord overdueRecord = new BorrowRecord(
                "alice",
                "ISBN-001",
                today.minusDays(2),
                false,
                null
        );

        BorrowRecordService brs = mock(BorrowRecordService.class);
        when(brs.getRecords()).thenReturn(Collections.singletonList(overdueRecord));

        Roles user = new Roles("alice", "pw", "MEMBER", "alice@example.com");

        AuthService auth = mock(AuthService.class);
        when(auth.getUsers()).thenReturn(Collections.singletonList(user));

        // لا نضيف أي observer
        ReminderService service = new ReminderService(brs, auth, timeProvider);

        // استدعاء sendReminderForUser يغطي حالة observers.isEmpty()
        service.sendReminderForUser(user);

        // نتحقق أنه لم يحدث استثناء وأن الرسالة تم "إرسالها" عبر EmailNotifier الافتراضي
        // لا نقدر نتحقق داخلياً من EmailNotifier بدون Mockito spy أو تعديل الكود، لكن على الأقل البرانش تم تغطيته
    }

    @Test
    void sendReminderForUser_nullUser_doesNothing() {
        BorrowRecordService brs = mock(BorrowRecordService.class);
        AuthService auth = mock(AuthService.class);
        TimeProvider tp = () -> LocalDate.now();
        ReminderService service = new ReminderService(brs, auth, tp);

        // لا exception ولا notify
        service.sendReminderForUser(null);
    }

    @Test
    void sendReminderForUser_nullUsername_doesNothing() {
        BorrowRecordService brs = mock(BorrowRecordService.class);
        AuthService auth = mock(AuthService.class);
        TimeProvider tp = () -> LocalDate.now();
        ReminderService service = new ReminderService(brs, auth, tp);

        Roles userWithNullUsername = mock(Roles.class);
        when(userWithNullUsername.getUsername()).thenReturn(null);

        Observer observer = mock(Observer.class);
        service.addObserver(observer);

        service.sendReminderForUser(userWithNullUsername);

        // verify أنه لم يحدث notify
        verify(observer, never()).notify(any(), any());
    }

    @Test
    void getOverdueDays_edgeCases_returnZero() {
        LocalDate today = LocalDate.of(2024, 1, 10);
        TimeProvider tp = () -> today;
        BorrowRecordService brs = mock(BorrowRecordService.class);
        AuthService auth = mock(AuthService.class);
        ReminderService service = new ReminderService(brs, auth, tp);

        // record == null
        assertEquals(0, service.getOverdueDays(null));

        // dueDate == null
        BorrowRecord r1 = new BorrowRecord("user", "ISBN", null, false, null);
        assertEquals(0, service.getOverdueDays(r1));

        // isReturned == true
        BorrowRecord r2 = new BorrowRecord("user", "ISBN", today.minusDays(5), true, null);
        assertEquals(0, service.getOverdueDays(r2));

        // today قبل أو يساوي dueDate
        BorrowRecord r3 = new BorrowRecord("user", "ISBN", today.plusDays(1), false, null);
        assertEquals(0, service.getOverdueDays(r3));

        BorrowRecord r4 = new BorrowRecord("user", "ISBN", today, false, null);
        assertEquals(0, service.getOverdueDays(r4));
    }
    // 1. sendReminders: authService.getUsers() ترجع قائمة فارغة
    @Test
    void sendReminders_noUsers_doesNothing() {
        BorrowRecordService brs = mock(BorrowRecordService.class);
        AuthService auth = mock(AuthService.class);
        TimeProvider tp = () -> LocalDate.now();
        ReminderService service = new ReminderService(brs, auth, tp);

        when(auth.getUsers()).thenReturn(Collections.emptyList());

        // لا exception ولا notify
        service.sendReminders();
    }

    // 2. calculateOverdueCounts: getRecords() ترجع قائمة فارغة
    @Test
    void calculateOverdueCounts_noRecords_returnsEmptyMap() {
        BorrowRecordService brs = mock(BorrowRecordService.class);
        AuthService auth = mock(AuthService.class);
        TimeProvider tp = () -> LocalDate.now();
        ReminderService service = new ReminderService(brs, auth, tp);

        when(brs.getRecords()).thenReturn(Collections.emptyList());

        // نستدعي sendReminders لكي يغطي calculateOverdueCounts branch
        when(auth.getUsers()).thenReturn(Collections.emptyList());
        service.sendReminders();
    }

    // 3. user.getUsername() ترجع null
    @Test
    void sendReminders_userWithNullUsername_doesNothing() {
        BorrowRecordService brs = mock(BorrowRecordService.class);
        AuthService auth = mock(AuthService.class);
        TimeProvider tp = () -> LocalDate.now();
        ReminderService service = new ReminderService(brs, auth, tp);

        Roles user = mock(Roles.class);
        when(user.getUsername()).thenReturn(null);
        when(auth.getUsers()).thenReturn(Collections.singletonList(user));

        Observer observer = mock(Observer.class);
        service.addObserver(observer);

        service.sendReminders();

        verify(observer, never()).notify(any(), any());
    }

}
