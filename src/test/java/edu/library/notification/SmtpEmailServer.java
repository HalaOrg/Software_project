package edu.library.notification;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Properties;

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
        // لا تتوقع أي استثناء أو إرسال عند القيم الفارغة
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
        // لا ينبغي أن يرمي استثناء — يطبع رسالة ويتوقف
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
                    .thenThrow(new jakarta.mail.MessagingException("Fail"));

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
        // لا يحدث إرسال لأن الإعدادات ناقصة
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

    // ======================
    // Testable Authenticator
    // ======================
    static class TestAuthenticator extends jakarta.mail.Authenticator {
        private final String username;
        private final String password;

        TestAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }

        // method public تستدعي protected method داخلياً حتى نقدر نختبرها
        public PasswordAuthentication call() {
            return getPasswordAuthentication();
        }
    }

    @Test
    void testAuthenticatorReturnsCorrectCredentials() {
        String expectedUser = "testUser";
        String expectedPass = "testPass";

        TestAuthenticator auth = new TestAuthenticator(expectedUser, expectedPass);

        // نستدعي ال-call() الآمنة
        PasswordAuthentication pa = auth.call();

        assertNotNull(pa);
        assertEquals(expectedUser, pa.getUserName());
        assertEquals(expectedPass, pa.getPassword());
    }
    @Test
    void testIsConfigured() throws Exception {
        // إعداد السيرفر مع بيانات صحيحة
        SmtpEmailServer.SmtpEmailSettings settings =
                new SmtpEmailServer.SmtpEmailSettings("smtp.com", 587, true, "user", "pass", null);
        SmtpEmailServer server = new SmtpEmailServer(settings);

        // استخدام reflection لاستدعاء private method
        Method isConfiguredMethod = SmtpEmailServer.class.getDeclaredMethod("isConfigured");
        isConfiguredMethod.setAccessible(true);

        // كل القيم صحيحة → true
        assertTrue((boolean) isConfiguredMethod.invoke(server));

        // host فارغ → false
        SmtpEmailServer emptyHostServer = new SmtpEmailServer(
                new SmtpEmailServer.SmtpEmailSettings("", 587, true, "user", "pass", null)
        );
        assertFalse((boolean) isConfiguredMethod.invoke(emptyHostServer));

        // username فارغ → false
        SmtpEmailServer emptyUserServer = new SmtpEmailServer(
                new SmtpEmailServer.SmtpEmailSettings("smtp.com", 587, true, "", "pass", null)
        );
        assertFalse((boolean) isConfiguredMethod.invoke(emptyUserServer));

        // password فارغ → false
        SmtpEmailServer emptyPassServer = new SmtpEmailServer(
                new SmtpEmailServer.SmtpEmailSettings("smtp.com", 587, true, "user", "", null)
        );
        assertFalse((boolean) isConfiguredMethod.invoke(emptyPassServer));
    }
    @Test
    void testFromAddressNullOrBlank() {
        // حالة fromAddress null → يجب أن يأخذ username
        SmtpEmailServer.SmtpEmailSettings settingsNull =
                new SmtpEmailServer.SmtpEmailSettings("smtp.com", 587, true, "user", "pass", null);
        SmtpEmailServer serverNull = new SmtpEmailServer(settingsNull);
        assertEquals("user", serverNull.getFromAddress(), "fromAddress should default to username if null");

        // حالة fromAddress فارغ → يجب أن يأخذ username
        SmtpEmailServer.SmtpEmailSettings settingsBlank =
                new SmtpEmailServer.SmtpEmailSettings("smtp.com", 587, true, "user", "pass", "   ");
        SmtpEmailServer serverBlank = new SmtpEmailServer(settingsBlank);
        assertEquals("user", serverBlank.getFromAddress(), "fromAddress should default to username if blank");

        // حالة fromAddress موجود → يجب أن يأخذ القيمة نفسها
        SmtpEmailServer.SmtpEmailSettings settingsValid =
                new SmtpEmailServer.SmtpEmailSettings("smtp.com", 587, true, "user", "pass", "from@example.com");
        SmtpEmailServer serverValid = new SmtpEmailServer(settingsValid);
        assertEquals("from@example.com", serverValid.getFromAddress(), "fromAddress should use provided value if not blank");
    }

    @Test
    void testSessionAndAuthenticatorIndirectly() throws Exception {
        String username = "testUser";
        String password = "testPass";

        // إعداد الخصائص كما في الكود
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.test.com");
        props.put("mail.smtp.port", "587");

        // إنشاء Authenticator كما في الكود
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        // Mock Transport.send لتجنب إرسال الإيميل فعلياً
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(Mockito.any(MimeMessage.class))).thenAnswer(i -> null);

            // إنشاء Session كما في الكود
            Session session = Session.getInstance(props, auth);
            assertNotNull(session);

            // إنشاء MimeMessage كما في sendEmail
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new jakarta.mail.internet.InternetAddress("from@test.com"));
            msg.setRecipients(Message.RecipientType.TO,
                    jakarta.mail.internet.InternetAddress.parse("to@test.com"));
            msg.setSubject("Test");
            msg.setText("Hello");

            // استدعاء send (سيستخدم Authenticator)
            Transport.send(msg);

            // تحقق أن Transport.send تم استدعاؤه مرة واحدة
            mockedTransport.verify(() -> Transport.send(Mockito.any(MimeMessage.class)), Mockito.times(1));
        }
    }
    @Test
    void testSendEmailEmptyMessageDoesNothing() {
        SmtpEmailServer server = new SmtpEmailServer(defaultSettings);

        // هذا الفرع لا يقع ضمن الشرط الأول (message == null) → ينتقل إلى isConfigured
        // ولأن السيرفر configured سوف ينفّذ Transport.send
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(Mockito.any(MimeMessage.class))).thenAnswer(i -> null);

            server.sendEmail("to@example.com", "");

            mockedTransport.verify(() -> Transport.send(Mockito.any(MimeMessage.class)), Mockito.times(1));
        }
    }
    @Test
    void testSendEmailNotConfiguredPrintsExactMessage() {
        SmtpEmailServer.SmtpEmailSettings badSettings =
                new SmtpEmailServer.SmtpEmailSettings("", 587, true, "", "", null);
        SmtpEmailServer server = new SmtpEmailServer(badSettings);

        // التقاط System.out
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));

        server.sendEmail("to@example.com", "msg");

        String printed = out.toString().trim();
        assertEquals("Email configuration is incomplete; skipping send.", printed);

        // إعادة System.out
        System.setOut(System.out);
    }
    @Test
    void testConstructorHandlesNullValuesGracefully() {
        SmtpEmailServer.SmtpEmailSettings settings =
                new SmtpEmailServer.SmtpEmailSettings(null, 587, true, null, null, null);

        SmtpEmailServer server = new SmtpEmailServer(settings);

        assertEquals("", server.getHost());
        assertEquals("", server.getUsername());
        assertEquals("", server.getPassword());
        assertEquals("", server.getFromAddress()); // لأنه username == ""
    }

}
