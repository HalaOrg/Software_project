package edu.library.notification;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class SmtpEmailServerUltimateTest {

    private SmtpEmailServer.SmtpEmailSettings defaultSettings;

    @BeforeEach
    void setUp() {
        defaultSettings = new SmtpEmailServer.SmtpEmailSettings(
                "smtp.example.com", 587, true, "user", "pass", null
        );
    }

    @Test
    void testConstructorSetsFromAddressWhenNull() {
        SmtpEmailServer server = new SmtpEmailServer(defaultSettings);
        assertEquals("user", server.getFromAddress());
    }

    @Test
    void testConstructorWithNonNullFromAddress() {
        SmtpEmailServer.SmtpEmailSettings settings = new SmtpEmailServer.SmtpEmailSettings(
                "host.com", 587, true, "user", "pass", "from@host.com"
        );
        SmtpEmailServer server = new SmtpEmailServer(settings);
        assertEquals("from@host.com", server.getFromAddress());
    }

    @Test
    void testDefaultConstructorAndGetters() {
        SmtpEmailServer server = new SmtpEmailServer();
        assertNotNull(server.getHost());
        assertNotNull(server.getUsername());
        assertNotNull(server.getPassword());
        assertNotNull(server.getFromAddress());
        // تحقق من fallback
        assertEquals(server.getUsername(), server.getFromAddress());
        assertEquals("alaasawalhh14@gmail.com", server.getUsername());
        assertEquals("alaasawalhh14@gmail.com", server.getFromAddress());
    }

    @Test
    void testSendEmailReturnsEarlyForNullOrBlankParams() {
        SmtpEmailServer server = new SmtpEmailServer(defaultSettings);
        server.sendEmail(null, "msg");
        server.sendEmail("", "msg");
        server.sendEmail("   ", "msg");
        server.sendEmail("to@example.com", null);
    }

    @Test
    void testSendEmailNotConfiguredPrintsMessage() {
        SmtpEmailServer.SmtpEmailSettings badSettings =
                new SmtpEmailServer.SmtpEmailSettings("", 587, true, "", "", null);
        SmtpEmailServer server = new SmtpEmailServer(badSettings);
        server.sendEmail("to@example.com", "Hello");
    }

    @Test
    void testSendEmailConfiguredWithMockTransportAndAuthenticator() throws Exception {
        SmtpEmailServer server = new SmtpEmailServer(defaultSettings);

        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(Mockito.any(MimeMessage.class))).thenAnswer(i -> null);

            server.sendEmail("receiver@example.com", "Test message");

            assertEquals("user", server.getFromAddress());
            assertEquals("user", server.getUsername());
            assertEquals("smtp.example.com", server.getHost());

            mockedTransport.verify(() -> Transport.send(Mockito.any(MimeMessage.class)), Mockito.times(1));
        }
    }


    @Test
    void testSendEmailThrowsRuntimeException() throws Exception {
        SmtpEmailServer server = new SmtpEmailServer(
                new SmtpEmailServer.SmtpEmailSettings("host.com", 587, true, "user", "pass", "from@example.com")
        );

        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(Mockito.any(MimeMessage.class)))
                    .thenThrow(new MessagingException("Fail"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> server.sendEmail("to@example.com", "msg"));
            assertTrue(ex.getMessage().contains("Failed to send email"));
        }
    }

    @Test
    void testIsConfiguredBranches() throws Exception {
        SmtpEmailServer emptyServer = new SmtpEmailServer(
                new SmtpEmailServer.SmtpEmailSettings("", 587, true, "", "", null)
        );
        emptyServer.sendEmail("to@example.com", "msg");

        SmtpEmailServer validServer = new SmtpEmailServer(
                new SmtpEmailServer.SmtpEmailSettings("smtp.example.com", 587, true, "user", "pass", "from@example.com")
        );
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(Mockito.any(MimeMessage.class))).thenAnswer(i -> null);
            validServer.sendEmail("to@example.com", "msg");
            mockedTransport.verify(() -> Transport.send(Mockito.any(MimeMessage.class)), Mockito.times(1));
        }
    }
}
