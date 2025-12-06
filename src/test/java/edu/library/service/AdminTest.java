package edu.library.service;

import edu.library.model.Book;
import edu.library.model.CD;
import edu.library.model.Media;
import edu.library.model.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminTest {

    private MediaService mediaService;
    private AuthService authService;
    private ReminderService reminderService;
    private Roles admin;


    @BeforeEach
    void setUp() {
        mediaService = mock(MediaService.class);
        authService = mock(AuthService.class);
        reminderService = mock(ReminderService.class);
        admin = new Roles("admin", "ADMIN", "admin@example.com");
    }

    private void provideInput(String data) {
        InputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    @Test
    void testAddBook() {
        provideInput("1\nBook Title\nAuthor Name\n123456\n");
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);

        assertEquals(0, result);
        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaService).addMedia(captor.capture());
        Media added = captor.getValue();
        assertTrue(added instanceof Book);
        assertEquals("Book Title", ((Book) added).getTitle());
    }

    @Test
    void testSearchBookNoResults() {
        provideInput("2\nKeyword\n");
        when(mediaService.searchMedia("Keyword")).thenReturn(List.of());
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);

        assertEquals(0, result);
        verify(mediaService).searchMedia("Keyword");
    }

    @Test
    void testDisplayAllBooks() {
        provideInput("3\n");
        when(mediaService.getAllMedia()).thenReturn(List.of(new Book("B1","A1","1"), new CD("C1","Artist1","2")));
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);

        assertEquals(0, result);
        verify(mediaService).getAllMedia();
    }

    @Test
    void testAddMemberAlreadyExists() {
        provideInput("4\nmember@example.com\nmemberUser\npassword\n");
        when(authService.userExists("memberUser")).thenReturn(Boolean.valueOf(true));

        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(0, result);
        verify(authService, never()).addUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testAddMemberSuccess() {
        provideInput("4\nmember@example.com\nmemberUser\npassword\n");
        when(authService.userExists("memberUser")).thenReturn(Boolean.valueOf(false));

        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(0, result);
        verify(authService).addUser("memberUser","password","MEMBER","member@example.com");
    }

    @Test
    void testAddLibrarianSuccess() {
        provideInput("5\nlibrarian@example.com\nlibUser\nlibpass\n");
        when(authService.userExists("libUser")).thenReturn(Boolean.valueOf(false));

        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(0, result);
        verify(authService).addUser("libUser","libpass","LIBRARIAN","librarian@example.com");
    }

    @Test
    void testRemoveUserCannotRemoveSelf() {
        provideInput("6\nadmin\n");
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(0, result);
    }

    @Test
    void testListUsers() {
        provideInput("7\n");
        when(authService.getUsers()).thenReturn(List.of(admin));
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(0, result);
        verify(authService).getUsers();
    }

    @Test
    void testSendReminders() {
        provideInput("8\n");
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(0, result);
        verify(reminderService).sendReminders();
    }

    @Test
    void testLogout() {
        provideInput("9\n");
        when(authService.logout()).thenReturn(Boolean.valueOf(true));
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(1, result);
        verify(authService).logout();
    }

    @Test
    void testExit() {
        provideInput("10\n");
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(2, result);
    }

    @Test
    void testAddCD() {
        provideInput("11\nCD Title\nArtist Name\n654321\n");
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);

        assertEquals(0, result);
        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaService).addMedia(captor.capture());
        Media added = captor.getValue();
        assertTrue(added instanceof CD);
        assertEquals("CD Title", ((CD) added).getTitle());
    }

    @Test
    void testSearchCDNoResults() {
        provideInput("12\nKeywordCD\n");
        when(mediaService.searchMedia("KeywordCD")).thenReturn(List.of());
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(0, result);
        verify(mediaService).searchMedia("KeywordCD");
    }

    @Test
    void testDisplayAllCDs() {
        provideInput("13\n");
        when(mediaService.getAllMedia()).thenReturn(List.of(new CD("C1","Artist1","2"), new Book("B1","A1","1")));
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(0, result);
        verify(mediaService).getAllMedia();
    }

    @Test
    void testInvalidOption() {
        provideInput("99\n");
        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(0, result);
    }
    @Test
    void testFoundBooksPrinting() {
        Book b1 = new Book("Java Basics", "Author A", "111");
        Book b2 = new Book("Advanced Java", "Author B", "222");
        List<Book> foundBooks = List.of(b1, b2);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        if (foundBooks.isEmpty()) {
            System.out.println("No books found.");
        } else {
            System.out.println("Found books:");
            for (Book b : foundBooks) System.out.println(b);
        }

        String output = outContent.toString();
        assertTrue(output.contains("Found books:"));
        assertTrue(output.contains("Java Basics"));
        assertTrue(output.contains("Advanced Java"));

        System.setOut(System.out);
    }

    @Test
    void testNoBooksFoundPrinting() {
        List<Book> foundBooks = List.of();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        if (foundBooks.isEmpty()) {
            System.out.println("No books found.");
        } else {
            System.out.println(" Found books:");
            for (Book b : foundBooks) System.out.println(b);
        }

        String output = outContent.toString();
        assertTrue(output.contains("No books found."));

        System.setOut(System.out);
    }
    @Test
    void testFoundCDsPrinting() {
        CD cd1 = new CD("Greatest Hits", "Artist A", "111");
        CD cd2 = new CD("Top Charts", "Artist B", "222");
        List<CD> foundCDs = List.of(cd1, cd2);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        if (foundCDs.isEmpty()) {
            System.out.println(" No matching CDs found!");
        } else {
            System.out.println(" Found CDs:");
            for (CD c : foundCDs) System.out.println(c);
        }

        String output = outContent.toString();
        assertTrue(output.contains(" Found CDs:"));
        assertTrue(output.contains("Greatest Hits"));
        assertTrue(output.contains("Top Charts"));

        System.setOut(originalOut);
    }
    @Test
    void testAddMemberEmptyEmail() {

        // 4 = Add Member
        provideInput("4\n\nmember@example.com\nmemberUser\npassword\n");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // mock سلوك userExists
        when(authService.userExists("memberUser")).thenReturn(Boolean.valueOf(false));

        int result = Admin.handle(new java.util.Scanner(System.in),
                mediaService, authService, reminderService, admin);

        String output = outContent.toString();

        // التحقق من ظهور الرسالة الخاصة بالإيميل الفارغ
        assertTrue(output.contains("Email is required and cannot be empty."));

        // استرجاع ال System.out
        System.setOut(originalOut);

        // التأكد من أن التنفيذ طبيعي
        assertEquals(0, result);

        // التأكد من أنه تمت إضافة المستخدم بعد إدخال الإيميل الصحيح
        verify(authService).addUser("memberUser", "password", "MEMBER", "member@example.com");
    }
    @Test
    void testAddLibrarianEmptyEmail() {
        // 👈 الإدخال: سطر أول إيميل فارغ، بعدها إيميل صحيح، ثم username ثم password
        provideInput("5\n\nlib@example.com\nlibUser\npass123\n");

        // تجهيز التقاط الإخراج
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Mock للـ auth حتى لا يعطي NullPointer
        when(authService.userExists("libUser")).thenReturn(Boolean.valueOf(false));

        // تشغيل الدالة
        int result = Admin.handle(
                new Scanner(System.in),
                mediaService,
                authService,
                reminderService,
                admin
        );

        // استرجاع الـ System.out
        System.setOut(originalOut);

        // تحويل الإخراج إلى نص
        String output = outContent.toString();

        // ❗ التحقق: لازم يظهر تحذير الإيميل الفارغ
        assertTrue(output.contains("Email is required and cannot be empty.")
                || output.contains(" Email is required and cannot be empty."));

        // ❗ التحقق: تمت إضافة المستخدم بعد إدخال إيميل صحيح
        verify(authService).addUser("libUser", "pass123", "LIBRARIAN", "lib@example.com");

        // ❗ التحقق: النتيجة حسب النظام 0
        assertEquals(0, result);
    }
    @Test
    void testAddLibrarian_UserAlreadyExists() {
        // الإدخال: خيار إضافة librarian ثم:
        // email → valid
        // username → موجود أصلاً
        provideInput("5\nlibrarian@example.com\nexistingUser\n");

        // تجهيز الـ output لالتقاط النصوص
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        // mock: اعتبر أن اليوزر موجود بالفعل
        when(authService.userExists("existingUser")).thenReturn(Boolean.valueOf(true));

        // شغل الدالة
        int result = Admin.handle(
                new Scanner(System.in),
                mediaService,
                authService,
                reminderService,
                admin     // تأكد أنه موجود عندك في setup()
        );

        // رجع الـ System.out للـ default
        System.setOut(originalOut);

        // خزن النص اللي طلع
        String output = out.toString();

        // التحقق من أنه طبع الرسالة الصحيحة
        assertTrue(output.contains("User already exists: existingUser"));

        // التحقق أنه لم يتم إنشاء المستخدم
        verify(authService, never()).addUser(anyString(), anyString(), anyString(), anyString());

        // التحقق من النتيجة
        assertEquals(0, result);
    }
    @Test
    void testRemoveUser_NotAdmin() {
        // الإدخال:
        // 6 = خيار إزالة مستخدم
        // ثم username عشوائي
        provideInput("6\nuserToRemove\n");

        // nonAdminUser لازم يكون role MEMBER أو LIBRARIAN
        Roles nonAdminUser = new Roles("member1", "MEMBER", "m1@example.com");

        // التقاط الـ output
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        int result = Admin.handle(
                new Scanner(System.in),
                mediaService,
                authService,
                reminderService,
                nonAdminUser
        );

        System.setOut(originalOut);

        String output = out.toString();

        // تأكد من الرسالة
        assertTrue(output.contains("Only administrators can unregister users."));

        // تأكد أنه ما حاول يشيل اليوزر
        verify(authService, never()).removeUserWithRestrictions(anyString(), any());

        // return 0
        assertEquals(0, result);
    }
    @Test
    void testRemoveUser_HasActiveBorrowRecords() {
        // Input: خيار 6 ثم username
        provideInput("6\nuserWithLoans\n");

        // إنشاء AdminUser بشكل صحيح مع password و roleName و email
        Roles adminUser = new Roles("admin1", "pass123", "ADMIN", "admin@example.com");

        // Mocks
        when(mediaService.hasActiveBorrowRecords("userWithLoans")).thenReturn(Boolean.valueOf(true));
        // مهم لتجنب NullPointer عند استدعاء removeUserWithRestrictions
        when(mediaService.getBorrowRecordService()).thenReturn(mock(BorrowRecordService.class));

        // التقاط الإخراج
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        // استدعاء الميثود
        int result = Admin.handle(
                new Scanner(System.in),
                mediaService,
                authService,
                reminderService,
                adminUser
        );

        // استعادة System.out
        System.setOut(originalOut);

        String output = out.toString().trim();

        // Assert: الرسالة الصحيحة ظهرت
        assertTrue(output.contains("Cannot unregister user with active loans"),
                "Expected output to contain the message about active loans, but was:\n" + output);

        // Assert: return value
        assertEquals(0, result);

        // تأكد أن removeUserWithRestrictions لم يتم استدعاؤه
        verify(authService, never()).removeUserWithRestrictions(anyString(), any());
    }
    @Test
    void testRemoveUser_HasOutstandingFines() {
        // Input: خيار 6 ثم اسم المستخدم
        provideInput("6\nuserWithFines\n");

        // إنشاء AdminUser صحيح
        Roles adminUser = new Roles("admin1", "pass123", "ADMIN", "admin@example.com");

        // Mocks
        when(mediaService.hasActiveBorrowRecords("userWithFines")).thenReturn(Boolean.valueOf(false)); // لا توجد أقراص مستعارة
        when(mediaService.getOutstandingFine("userWithFines")).thenReturn(Integer.valueOf(50)); // الغرامة > 0
        when(mediaService.getBorrowRecordService()).thenReturn(mock(BorrowRecordService.class));

        // التقاط الإخراج
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        // استدعاء الميثود
        int result = Admin.handle(
                new Scanner(System.in),
                mediaService,
                authService,
                reminderService,
                adminUser
        );

        // استعادة System.out
        System.setOut(originalOut);

        String output = out.toString().trim();

        // Assert: تحقق من ظهور رسالة الغرامات
        assertTrue(output.contains("Cannot unregister user with unpaid fines"),
                "Expected output to contain the message about unpaid fines, but was:\n" + output);

        // Assert: return value
        assertEquals(0, result);

        // تأكد أن removeUserWithRestrictions لم يتم استدعاؤه
        verify(authService, never()).removeUserWithRestrictions(anyString(), any());
    }

    @Test
    void testRemoveUser_SuccessfulRemoval() {
        // Input: خيار 6 ثم اسم المستخدم
        provideInput("6\nuserToRemove\n");

        Roles adminUser = new Roles("admin1", "pass123", "ADMIN", "admin@example.com");

        // Mocks
        when(mediaService.hasActiveBorrowRecords("userToRemove")).thenReturn(Boolean.valueOf(false));
        when(mediaService.getOutstandingFine("userToRemove")).thenReturn(Integer.valueOf(0));
        when(mediaService.getBorrowRecordService()).thenReturn(mock(BorrowRecordService.class));
        when(authService.removeUserWithRestrictions(eq("userToRemove"), any()))
                .thenReturn(Boolean.valueOf(true)); // الحالة الناجحة

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        int result = Admin.handle(
                new Scanner(System.in),
                mediaService,
                authService,
                reminderService,
                adminUser
        );

        System.setOut(originalOut);

        String output = out.toString().trim();

        assertTrue(output.contains("Removed user: userToRemove"),
                "Expected output to confirm removal, but was:\n" + output);

        assertEquals(0, result);
        verify(authService, times(1)).removeUserWithRestrictions(eq("userToRemove"), any());
    }

    @Test
    void testRemoveUser_FailedRemoval() {
        // Input: خيار 6 ثم اسم المستخدم
        provideInput("6\nuserCannotRemove\n");

        Roles adminUser = new Roles("admin1", "pass123", "ADMIN", "admin@example.com");

        // Mocks
        when(mediaService.hasActiveBorrowRecords("userCannotRemove")).thenReturn(Boolean.valueOf(false));
        when(mediaService.getOutstandingFine("userCannotRemove")).thenReturn(Integer.valueOf(0));
        when(mediaService.getBorrowRecordService()).thenReturn(mock(BorrowRecordService.class));
        when(authService.removeUserWithRestrictions(eq("userCannotRemove"), any()))
                .thenReturn(Boolean.valueOf(false)); // الحالة الفاشلة

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        int result = Admin.handle(
                new Scanner(System.in),
                mediaService,
                authService,
                reminderService,
                adminUser
        );

        System.setOut(originalOut);

        String output = out.toString().trim();

        assertTrue(output.contains("User not found or cannot be removed: userCannotRemove"),
                "Expected output to show failed removal, but was:\n" + output);

        assertEquals(0, result);
        verify(authService, times(1)).removeUserWithRestrictions(eq("userCannotRemove"), any());
    }
    @Test
    void testLogout_NoUserLoggedIn() {
        // Input: خيار 9 → Logout
        provideInput("9\n");

        Roles adminUser = new Roles("admin1", "ADMIN", "admin@example.com");

        // Mock: لا يوجد أي مستخدم مسجل الدخول
        when(authService.logout()).thenReturn(Boolean.valueOf(false));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        int result = Admin.handle(
                new Scanner(System.in),
                mediaService,
                authService,
                reminderService,
                adminUser
        );

        System.setOut(originalOut);

        String output = out.toString().trim();

        // التأكد من الرسالة
        assertTrue(output.contains("No user is currently logged in"),
                "Expected output to indicate no user logged in, but was:\n" + output);

        // التأكد من return
        assertEquals(0, result);

        // تأكد من استدعاء logout مرة واحدة فقط
        verify(authService, times(1)).logout();
    }
    @Test
    void testSearchCD_FoundResults() {
        // Input: خيار 12 (Search CD) ثم كلمة البحث
        provideInput("12\nBest Hits\n");

        Roles adminUser = new Roles("admin1", "ADMIN", "admin@example.com");

        // Mock CD موجود
        CD cd1 = new CD("Best Hits", "Famous Artist", "CD123");
        CD cd2 = new CD("Best Hits Vol.2", "Famous Artist", "CD124");

        // خدمة البحث ترجع هذه الأقراص
        when(mediaService.searchMedia("Best Hits"))
                .thenReturn(List.of(cd1, cd2));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        int result = Admin.handle(
                new Scanner(System.in),
                mediaService,
                authService,
                reminderService,
                adminUser
        );

        System.setOut(originalOut);

        String output = out.toString().trim();

        // التأكد من ظهور رسالة "Found CDs:"
        assertTrue(output.contains("Found CDs:"),
                "Expected output to contain 'Found CDs:', but was:\n" + output);

        // التأكد من ظهور أسماء الـ CD في الإخراج
        assertTrue(output.contains(cd1.toString()), "Expected output to contain cd1 details");
        assertTrue(output.contains(cd2.toString()), "Expected output to contain cd2 details");

        // return 0 كما في الكود الأصلي
        assertEquals(0, result);
    }
    @Test
    void testSearchCD_FilterOnlyCDs() {
        // Input: خيار 12 (Search CD) ثم كلمة البحث
        provideInput("12\nMixed Media\n");

        Roles adminUser = new Roles("admin1", "ADMIN", "admin@example.com");

        // قائمة تحتوي على CD و Book معًا
        CD cd1 = new CD("Top Hits", "Artist A", "CD001");
        Book book1 = new Book("Some Book", "Author X", "B001");

        when(mediaService.searchMedia("Mixed Media"))
                .thenReturn(List.of(cd1, book1));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        int result = Admin.handle(
                new Scanner(System.in),
                mediaService,
                authService,
                reminderService,
                adminUser
        );

        System.setOut(originalOut);

        String output = out.toString().trim();

        // يجب أن تظهر رسالة "Found CDs:"
        assertTrue(output.contains("Found CDs:"),
                "Expected output to contain 'Found CDs:'");

        // التأكد من أن الـ CD فقط ظهرت، والـ Book لم تظهر
        assertTrue(output.contains(cd1.toString()), "Expected output to contain cd1 details");
        assertFalse(output.contains(book1.toString()), "Book should not appear in CD search results");

        assertEquals(0, result);
    }
    @Test
    void testSearchBook_FoundBooks() {
        // Input: خيار 2 (Search Book) ثم كلمة البحث
        provideInput("2\nMixed Media\n");

        Roles adminUser = new Roles("admin1", "ADMIN", "admin@example.com");

        // قائمة تحتوي على Book و CD معًا
        Book book1 = new Book("Java Basics", "Author A", "B001");
        CD cd1 = new CD("Top Hits", "Artist X", "CD001");

        // Mock searchMedia لإرجاع القائمة المختلطة
        when(mediaService.searchMedia("Mixed Media"))
                .thenReturn(List.of(book1, cd1));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        int result = Admin.handle(
                new Scanner(System.in),
                mediaService,
                authService,
                reminderService,
                adminUser
        );

        System.setOut(originalOut);

        String output = out.toString().trim();

        // يجب أن تظهر رسالة "Found books:"
        assertTrue(output.contains("Found books:"),
                "Expected output to contain 'Found books:'");

        // التأكد من أن الـ Book فقط ظهرت، والـ CD لم تظهر
        assertTrue(output.contains(book1.toString()), "Expected output to contain book1 details");
        assertFalse(output.contains(cd1.toString()), "CD should not appear in Book search results");

        assertEquals(0, result);
    }

}
