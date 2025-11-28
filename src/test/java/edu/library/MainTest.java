package edu.library;
import edu.library.model.Book;
import edu.library.model.Roles;
import edu.library.service.AuthService;
import edu.library.service.BookService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;

    @BeforeEach
    void redirectUserDirToTemp() {
        // نخلي كل المسارات النسبية (users.txt, books.txt, ...) تتوجه لـ tempDir
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
    }

    @AfterEach
    void restoreUserDir() {
        System.setProperty("user.dir", originalUserDir);
    }

    /**
     * Helper: يشغّل Main.main بإدخال مزيّف ويعيد الـ output كنص.
     */
    private String runMainWithInput(String input) throws Exception {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayInputStream testIn =
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        try {
            System.setIn(testIn);
            System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));

            Main.main(new String[0]);

        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        return outContent.toString(StandardCharsets.UTF_8);
    }

    @Test
    void option3_exitImmediately() {
        assertDoesNotThrow(() -> {
            try {
                runMainWithInput("3\n");
            } catch (Exception e) {
                fail(e);
            }
        });
    }

    @Test
    void invalidMenuInput_thenExit_doesNotCrash() throws Exception {
        String input = "abc\n3\n";
        String output = runMainWithInput(input);

        assertTrue(output.contains("Invalid input"));
        assertTrue(output.contains("Exiting"));
    }
    @Test
    void signUp_duplicateUsername_showsAlreadyExistsMessage() throws Exception {
        // التشغيل الأول: نعمل تسجيل user1
        String firstRunInput =
                "1\n" +
                        "u1@example.com\n" +
                        "user1\n" +
                        "pass1\n" +
                        "3\n";      // Exit بعد التسجيل الأول

        runMainWithInput(firstRunInput);

        // التشغيل الثاني: نحاول نسجل نفس الاسم user1
        String secondRunInput =
                "1\n" +
                        "u1_new@example.com\n" +
                        "user1\n" +     // نفس ال username
                        "newpass\n" +
                        "3\n";          // Exit

        String output2 = runMainWithInput(secondRunInput);

        // لازم يطلع إنه اليوزر موجود
        assertTrue(output2.contains("Username already exists. Choose another."));
    }


    @Test
    void signUp_success_printsSuccessMessage() throws Exception {
        String input =
                "1\n" +                 // Sign up
                        "\n" +                  // first email empty (should re-prompt)
                        "member@example.com\n" +// valid email
                        "memberUser\n" +        // username
                        "secret\n" +            // password
                        "3\n";                  // Exit

        String output = runMainWithInput(input);

        // تحقق من إعادة طلب الإيميل
        assertTrue(output.contains("Email is required and cannot be empty."));

    }

    @Test
    void login_wrongPassword_showsInvalidCredentialsAndReturnsToMenu() throws Exception {
        // نجهز admin مسبقاً
        AuthService authSetup = new AuthService();
        authSetup.addUser("admin", "1234", "ADMIN", "admin@example.com");

        // نحاول login بكلمة سر غلط ثم نخرج بالخيار 3
        String input =
                "2\n" +        // Login
                        "admin\n" +    // username
                        "wrong\n" +    // password الخاطئة
                        "3\n";         // Exit

        String output = runMainWithInput(input);

        assertTrue(output.contains("Invalid credentials"));
        assertTrue(output.contains("Exiting"));
    }


    @Test
    void findBookByIsbn_returnsMatchingBook() throws Exception {
        BookService service = new BookService();
        service.addBook(new Book("T1", "A1", "ISBN-1"));
        service.addBook(new Book("T2", "A2", "ISBN-2"));

        Method m = Main.class.getDeclaredMethod("findBookByIsbn", BookService.class, String.class);
        m.setAccessible(true);

        Object result = m.invoke(null, service, "ISBN-2");
        assertNotNull(result);
        assertTrue(result instanceof Book);
        Book b = (Book) result;
        assertEquals("ISBN-2", b.getIsbn());
        assertEquals("T2", b.getTitle());
    }

    @Test
    void findBookByIsbn_unknownIsbn_returnsNull() throws Exception {
        BookService service = new BookService();
        service.addBook(new Book("T1", "A1", "ISBN-1"));

        Method m = Main.class.getDeclaredMethod("findBookByIsbn", BookService.class, String.class);
        m.setAccessible(true);

        Object result = m.invoke(null, service, "NOT-EXIST");
        assertNull(result);
    }

    @Test
    void findBookByIsbn_nullIsbn_returnsNull() throws Exception {
        BookService service = new BookService();
        service.addBook(new Book("T1", "A1", "ISBN-1"));

        Method m = Main.class.getDeclaredMethod("findBookByIsbn", BookService.class, String.class);
        m.setAccessible(true);

        Object result = m.invoke(null, service, (String) null);
        assertNull(result);
    }
    @Test
    void loginAsAdmin_thenLogoutFromAdminMenu() throws Exception {
        // نجهّز يوزر admin في ملف users.txt داخل tempDir
        AuthService authSetup = new AuthService();
        authSetup.addUser("admin", "1234", "ADMIN", "admin@example.com");

        // السيناريو:
        // 2  -> Login
        // admin / 1234 -> دخول ناجح
        // 8  -> من منيو Admin: Logout
        // 3  -> من المنيو الرئيسي: Exit
        String input =
                "2\n" +       // Login
                        "admin\n" +   // username
                        "1234\n" +    // password الصحيحة
                        "8\n" +       // منيو Admin: خيار 8 = Logout
                        "3\n";        // بعد ما يرجع للمنيو الرئيسي: Exit

        String output = runMainWithInput(input);

        // نتأكد أنه دخل فعلاً
        assertTrue(output.contains("Logged in as: admin (ADMIN)") ||
                output.contains("Logged in as: admin (ADMIN".replace("Logged in as: ", "✅ Logged in as: ")));

        // نتأكد أنه عمل Logout (رسالة من Admin.handle)
        assertTrue(output.contains("Logged out successfully"));

        // وطلع في الآخر
        assertTrue(output.contains("Exiting"));
    }
    @Test
    void loginAsAdmin_thenExitFromAdminMenu_exitsApplication() throws Exception {
        // نجهّز admin نفسه
        AuthService authSetup = new AuthService();
        authSetup.addUser("admin2", "abcd", "ADMIN", "admin2@example.com");

        // السيناريو:
        // 2  -> Login
        // admin2 / abcd -> دخول
        // 9  -> من منيو Admin: Exit (تعيد 2)
        String input =
                "2\n" +         // Login
                        "admin2\n" +    // username
                        "abcd\n" +      // password
                        "9\n";          // من منيو Admin: خيار 9 = Exit (يرجع 2)

        String output = runMainWithInput(input);

        // نتأكد أنه دخل فعلاً كـ admin
        assertTrue(output.contains("Logged in as: admin2 (ADMIN)") ||
                output.contains("Logged in as: admin2 (ADMIN".replace("Logged in as: ", "✅ Logged in as: ")));

        // الآن Admin.handle لما يختار 9 يرجع 2
        // Main يشوف result == 2 ويطبع "Exiting..." ويعمل return;
        assertTrue(output.contains("Exiting"));
    }
    @Test
    void loginAsMember_thenLogoutFromMemberMenu() throws Exception {
        // تجهيز يوزر member في users.txt داخل tempDir
        AuthService authSetup = new AuthService();
        authSetup.addUser("mem", "111", "MEMBER", "mem@example.com");

        // السيناريو:
        // 2  -> Login
        // mem / 111 -> دخول ناجح
        // 7  -> من منيو Member: Logout
        // 3  -> من المنيو الرئيسي: Exit
        String input =
                "2\n" +      // Main menu: Login
                        "mem\n" +    // username
                        "111\n" +    // password
                        "7\n" +      // Member menu: 7 = Logout (يرجع 1)
                        "3\n";       // Main menu: 3 = Exit

        String output = runMainWithInput(input);

        assertTrue(output.contains("Logged in as: mem (MEMBER)"));
        assertTrue(output.contains("Logged out successfully"));
        assertTrue(output.contains("Exiting"));
    }
    @Test
    void loginAsLibrarian_thenLogoutFromLibrarianMenu() throws Exception {
        // تجهيز يوزر librarian
        AuthService authSetup = new AuthService();
        authSetup.addUser("lib", "222", "LIBRARIAN", "lib@example.com");

        // السيناريو:
        // 2  -> Login
        // lib / 222 -> دخول ناجح
        // 8  -> من منيو Librarian: Logout
        // 3  -> Exit من المنيو الرئيسي
        String input =
                "2\n" +      // Main menu: Login
                        "lib\n" +
                        "222\n" +
                        "8\n" +      // Librarian menu: 8 = Logout (يرجع 1)
                        "3\n";       // Main menu: Exit

        String output = runMainWithInput(input);

        assertTrue(output.contains("Logged in as: lib (LIBRARIAN)"));
        assertTrue(output.contains("Logged out successfully"));
        assertTrue(output.contains("Exiting"));
    }


}
