package edu.library.notification;

import edu.library.model.Roles;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class EmailNotifierTest {

    @Test
    void notify_sendsEmailWhenUserAndMessageProvided() {
        EmailServer server = mock(EmailServer.class);
        EmailNotifier notifier = new EmailNotifier(server);
        Roles user = new Roles("alice", "pw", "MEMBER", "alice@example.com");

        notifier.notify(user, "You have 1 overdue book(s).");

        verify(server).sendEmail("alice@example.com", "You have 1 overdue book(s).");
    }

    @Test
    void notify_sendsEmailWhenOverdueCountIsZero() {
        EmailServer server = mock(EmailServer.class);
        EmailNotifier notifier = new EmailNotifier(server);
        Roles user = new Roles("carol", "pw", "MEMBER", "carol@example.com");

        notifier.notify(user, "You have 0 overdue book(s).");

        verify(server, times(1)).sendEmail("carol@example.com", "You have 0 overdue book(s).");
        verifyNoMoreInteractions(server);
    }

    @Test
    void notify_doesNothingWhenUserIsNull() {
        EmailServer server = mock(EmailServer.class);
        EmailNotifier notifier = new EmailNotifier(server);

        notifier.notify(null, "You have 0 overdue book(s).");

        verify(server, never()).sendEmail(anyString(), anyString());
    }

    @Test
    void notify_doesNothingWhenMessageIsNull() {
        EmailServer server = mock(EmailServer.class);
        EmailNotifier notifier = new EmailNotifier(server);
        Roles user = new Roles("bob", "pw", "MEMBER", "bob@example.com");

        notifier.notify(user, null);

        verify(server, never()).sendEmail(anyString(), anyString());
    }
}
