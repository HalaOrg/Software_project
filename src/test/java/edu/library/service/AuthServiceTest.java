package edu.library.service;

import edu.library.model.BorrowRecord;
import edu.library.model.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;
class AuthServiceTest {

    private AuthService authService;
    private FineService fineService;
    private BorrowRecordService mockBorrow;

    @TempDir
    Path tempDir;      // كل تيست له فولدر مؤقت خاص فيه

    private Path usersFile;  // users.txt داخل الـ tempDir

    @BeforeEach
    void setUp() throws IOException {
        fineService = mock(FineService.class);
        mockBorrow = mock(BorrowRecordService.class);

        // نحدد ملف users.txt داخل الفولدر المؤقت
        usersFile = tempDir.resolve("users.txt");
        Files.createFile(usersFile);

        // نستخدم الكونستركتور اللي يسمح بتحديد المسار + fineService
        authService = new AuthService(usersFile.toString(), fineService);

        // نضيف المستخدمين اللي بدنا نشتغل عليهم في معظم التيستات
        authService.addUser("admin", "adminpwd", "ADMIN", "admin@example.com");
        authService.addUser("member1", "pwd1", "MEMBER", "member1@example.com");
        authService.addUser("member2", "pwd2", "MEMBER", "member2@example.com");
    }

    @Test
    void testRemoveUserWithRestrictions() {
        // تسجيل دخول كـ admin
        authService.login("admin", "adminpwd");

        // 1️⃣ حالة: currentUser = null أو ليس admin
        authService.logout();
        assertFalse(authService.removeUserWithRestrictions("member1", mockBorrow));
        authService.login("admin", "adminpwd");

        // 2️⃣ حالة: username = null أو نفس admin
        assertFalse(authService.removeUserWithRestrictions(null, mockBorrow));
        assertFalse(authService.removeUserWithRestrictions("admin", mockBorrow));

        // 3️⃣ حالة: المستخدم عنده balance > 0
        when(fineService.getBalance("member1")).thenReturn(50);
        assertFalse(authService.removeUserWithRestrictions("member1", mockBorrow));

        // 4️⃣ حالة: المستخدم عنده borrow records active
        when(fineService.getBalance("member2")).thenReturn(0);
        BorrowRecord fakeRecord = mock(BorrowRecord.class);
        when(mockBorrow.getActiveBorrowRecordsForUser("member2"))
                .thenReturn(Collections.singletonList(fakeRecord));
        assertFalse(authService.removeUserWithRestrictions("member2", mockBorrow));

        // 5️⃣ حالة: كل الشروط صح → يجب أن يتم الحذف
        authService.addUser("member3", "pwd3", "MEMBER", "member3@example.com");
        when(fineService.getBalance("member3")).thenReturn(0);
        when(mockBorrow.getActiveBorrowRecordsForUser("member3"))
                .thenReturn(Collections.emptyList());

        assertTrue(authService.removeUserWithRestrictions("member3", mockBorrow));
        assertFalse(authService.userExists("member3"));
    }

    @Test
    void testDefaultConstructor() {
        // نخلي الـ working directory يروح على tempDir
        String oldDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            AuthService defaultAuth = new AuthService();
            assertNotNull(defaultAuth);
            // نتأكد إنه أنشأ users.txt في tempDir مش في المشروع الحقيقي
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
        // مسار غير صالح داخل tempDir ليغطي catch(IOException)
        Path badPath = tempDir.resolve("invalid_dir/users.txt");
        AuthService auth = new AuthService(badPath.toString());

        assertDoesNotThrow(() ->
                auth.addUser("userX", "pwdX", "MEMBER")
        );
    }

    @Test
    void testAddUserWithNullParameters() {
        Path usersFile = tempDir.resolve("users_null.txt");
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.addUser(null, "pwd", "MEMBER", "x@example.com"));
        assertNull(auth.addUser("u", null, "MEMBER", "x@example.com"));
        assertNull(auth.addUser("u", "pwd", null, "x@example.com"));
        assertNull(auth.addUser("u", "pwd", "MEMBER", null));
        assertNull(auth.addUser("u", "pwd", "MEMBER", "   "));
    }

    @Test
    void testUserExistsAndRemoveUserCaseInsensitive() {
        Path file = tempDir.resolve("users_case.txt");
        AuthService auth = new AuthService(file.toString());

        auth.addUser("TestUser", "pwd", "MEMBER", "t@example.com");
        assertTrue(auth.userExists("testuser"));
        assertTrue(auth.userExists("TESTUSER"));
        assertTrue(auth.removeUser("TeStUsEr"));
        assertFalse(auth.userExists("testuser"));
    }

    @Test
    void testLogoutAndCurrentAdmin() throws IOException {
        Path file = tempDir.resolve("users_admin.txt");
        Files.write(file, Collections.singletonList("admin,adminpwd,ADMIN,admin@example.com"));
        AuthService auth = new AuthService(file.toString());

        assertNull(auth.getCurrentUser());
        Roles admin = auth.login("admin", "adminpwd");
        assertNotNull(admin);
        assertTrue(auth.getCurrentAdmin().isAdmin());

        assertTrue(auth.logout());
        assertNull(auth.getCurrentAdmin());
        assertNull(auth.getCurrentUser());
        assertFalse(auth.logout()); // logout ثانية بدون مستخدم
    }

    @Test
    void loginReadsUsersFromFile() throws IOException {
        Path file = tempDir.resolve("users_login.txt");
        Files.write(file, Arrays.asList(
                "admin,admin123,ADMIN,admin@example.com",
                "member,member123,MEMBER,member@example.com"
        ));

        AuthService authService = new AuthService(file.toString());

        Roles admin = authService.login("admin", "admin123");
        assertNotNull(admin);
        assertTrue(admin.isAdmin());
        assertEquals("ADMIN", admin.getRoleName());

        Roles member = authService.login("member", "member123");
        assertNotNull(member);
        assertEquals("MEMBER", member.getRoleName());
    }

    @Test
    void addUserPersistsToDisk() {
        Path file = tempDir.resolve("users_persist.txt");
        AuthService authService = new AuthService(file.toString());

        assertNull(authService.login("librarian", "lib123"));
        authService.addUser("librarian", "lib123", "LIBRARIAN", "librarian@example.com");

        AuthService reloaded = new AuthService(file.toString());
        Roles librarian = reloaded.login("librarian", "lib123");
        assertNotNull(librarian);
        assertEquals("LIBRARIAN", librarian.getRoleName());
    }

    @Test
    void loginFailsForUnknownCredentials() throws IOException {
        Path file = tempDir.resolve("users_fail.txt");
        Files.write(file, Collections.singletonList("user,password,MEMBER,user@example.com"));
        AuthService authService = new AuthService(file.toString());

        assertNull(authService.login("user", "wrong"));
        assertNull(authService.login("unknown", "password"));
    }

    @Test
    void login_success() throws IOException {
        Path file = tempDir.resolve("users_success.txt");
        Files.write(file, Collections.singletonList("alice,alicepwd,MEMBER,alice@example.com"));

        AuthService auth = new AuthService(file.toString());

        Roles r = auth.login("alice", "alicepwd");
        assertNotNull(r);
        assertEquals("alice", r.getUsername());
        assertEquals("MEMBER", r.getRoleName());
    }
    @Test
    void login_wrongPassword() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("bob,bobpwd,MEMBER"));
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.login("bob", "wrongpwd"));
    }

    @Test
    void login_wrongUsername() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("carol,carolpwd,MEMBER"));
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.login("notcarol", "carolpwd"));
    }

    @Test
    void login_nullInputs() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("dave,davepwd,ADMIN"));
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.login(null, "davepwd"));
        assertNull(auth.login("dave", null));
        assertNull(auth.login(null, null));
    }

    @Test
    void login_afterSuccessAndFailure() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Arrays.asList(
                "eve,evepwd,MEMBER",
                "frank,frankpwd,LIBRARIAN"
        ));
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.login("eve", "wrong"));
        Roles eve = auth.login("eve", "evepwd");
        assertNotNull(eve);
        assertEquals("eve", auth.getCurrentUser().getUsername());

        Roles frank = auth.login("frank", "frankpwd");
        assertNotNull(frank);
        assertEquals("frank", auth.getCurrentUser().getUsername());
    }

    @Test
    void addUser_tests() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        Roles added = auth.addUser("greg", "gregpwd", "MEMBER", "greg@example.com");
        assertNotNull(added);
        assertTrue(auth.userExists("greg"));

        AuthService reloaded = new AuthService(usersFile.toString());
        assertNotNull(reloaded.login("greg", "gregpwd"));
    }

    @Test
    void removeUser_tests() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("harry", "harrypwd", "MEMBER", "harry@example.com");
        assertTrue(auth.userExists("harry"));

        assertTrue(auth.removeUser("harry"));
        assertFalse(auth.userExists("harry"));

        AuthService reloaded = new AuthService(usersFile.toString());
        assertNull(reloaded.login("harry", "harrypwd"));

        assertFalse(auth.removeUser("noone"));
    }

    @Test
    void userExists_tests() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        assertFalse(auth.userExists("ivy"));
        auth.addUser("ivy", "ivypwd", "MEMBER", "ivy@example.com");
        assertTrue(auth.userExists("ivy"));
    }

    @Test
    void logout_tests() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("john,johnpwd,MEMBER"));
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.getCurrentUser());
        Roles john = auth.login("john", "johnpwd");
        assertNotNull(john);
        assertTrue(auth.logout());
        assertNull(auth.getCurrentUser());

        assertFalse(auth.logout());
    }

    @Test
    void addUser_withEmailPersists() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        Roles r = auth.addUser("lee", "leepwd", "MEMBER", "lee@example.com");
        assertNotNull(r);
        assertEquals("lee@example.com", r.getEmail());

        AuthService reloaded = new AuthService(usersFile.toString());
        Roles loaded = reloaded.login("lee", "leepwd");
        assertNotNull(loaded);
        assertEquals("lee@example.com", loaded.getEmail());
    }

    @Test
    void addUser_nullParams_returnsNull() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.addUser(null, "pwd", "MEMBER", "x@example.com"));
        assertNull(auth.addUser("u", null, "MEMBER", "x@example.com"));
        assertNull(auth.addUser("u", "pwd", null, "x@example.com"));
        assertNull(auth.addUser("u", "pwd", "MEMBER", null));
        assertNull(auth.addUser("u", "pwd", "MEMBER", "   "));
    }

    @Test
    void userExists_caseInsensitive() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("CaseUser", "pwd", "MEMBER", "case@example.com");
        assertTrue(auth.userExists("caseuser"));
        assertTrue(auth.userExists("CASEUSER"));
    }

    @Test
    void removeUser_caseInsensitive() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("Sam", "sampwd", "MEMBER", "sam@example.com");
        assertTrue(auth.userExists("sam"));
        assertTrue(auth.removeUser("sAm"));
        assertFalse(auth.userExists("sam"));
    }

    @Test
    void getUsers_returnsCopy() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("copyUser", "pwd", "MEMBER", "copy@example.com");
        java.util.List<Roles> list = auth.getUsers();
        assertFalse(list.isEmpty());
        list.clear();
        assertTrue(auth.userExists("copyUser"));
    }

    @Test
    void removeUser_clearsCurrentUser_whenRemovingLoggedInUser() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("tom", "tompwd", "MEMBER", "tom@example.com");
        Roles tom = auth.login("tom", "tompwd");
        assertNotNull(tom);
        assertEquals("tom", auth.getCurrentUser().getUsername());

        assertTrue(auth.removeUser("tom"));
        assertNull(auth.getCurrentUser());

        AuthService reloaded = new AuthService(usersFile.toString());
        assertNull(reloaded.login("tom", "tompwd"));
    }

    @Test
    void loadUsers_createsFile_whenNotExists() {
        Path usersFile = tempDir.resolve("missing_users.txt");
        assertFalse(Files.exists(usersFile));

        new AuthService(usersFile.toString());
        assertTrue(Files.exists(usersFile));
    }

    @Test
    void login_caseSensitiveUsername() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("CaseUser,pass,MEMBER"));
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.login("caseuser", "pass"));
        assertNotNull(auth.login("CaseUser", "pass"));
    }

    @Test
    void addUser_duplicateUsernames_allowed() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());

        auth.addUser("dupe", "pwd1", "MEMBER", "dupe1@example.com");
        auth.addUser("dupe", "pwd2", "MEMBER", "dupe2@example.com");

        assertNotNull(auth.login("dupe", "pwd1"));
        assertNotNull(auth.login("dupe", "pwd2"));

        AuthService reloaded = new AuthService(usersFile.toString());
        assertNotNull(reloaded.login("dupe", "pwd1"));
        assertNotNull(reloaded.login("dupe", "pwd2"));
    }

    @Test
    void getCurrentAdmin_afterAdminLogin_andLogout() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("admin,adminpwd,ADMIN,admin@example.com"));
        AuthService auth = new AuthService(usersFile.toString());

        assertNull(auth.getCurrentAdmin());

        Roles admin = auth.login("admin", "adminpwd");
        assertNotNull(admin);
        Roles current = auth.getCurrentAdmin();
        assertNotNull(current);
        assertTrue(current.isAdmin());
        assertEquals("admin", current.getUsername());

        assertTrue(auth.logout());
        assertNull(auth.getCurrentAdmin());
    }

    @Test
    void getCurrentAdmin_isSameAsGetCurrentUser_afterLogin() throws IOException {
        Path usersFile = tempDir.resolve("users2.txt");
        Files.write(usersFile, Collections.singletonList("u1,pwd1,MEMBER,u1@example.com"));
        AuthService auth = new AuthService(usersFile.toString());

        Roles r = auth.login("u1", "pwd1");
        assertNotNull(r);
        assertSame(auth.getCurrentUser(), auth.getCurrentAdmin());
    }

    @Test
    void removeUser_null_returnsFalse() {
        Path usersFile = tempDir.resolve("users.txt");
        AuthService auth = new AuthService(usersFile.toString());
        assertFalse(auth.removeUser(null));
    }

    @Test
    void loadUsers_skipsMalformedLines() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Arrays.asList(
                "shortline", // معيب
                "good,user,MEMBER,good@example.com" // صحيح
        ));

        AuthService auth = new AuthService(usersFile.toString());
        assertFalse(auth.userExists("shortline")); // يجب أن يكون false
        assertTrue(auth.userExists("good"));       // يجب أن يكون true
    }

    @Test
    void testIOExceptionDuringAuthServiceInit() {
        // مسار "غريب" جوّا tempDir، الهدف نتأكد إنه حتى لو صار IOException جوّا الكونسّتركتور ما يطلع لبرا
        Path badFile = tempDir
                .resolve("invalid_dir")
                .resolve("users.txt");

        assertDoesNotThrow(() -> new AuthService(badFile.toString()));
    }
    @Test
    void testIOExceptionInSaveUsersToFile() throws IOException {
        // نعمل فولدر ونمرره كأنه ملف → هذا غالبًا بيعمل IOException عند الكتابة
        Path dirAsFile = tempDir.resolve("not_a_file");
        Files.createDirectory(dirAsFile);   // صار Directory فعليًا

        // نمرر المسار للكلاس (المسار هذا هو فولدر، مش ملف)
        AuthService auth = new AuthService(dirAsFile.toString(), new FineService());

        // addUser لازم يدخل في catch(IOException) جوّا الكلاس وما يرمي Exception لبرا
        assertDoesNotThrow(() ->
                auth.addUser("userX", "pwX", "MEMBER")
        );
    }







}
