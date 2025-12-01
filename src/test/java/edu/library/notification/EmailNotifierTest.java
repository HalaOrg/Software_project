package edu.library.notification;

import edu.library.model.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class EmailNotifierTest {

    private EmailServer emailServerMock;
    private EmailNotifier emailNotifier;

    @BeforeEach
    void setUp() {
        emailServerMock = mock(EmailServer.class);  // عمل Mock لـ EmailServer
        emailNotifier = new EmailNotifier(emailServerMock);
    }

    @Test
    void testNotify_SendsEmail_WhenValidData() {
        // Arrange
        Roles user = new Roles("john", "1234", "MEMBER", "john@example.com");
        String message = "Your book is overdue";

        // Act
        emailNotifier.notify(user, message);

        // Assert
        verify(emailServerMock, times(1))
                .sendEmail("john@example.com", message);
    }

    @Test
    void testNotify_DoesNotSendEmail_WhenUserIsNull() {
        // Act
        emailNotifier.notify(null, "Hello");

        // Assert
        verify(emailServerMock, never()).sendEmail(anyString(), anyString());
    }

    @Test
    void testNotify_DoesNotSendEmail_WhenMessageIsNull() {
        // Arrange
        Roles user = new Roles("john", "1234", "MEMBER", "john@example.com");

        // Act
        emailNotifier.notify(user, null);

        // Assert
        verify(emailServerMock, never()).sendEmail(anyString(), anyString());
    }

    @Test
    void testNotify_UsesCorrectEmail() {
        // Arrange
        Roles user = new Roles("alex", "xx", "LIBRARIAN", "alex@gmail.com");
        String msg = "Reminder";

        // Act
        emailNotifier.notify(user, msg);

        // Assert
        verify(emailServerMock).sendEmail("alex@gmail.com", "Reminder");
    }
}
