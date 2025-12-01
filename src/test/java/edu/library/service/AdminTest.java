package edu.library.service;

import edu.library.model.Book;
import edu.library.model.Roles;
import edu.library.time.SystemTimeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    @TempDir
    Path tempDir;

    // helper to run Admin.handle with provided input string
    private int runHandle(String inputLines,
                          MediaService bookService,
                          AuthService authService,
                          ReminderService reminderService,
                          Roles user)
    {
        Scanner scanner = new Scanner(inputLines);
        return Admin.handle(scanner, bookService, authService, reminderService, user);
    }

    @Test
    void optionRemoveSelf_isBlocked() throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, Collections.singletonList("admin,apwd,ADMIN,admin@example.com"));
        AuthService auth = new AuthService(usersFile.toString());

        Roles admin = auth.login("admin", "apwd");
        assertNotNull(admin);

        // choose option 6 and attempt to remove self
        String input = "6\n" + admin.getUsername() + "\n";

        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_admin.txt").toString()
        );
        MediaService bookService = new MediaService(
                tempDir.resolve("books_admin.txt").toString(),
                borrowService,
                new FineService(tempDir.resolve("fines_admin.txt").toString())
        );
        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new edu.library.time.SystemTimeProvider()
        );

        int result = runHandle(input, bookService, auth, reminderService, admin);
        assertEquals(0, result);
        // user should still exist
        assertTrue(auth.userExists("admin"));
    }


    @Test
    void optionLogout_returns1_and_clearsCurrentUser() throws IOException {
        Path usersFile = tempDir.resolve("users2.txt");
        Files.write(usersFile, Collections.singletonList("joe,joepwd,ADMIN,joe@example.com"));
        AuthService auth = new AuthService(usersFile.toString());
        Roles joe = auth.login("joe", "joepwd");
        assertNotNull(joe);

        String input = "9\n"; // logout option

        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_admin.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_admin.txt").toString()
        );
        MediaService bookService = new MediaService(
                tempDir.resolve("books_admin.txt").toString(),
                borrowService,
                fineService
        );
        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );

        int result = runHandle(input, bookService, auth, reminderService, joe);
        assertEquals(1, result);
        assertNull(auth.getCurrentUser());
    }
    @Test
    void optionExit_returns2() {
        AuthService auth = new AuthService(tempDir.resolve("u.txt").toString());
        Roles dummy = new Roles("x","p","ADMIN","x@example.com");

        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_admin.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_admin.txt").toString()
        );
        MediaService bookService = new MediaService(
                tempDir.resolve("books_admin.txt").toString(),
                borrowService,
                fineService
        );
        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );

        String input = "10\n";
        int result = runHandle(input, bookService, auth, reminderService, dummy);
        assertEquals(2, result);
    }
    @Test
    void optionListUsers_printsUsers() throws IOException {
        Path usersFile = tempDir.resolve("users3.txt");
        Files.write(usersFile, List.of(
                "a,apwd,ADMIN,a@example.com",
                "b,bpwd,MEMBER,b@example.com"
        ));
        AuthService auth = new AuthService(usersFile.toString());
        Roles admin = auth.login("a", "apwd");
        assertNotNull(admin);

        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_admin.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_admin.txt").toString()
        );
        MediaService bookService = new MediaService(
                tempDir.resolve("books_admin.txt").toString(),
                borrowService,
                fineService
        );
        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );
        // choose option 7 (List Users)
        String input = "7\n";
        int result = runHandle(input, bookService, auth, reminderService, admin);
        assertEquals(0, result);
        // ensure both users reported via auth.getUsers()
        List<Roles> users = auth.getUsers();
        assertEquals(2, users.size());
    }
    @Test
    void optionAddBook_addsBookToService_andFile() {
        // BookService writes to media.txt in project root; change working dir to temp to avoid pollution
        Path originalCwd = Path.of(System.getProperty("user.dir"));
        try {
            System.setProperty("user.dir", tempDir.toString());

            BorrowRecordService borrowService = new BorrowRecordService(
                    tempDir.resolve("borrow_records_admin.txt").toString()
            );
            FineService fineService = new FineService(
                    tempDir.resolve("fines_admin.txt").toString()
            );
            MediaService service = new MediaService(
                    tempDir.resolve("books_admin.txt").toString(),
                    borrowService,
                    fineService
            );

            AuthService auth = new AuthService(tempDir.resolve("u4.txt").toString());
            Roles admin = new Roles("adm","pwd","ADMIN","adm@example.com");

            ReminderService reminderService = new ReminderService(
                    borrowService,
                    auth,
                    new SystemTimeProvider()
            );

            String input = "1\nThe Title\nAuthor Name\nISBN123\n";
            int result = runHandle(input, service, auth, reminderService, admin);
            assertEquals(0, result);

            // service should have the book in memory
            List<Book> books = service.getBooks();
            assertFalse(books.isEmpty());
            Book b = books.get(0);
            assertEquals("The Title", b.getTitle());
            assertEquals("Author Name", b.getAuthor());
            assertEquals("ISBN123", b.getIsbn());

        } finally {
            System.setProperty("user.dir", originalCwd.toString());
        }
    }
    @Test
    void optionAddMember_success_withEmailLoop() {
        Path usersFile = tempDir.resolve("users_add_member.txt");
        AuthService auth = new AuthService(usersFile.toString());

        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_admin.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_admin.txt").toString()
        );
        MediaService service = new MediaService(
                tempDir.resolve("books_admin.txt").toString(),
                borrowService,
                fineService
        );
        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );

        Roles admin = new Roles("admin","pwd","ADMIN","admin@example.com");

        // first provide empty email to trigger the loop, then valid email, username and password
        String input = "4\n\nnewmember@example.com\nnewmember\nnewpass\n";
        int rc = runHandle(input, service, auth, reminderService, admin);
        assertEquals(0, rc);
        assertTrue(auth.userExists("newmember"));

        AuthService reloaded = new AuthService(usersFile.toString());
        assertNotNull(reloaded.login("newmember","newpass"));
    }

    @Test
    void optionAddMember_duplicateUsername_noop() {
        Path usersFile = tempDir.resolve("users_add_member_dup.txt");
        AuthService auth = new AuthService(usersFile.toString());
        // pre-add a user
        auth.addUser("sam", "sampwd", "MEMBER", "sam@example.com");

        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_admin.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_admin.txt").toString()
        );
        MediaService service = new MediaService(
                tempDir.resolve("books_admin.txt").toString(),
                borrowService,
                fineService
        );
        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );

        Roles admin = new Roles("admin","pwd","ADMIN","admin@example.com");

        String input = "4\nmember@example.com\nsam\notherpwd\n";
        int rc = runHandle(input, service, auth, reminderService, admin);
        assertEquals(0, rc);
        // still only the original 'sam' exists
        assertNotNull(auth.login("sam","sampwd"));
        // verify that "otherpwd" does not authenticate for 'sam'
        assertNull(auth.login("sam","otherpwd"));
    }

    @Test
    void optionAddLibrarian_success_withEmailLoop() {
        Path usersFile = tempDir.resolve("users_add_lib.txt");
        AuthService auth = new AuthService(usersFile.toString());

        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_admin.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_admin.txt").toString()
        );
        MediaService service = new MediaService(
                tempDir.resolve("books_admin.txt").toString(),
                borrowService,
                fineService
        );
        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );

        Roles admin = new Roles("admin","pwd","ADMIN","admin@example.com");

        String input = "5\n\nnewlib@example.com\nnewlib\nlibpass\n";
        int rc = runHandle(input, service, auth, reminderService, admin);
        assertEquals(0, rc);
        assertTrue(auth.userExists("newlib"));
        AuthService reloaded = new AuthService(usersFile.toString());
        assertNotNull(reloaded.login("newlib","libpass"));
    }

    @Test
    void optionAddLibrarian_duplicateUsername_noop() {
        Path usersFile = tempDir.resolve("users_add_lib_dup.txt");
        AuthService auth = new AuthService(usersFile.toString());
        auth.addUser("libx", "pw1", "LIBRARIAN", "libx@example.com");

        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_admin.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_admin.txt").toString()
        );
        MediaService service = new MediaService(
                tempDir.resolve("books_admin.txt").toString(),
                borrowService,
                fineService
        );
        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );

        Roles admin = new Roles("admin","pwd","ADMIN","admin@example.com");

        String input = "5\nlib2@example.com\nlibx\nnewpw\n";
        int rc = runHandle(input, service, auth, reminderService, admin);
        assertEquals(0, rc);
        // original password still valid, newpw should not authenticate for libx
        assertNotNull(auth.login("libx","pw1"));
        assertNull(auth.login("libx","newpw"));
    }

    @Test
    void optionSearchBook_found() {
        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_admin.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_admin.txt").toString()
        );
        MediaService service = new MediaService(
                tempDir.resolve("books_admin.txt").toString(),
                borrowService,
                fineService
        );
        service.addBook(new Book("SearchTitle","Auth","S-ISBN"));

        AuthService auth = new AuthService(tempDir.resolve("u_search_admin.txt").toString());
        Roles admin = new Roles("admin","pwd","ADMIN","admin@example.com");

        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );

        String input = "2\nSearchTitle\n";
        int rc = runHandle(input, service, auth, reminderService, admin);
        assertEquals(0, rc);
        List<Book> found = service.searchBook("SearchTitle");
        assertFalse(found.isEmpty());
    }

    @Test
    void optionSearchBook_notFound() {
        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_admin2.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_admin2.txt").toString()
        );
        MediaService service = new MediaService(
                tempDir.resolve("books_admin2.txt").toString(),
                borrowService,
                fineService
        );
        AuthService auth = new AuthService(tempDir.resolve("u_search_admin2.txt").toString());
        Roles admin = new Roles("admin","pwd","ADMIN","admin@example.com");

        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );

        String input = "2\nNoMatchHere\n";
        int rc = runHandle(input, service, auth, reminderService, admin);
        assertEquals(0, rc);
        List<Book> found = service.searchBook("NoMatchHere");
        assertTrue(found.isEmpty());
    }

    @Test
    void optionDisplayBooks_empty_and_withBooks() {
        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_display.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_display.txt").toString()
        );
        MediaService service = new MediaService(
                tempDir.resolve("books_display.txt").toString(),
                borrowService,
                fineService
        );
        AuthService auth = new AuthService(tempDir.resolve("u_display.txt").toString());
        Roles admin = new Roles("admin","pwd","ADMIN","admin@example.com");

        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );

        // display when empty
        int rcEmpty = runHandle("3\n", service, auth, reminderService, admin);
        assertEquals(0, rcEmpty);

        // add a book and display
        service.addBook(new Book("D1","A1","ISBN-D1"));
        int rcWith = runHandle("3\n", service, auth, reminderService, admin);
        assertEquals(0, rcWith);
    }

    @Test
    void invalidOption_returns0_admin() {
        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_inv.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_inv.txt").toString()
        );
        MediaService service = new MediaService(
                tempDir.resolve("books_inv.txt").toString(),
                borrowService,
                fineService
        );
        AuthService auth = new AuthService(tempDir.resolve("u_inv_admin.txt").toString());
        Roles admin = new Roles("admin","pwd","ADMIN","admin@example.com");

        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );

        int rc = runHandle("notanumber\n", service, auth, reminderService, admin);
        assertEquals(0, rc);
    }

    @Test
    void optionRemoveUser_success() {
        Path usersFile = tempDir.resolve("users_remove.txt");
        AuthService auth = new AuthService(usersFile.toString());
        auth.addUser("victim", "vpwd", "MEMBER", "v@example.com");

        BorrowRecordService borrowService = new BorrowRecordService(
                tempDir.resolve("borrow_records_remove.txt").toString()
        );
        FineService fineService = new FineService(
                tempDir.resolve("fines_remove.txt").toString()
        );
        MediaService service = new MediaService(
                tempDir.resolve("books_remove.txt").toString(),
                borrowService,
                fineService
        );
        ReminderService reminderService = new ReminderService(
                borrowService,
                auth,
                new SystemTimeProvider()
        );

        Roles admin = new Roles("admin","pwd","ADMIN","admin@example.com");

        assertTrue(auth.userExists("victim"));
        String input = "6\nvictim\n";
        int rc = runHandle(input, service, auth, reminderService, admin);
        assertEquals(0, rc);
        assertFalse(auth.userExists("victim"));
    }
}
