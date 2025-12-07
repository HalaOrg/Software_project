/*
package edu.library;

import edu.library.model.Roles;
import edu.library.service.*;
import edu.library.time.SystemTimeProvider;
import edu.library.fine.FineCalculator;
import edu.library.notification.EmailNotifier;
import edu.library.notification.SmtpEmailServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicesTest {

    private MediaService mediaService;
    private FineService fineService;
    private BorrowRecordService borrowService;
    private AuthService authService;
    private ReminderService reminderService;

    @BeforeEach
    void setUp() {
        fineService = new FineService();
        borrowService = new BorrowRecordService();

        mediaService = new MediaService(
                "media.txt",
                borrowService,
                fineService,
                new SystemTimeProvider(),
                new FineCalculator()
        );

        authService = new AuthService(fineService);

        reminderService = new ReminderService(borrowService, authService, new SystemTimeProvider());
        reminderService.addObserver(new EmailNotifier(new SmtpEmailServer()));
    }

    @Test
    void testAuthServiceAddAndLogin() {
        authService.addUser("user1", "pass1", "MEMBER", "user1@test.com");
        Roles user = authService.login("user1", "pass1");
        assertNotNull(user);
        assertEquals("user1", user.getUsername());

        Roles invalid = authService.login("user1", "wrongpass");
        assertNull(invalid);
    }

    @Test
    void testReminderServiceObserver() {
        authService.addUser("user2", "pass2", "MEMBER", "user2@test.com");
        Roles user = authService.login("user2", "pass2");
        assertNotNull(user);

        // مجرد التأكد أنه ما يطيح استثناء عند إرسال تذكير
        assertDoesNotThrow(() -> reminderService.sendReminderForUser(user));
    }

    @Test
    void testMediaServiceAddBookAndFind() {
        // تنظيف قائمة الكتب قبل الاختبار لضمان environment نظيفة
        mediaService.getItems().clear(); // استخدم getItems بدل getBooks() لضمان شمول كل الميديا

        mediaService.addBook("Book A", "Author A", "12345", 1);

        // تحقق أن عدد الكتب = 1
        assertEquals(1, mediaService.getBooks().size());

        // تحقق أن الكتاب الموجود هو نفسه
        boolean exists = mediaService.getBooks().stream()
                .anyMatch(b -> b.getIsbn().equals("12345"));
        assertTrue(exists);
    }



    @Test
    void testBorrowRecordService() {
        // إضافة سجل استعارة وهمي والتأكد
        assertDoesNotThrow(() -> borrowService.addBorrowRecord("user1", "12345"));
        List<?> active = borrowService.getActiveBorrowRecordsForUser("user1");
        assertEquals(1, active.size());
    }
}*/

