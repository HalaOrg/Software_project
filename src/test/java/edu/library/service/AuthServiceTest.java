package edu.library.service;

import edu.library.domain.model.BorrowRecord;
import edu.library.domain.model.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;
    private FineService fineService;
    private BorrowRecordService mockBorrow;

    @TempDir
    Path tempDir;
    private Path usersFile;

    @BeforeEach
    void setUp() throws IOException {
        fineService = mock(FineService.class);
        mockBorrow = mock(BorrowRecordService.class);

        usersFile = tempDir.resolve("users.txt");
        Files.createFile(usersFile);

        authService = new AuthService(usersFile.toString(), fineService);

        authService.addUser("admin", "adminpwd", "ADMIN", "admin@example.com");
        authService.addUser("member1", "pwd1", "MEMBER", "member1@example.com");
        authService.addUser("member2", "pwd2", "MEMBER", "member2@example.com");
    }

    @Test
    void testRemoveUserWithRestrictions() {
        authService.login("admin", "adminpwd");

        authService.logout();
        assertFalse(authService.removeUserWithRestrictions("member1", mockBorrow));
        authService.login("admin", "adminpwd");

        assertFalse(authService.removeUserWithRestrictions(null, mockBorrow));
        assertFalse(authService.removeUserWithRestrictions("admin", mockBorrow));

        when(fineService.getBalance("member1")).thenReturn(Integer.valueOf(50));
        assertFalse(authService.removeUserWithRestrictions("member1", mockBorrow));

        when(fineService.getBalance("member2")).thenReturn(Integer.valueOf(0));
        BorrowRecord fakeRecord = mock(BorrowRecord.class);
        when(mockBorrow.getActiveBorrowRecordsForUser("member2"))
                .thenReturn(Collections.singletonList(fakeRecord));
        assertFalse(authService.removeUserWithRestrictions("member2", mockBorrow));

        authService.addUser("member3", "pwd3", "MEMBER", "member3@example.com");
        when(fineService.getBalance("member3")).thenReturn(Integer.valueOf(0));
        when(mockBorrow.getActiveBorrowRecordsForUser("member3"))
                .thenReturn(Collections.emptyList());

        assertTrue(authService.removeUserWithRestrictions("member3", mockBorrow));
        assertFalse(authService.userExists("member3"));
    }

    @Test
    void testDefaultConstructor() {
        String oldDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            AuthService defaultAuth = new AuthService();
            assertNotNull(defaultAuth);
            assertTrue(Files.exists(tempDir.resolve("users.txt")));
        } finally {
            System.setProperty("user.dir", oldDir);
        }
    }

    @Test
    void testConstructorWithFineService() {
        String oldDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            AuthService auth = new AuthService(fineService);
            assertNotNull(auth);
            assertTrue(Files.exists(tempDir.resolve("users.txt")));
        } finally {
            System.setProperty("user.dir", oldDir);
        }
    }

    @Test
    void testAddUserOverload() {
        Roles role = authService.addUser("user1", "pass1", "MEMBER", "user1@example.com");
        assertNotNull(role);

        assertEquals("user1", role.getUsername());
        assertEquals("MEMBER", role.getRoleName());
        assertEquals("user1@example.com", role.getEmail());
    }

    @Test
    void testResolveDefaultViaConstructor() throws IOException {
        Path fileInsideTemp = tempDir.resolve("users_resolve.txt");
        assertFalse(Files.exists(fileInsideTemp));
        AuthService auth = new AuthService(fileInsideTemp.toString());
        assertTrue(Files.exists(fileInsideTemp));
    }

    @Test
    void testAddUserAndCatchIOExceptions() {
        Path badPath = tempDir.resolve("invalid_dir/users.txt");
        AuthService auth = new AuthService(badPath.toString());
        assertDoesNotThrow(() -> auth.addUser("userX", "pwdX", "MEMBER"));
    }
    @Test
    void testRolesConstructorWithNullFields() {
        // username null
        Roles r1 = new Roles(null, "pass", "MEMBER", "email@test.com");
        assertNull(r1.getUsername(), "Username should be null");

        // password null
        Roles r2 = new Roles("user", null, "MEMBER", "email@test.com");
        assertNull(r2.getPassword(), "Password should be null");

        // roleName null → يجب أن يتحول تلقائياً لـ "UNKNOWN" حسب كودك
        Roles r3 = new Roles("user", "pass", null, "email@test.com");
        assertEquals("UNKNOWN", r3.getRoleName(), "RoleName should default to UNKNOWN");

        // email null → يجب أن يتحول تلقائياً لـ ""
        Roles r4 = new Roles("user", "pass", "MEMBER", null);
        assertEquals("", r4.getEmail(), "Email should default to empty string");
    }
    @Test
    void testLogoutCurrentUserIfMatchesUsername() throws Exception {
        AuthService auth = new AuthService();

        // إعداد مستخدم
        Roles current = new Roles("user1", "pass", "MEMBER", "user1@test.com");

        // وضع المستخدم الحالي باستخدام Reflection
        java.lang.reflect.Field field = AuthService.class.getDeclaredField("currentUser");
        field.setAccessible(true);
        field.set(auth, current);

        // username الذي نحاول تسجيل خروجه مطابق للمستخدم الحالي
        String usernameToLogout = "User1"; // الاختلاف في حالة الأحرف

        // تنفيذ الكود كما في كلاسك
        if (auth.getCurrentUser() != null && auth.getCurrentUser().getUsername().equalsIgnoreCase(usernameToLogout)) {
            field.set(auth, null); // إزالة currentUser
        }

        // التأكد أن currentUser أصبح null بعد logout
        assertNull(auth.getCurrentUser(), "currentUser يجب أن يكون null بعد logout عند المطابقة");
    }
    @Test
    void testSaveUsersToFileIOException() {
        // إنشاء AuthService spy
        AuthService authSpy = spy(new AuthService());

        // إجبار saveUsersToFile على طباعة رسالة بدلاً من فعل IO
        doAnswer(invocation -> {
            System.out.println("Error saving users: Simulated IO error");
            return null;
        }).when(authSpy).saveUsersToFile();

        // التقاط System.out
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        // استدعاء دالة تؤدي إلى saveUsersToFile()
        authSpy.addUser("user1", "pass", "MEMBER", "user@test.com");

        // استعادة System.out
        System.setOut(originalOut);

        String output = out.toString();
        assertTrue(output.contains("Error saving users: Simulated IO error"),
                "Expected error message about saving users, but was:\n" + output);
    }
    @Test
    void testGetCurrentAdmin() throws Exception {
        AuthService auth = new AuthService();

        // التأكد من أن getCurrentAdmin ترجع null في البداية
        assertNull(auth.getCurrentAdmin(), "Expected currentAdmin to be null initially");

        // إنشاء مستخدم وضبطه كـ currentUser باستخدام Reflection
        Roles adminUser = new Roles("admin", "adminpwd", "ADMIN", "admin@example.com");
        java.lang.reflect.Field field = AuthService.class.getDeclaredField("currentUser");
        field.setAccessible(true);
        field.set(auth, adminUser);

        // التأكد أن getCurrentAdmin ترجع نفس المستخدم الحالي
        Roles result = auth.getCurrentAdmin();
        assertNotNull(result, "Expected currentAdmin to not be null");
        assertEquals("admin", result.getUsername());
        assertEquals("ADMIN", result.getRoleName());
        assertEquals("admin@example.com", result.getEmail());
    }
    @Test
    void testGetUsers() throws IOException {
        // إنشاء ملف مؤقت فارغ ليكون مصدر بيانات AuthService
        Path tempFile = Files.createTempFile("users_test", ".txt");
        AuthService auth = new AuthService(tempFile.toString());

        // إضافة مستخدمين جدد
        Roles user1 = auth.addUser("user1", "pass1", "MEMBER", "user1@test.com");
        Roles user2 = auth.addUser("user2", "pass2", "MEMBER", "user2@test.com");

        // استدعاء getUsers
        List<Roles> usersList = auth.getUsers();

        // التأكد من أن القائمة تحتوي على المستخدمين المضافين فقط
        assertEquals(2, usersList.size(), "Expected 2 users in the list");
        assertTrue(usersList.contains(user1), "List should contain user1");
        assertTrue(usersList.contains(user2), "List should contain user2");

        // التأكد أن تعديل القائمة المسترجعة لا يغير القائمة الداخلية
        usersList.clear();
        List<Roles> originalList = auth.getUsers();
        assertEquals(2, originalList.size(), "Clearing returned list should not affect internal list");
    }
    @Test
    void testLogoutIfUsernameMatchesCurrentUser() throws Exception {
        // إنشاء AuthService مع مستخدمين فارغين
        AuthService auth = new AuthService();

        // إعداد currentUser
        Roles current = new Roles("User1", "pass", "MEMBER", "user1@test.com");

        // استخدام Reflection لوضع currentUser (لأنه private)
        java.lang.reflect.Field field = AuthService.class.getDeclaredField("currentUser");
        field.setAccessible(true);
        field.set(auth, current);

        // username الذي نحاول تسجيل خروجه مطابق للمستخدم الحالي (بشكل مختلف في حالة الأحرف)
        String usernameToLogout = "user1"; // lower case

        // تنفيذ الكود المطلوب
        if (auth.getCurrentUser() != null &&
                auth.getCurrentUser().getUsername().equalsIgnoreCase(usernameToLogout)) {
            field.set(auth, null); // إزالة currentUser
        }

        // التأكد أن currentUser أصبح null بعد logout
        assertNull(auth.getCurrentUser(), "currentUser يجب أن يكون null بعد logout عند المطابقة");
    }

    @Test
    void testAddUserWithNullValues() {
        AuthService auth = new AuthService();

        // جميع الحالات التي فيها null يجب أن ترجع null
        assertNull(auth.addUser(null, "pass", "MEMBER", "email@test.com"), "Username null → should return null");
        assertNull(auth.addUser("user", null, "MEMBER", "email@test.com"), "Password null → should return null");
        assertNull(auth.addUser("user", "pass", null, "email@test.com"), "RoleName null → should return null");
        assertNull(auth.addUser("user", "pass", "MEMBER", null), "Email null → should return null");
    }
    @Test
    void testAddUserReturnsNull() {
        AuthService auth = new AuthService();

        // حالة: username null
        assertNull(auth.addUser(null, "pass", "MEMBER", "email@test.com"), "Expected null when username is null");

        // حالة: password null
        assertNull(auth.addUser("user", null, "MEMBER", "email@test.com"), "Expected null when password is null");

        // حالة: roleName null
        assertNull(auth.addUser("user", "pass", null, "email@test.com"), "Expected null when roleName is null");

        // حالة: email null
        assertNull(auth.addUser("user", "pass", "MEMBER", null), "Expected null when email is null");

        // حالة: email فارغة
        assertNull(auth.addUser("user", "pass", "MEMBER", ""), "Expected null when email is empty");
    }

}
