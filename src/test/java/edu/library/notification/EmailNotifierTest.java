package edu.library.notification;

import edu.library.model.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class EmailNotifierTest {

    private EmailServer mockEmailServer;
    private EmailNotifier emailNotifier;

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        mockEmailServer = mock(EmailServer.class);
        emailNotifier = new EmailNotifier(mockEmailServer);

        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void testNotifyWithValidUserAndMessage() {
        Roles user = new Roles("john_doe", "password", "MEMBER", "john@example.com");
        String message = "Your book is overdue!";

        emailNotifier.notify(user, message);

        verify(mockEmailServer, times(1)).sendEmail("john@example.com", message);

        String consoleOutput = outContent.toString();
        assertTrue(consoleOutput.contains("=== Reminder Email ==="));
        assertTrue(consoleOutput.contains("To user   : john_doe"));
        assertTrue(consoleOutput.contains("Email     : john@example.com"));
        assertTrue(consoleOutput.contains("Message   : Your book is overdue!"));
        assertTrue(consoleOutput.contains("======================"));
    }

    @Test
    void testNotifyWithNullUser() {
        String message = "Your book is overdue!";
        emailNotifier.notify(null, message);

        verify(mockEmailServer, never()).sendEmail(anyString(), anyString());
        assertEquals("", outContent.toString());
    }

    @Test
    void testNotifyWithNullMessage() {
        Roles user = new Roles("john_doe", "password", "MEMBER", "john@example.com");
        emailNotifier.notify(user, null);

        verify(mockEmailServer, never()).sendEmail(anyString(), anyString());
        assertEquals("", outContent.toString());
    }

    @Test
    void testNotifyWithNullUserAndMessage() {
        emailNotifier.notify(null, null);

        verify(mockEmailServer, never()).sendEmail(anyString(), anyString());
        assertEquals("", outContent.toString());
    }

    @BeforeEach
    void tearDown() {
        System.setOut(originalOut);
    }
}
