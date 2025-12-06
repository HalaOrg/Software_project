package edu.library.notification;

import edu.library.model.Roles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailNotifierTest {

    private EmailServer mockEmailServer;
    private EmailNotifier emailNotifier;

    // لو حابة تكمّلي التقاط System.out (مش ضروري بعد logger)
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        mockEmailServer = mock(EmailServer.class);
        emailNotifier = new EmailNotifier(mockEmailServer);

        // تقدرِ تشيلي هذول لو مش ناوية تفحص الـ output
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testNotifyWithValidUserAndMessage() {
        Roles user = new Roles("john_doe", "password", "MEMBER", "john@example.com");
        String message = "Your book is overdue!";

        emailNotifier.notify(user, message);

        // أهم شيء: التأكد إنه تم إرسال الإيميل مرة واحدة
        verify(mockEmailServer, times(1))
                .sendEmail("john@example.com", message);
    }

    @Test
    void testNotifyWithNullUser() {
        String message = "Your book is overdue!";

        emailNotifier.notify(null, message);

        // ما بصير يرسل إيميل
        verify(mockEmailServer, never())
                .sendEmail(anyString(), anyString());
    }

    @Test
    void testNotifyWithNullMessage() {
        Roles user = new Roles("john_doe", "password", "MEMBER", "john@example.com");

        emailNotifier.notify(user, null);

        verify(mockEmailServer, never())
                .sendEmail(anyString(), anyString());
    }

    @Test
    void testNotifyWithNullUserAndMessage() {

        emailNotifier.notify(null, null);

        verify(mockEmailServer, never())
                .sendEmail(anyString(), anyString());
    }
}
