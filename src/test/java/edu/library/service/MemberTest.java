package edu.library.service;

import edu.library.model.Book;
import edu.library.model.Roles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import edu.library.service.BorrowRecordService;
import edu.library.service.FineService;

public class MemberTest {

    @TempDir
    Path tempDir;

    private int runHandle(String inputLines, BookService service, AuthService auth, Roles user) {
        Scanner scanner = new Scanner(inputLines);
        return Member.handle(scanner, service, auth, user);
    }

    @Test
    void search_noResults_returns0() {
        BookService service = new BookService(tempDir.resolve("books_search_none.txt").toString(),
                new BorrowRecordService(tempDir.resolve("borrow_records_none.txt").toString()),
                new FineService(tempDir.resolve("fines_none.txt").toString()));
        AuthService auth = new AuthService(tempDir.resolve("u_search_no.txt").toString());
        Roles member = new Roles("m","p","MEMBER","m@example.com");

        int rc = runHandle("1\nNoSuchTitle\n", service, auth, member);
        assertEquals(0, rc);
    }
    @Test
    void search_withResults_returns0() {
        // ensure BookService writes to temp dir to avoid polluting project root
        Path originalCwd = Path.of(System.getProperty("user.dir"));
        try {
            System.setProperty("user.dir", tempDir.toString());
            BookService service = new BookService(tempDir.resolve("books_search_yes.txt").toString(),
                    new BorrowRecordService(tempDir.resolve("borrow_records_yes.txt").toString()),
                    new FineService(tempDir.resolve("fines_yes.txt").toString()));
            service.addBook(new Book("Alpha","Author A","ISBN-ALPHA"));

            AuthService auth = new AuthService(tempDir.resolve("u_search_yes.txt").toString());
            Roles member = new Roles("m","p","MEMBER","m@example.com");

            int rc = runHandle("1\nAlpha\n", service, auth, member);
            assertEquals(0, rc);

            List<Book> res = service.searchBook("Alpha");
            assertFalse(res.isEmpty());
        } finally {
            System.setProperty("user.dir", originalCwd.toString());
        }
    }

    @Test
    void borrow_success_setsDueDateAndUnavailable() {
        Path originalCwd = Path.of(System.getProperty("user.dir"));
        try {
            System.setProperty("user.dir", tempDir.toString());
            BookService service = new BookService(tempDir.resolve("books_borrow_success.txt").toString(),
                    new BorrowRecordService(tempDir.resolve("borrow_records_borrow.txt").toString()),
                    new FineService(tempDir.resolve("fines_borrow.txt").toString()));
            Book book = new Book("BorrowMe","Auth","B123");
            service.addBook(book);

            AuthService auth = new AuthService(tempDir.resolve("u_borrow.txt").toString());
            Roles member = new Roles("m","p","MEMBER","m@example.com");

            int rc = runHandle("2\nB123\n", service, auth, member);
            assertEquals(0, rc);
            assertFalse(book.isAvailable());
            assertNotNull(book.getDueDate());
        } finally {
            System.setProperty("user.dir", originalCwd.toString());
        }
    }

    @Test
    void borrow_invalidIsbn_noChange() {
        BookService service = new BookService(tempDir.resolve("books_borrow_invalid.txt").toString(),
                new BorrowRecordService(tempDir.resolve("borrow_records_invalid.txt").toString()),
                new FineService(tempDir.resolve("fines_invalid.txt").toString()));
        Book book = new Book("X","A","B999");
        service.addBook(book);

        AuthService auth = new AuthService(tempDir.resolve("u_borrow_no.txt").toString());
        Roles member = new Roles("m","p","MEMBER","m@example.com");

        int rc = runHandle("2\nNOISBN\n", service, auth, member);
        assertEquals(0, rc);
        assertTrue(book.isAvailable());

    }

    @Test
    void borrow_notAvailable_reportsAndNoChange() {
        BookService service = new BookService(tempDir.resolve("books_borrow_na.txt").toString(),
                new BorrowRecordService(tempDir.resolve("borrow_records_na.txt").toString()),
                new FineService(tempDir.resolve("fines_na.txt").toString()));
        Book book = new Book("Z","A","B200");
        service.addBook(book);
        // make unavailable
        service.borrowBook(book, "m");

        AuthService auth = new AuthService(tempDir.resolve("u_borrow_na.txt").toString());
        Roles member = new Roles("m","p","MEMBER","m@example.com");

        int rc = runHandle("2\nB200\n", service, auth, member);
        assertEquals(0, rc);
        assertFalse(book.isAvailable());
    }

    @Test
    void return_success_makesBookAvailable() {
        Path originalCwd = Path.of(System.getProperty("user.dir"));
        try {
            System.setProperty("user.dir", tempDir.toString());
            BookService service = new BookService(tempDir.resolve("books_return_success.txt").toString(),
                    new BorrowRecordService(tempDir.resolve("borrow_records_return.txt").toString()),
                    new FineService(tempDir.resolve("fines_return.txt").toString()));
            Book book = new Book("ToReturn","A","R100");
            service.addBook(book);
            service.borrowBook(book, "m");
            assertFalse(book.isAvailable());

            AuthService auth = new AuthService(tempDir.resolve("u_return.txt").toString());
            Roles member = new Roles("m","p","MEMBER","m@example.com");

            int rc = runHandle("3\nR100\n", service, auth, member);
            assertEquals(0, rc);
            assertTrue(book.isAvailable());
            assertNull(book.getDueDate());
        } finally {
            System.setProperty("user.dir", originalCwd.toString());
        }
    }

    @Test
    void return_notBorrowed_reportsAndNoChange() {
        BookService service = new BookService(tempDir.resolve("books_return_no.txt").toString(),
                new BorrowRecordService(tempDir.resolve("borrow_records_return_no.txt").toString()),
                new FineService(tempDir.resolve("fines_return_no.txt").toString()));
        Book book = new Book("NotBorrowed","A","R200");
        service.addBook(book);

        AuthService auth = new AuthService(tempDir.resolve("u_return_no.txt").toString());
        Roles member = new Roles("m","p","MEMBER","m@example.com");

        int rc = runHandle("3\nR200\n", service, auth, member);
        assertEquals(0, rc);
        assertTrue(book.isAvailable());
    }

    @Test
    void viewRemainingTime_showsActiveLoans() {
        BorrowRecordService borrowRecordService = new BorrowRecordService(tempDir.resolve("borrow_records_remaining.txt").toString());
        BookService service = new BookService(tempDir.resolve("books_remaining.txt").toString(),
                borrowRecordService,
                new FineService(tempDir.resolve("fines_remaining.txt").toString()));
        Book book = new Book("Clock","Author","CLK-1");
        service.addBook(book);
        service.borrowBook(book, "m");

        AuthService auth = new AuthService(tempDir.resolve("u_remaining.txt").toString());
        Roles member = new Roles("m","p","MEMBER","m@example.com");

        int rc = runHandle("6\n", service, auth, member);
        assertEquals(0, rc);
    }

    @Test
    void logout_returns1_andClearsCurrentUser() throws IOException {
        Path usersFile = tempDir.resolve("u_logout_member.txt");
        Files.write(usersFile, Collections.singletonList("mem,mpwd,MEMBER,mem@example.com"));
        AuthService auth = new AuthService(usersFile.toString());
        Roles mem = auth.login("mem","mpwd");
        assertNotNull(mem);

        BookService service = new BookService(tempDir.resolve("books_logout.txt").toString(),
                new BorrowRecordService(tempDir.resolve("borrow_records_logout.txt").toString()),
                new FineService(tempDir.resolve("fines_logout.txt").toString()));
        int rc = runHandle("7\n", service, auth, mem);
        assertEquals(1, rc);
        assertNull(auth.getCurrentUser());
    }
    @Test
    void exit_returns2() {
        AuthService auth = new AuthService(tempDir.resolve("u_exit_member.txt").toString());
        Roles mem = new Roles("m","p","MEMBER","m@example.com");
        BookService service = new BookService(tempDir.resolve("books_exit.txt").toString(),
                new BorrowRecordService(tempDir.resolve("borrow_records_exit.txt").toString()),
                new FineService(tempDir.resolve("fines_exit.txt").toString()));

        int rc = runHandle("8\n", service, auth, mem);
        assertEquals(2, rc);
    }
    @Test
    void invalidOption_returns0() {
        AuthService auth = new AuthService(tempDir.resolve("u_invalid_member.txt").toString());
        Roles mem = new Roles("m","p","MEMBER","m@example.com");
        BookService service = new BookService(tempDir.resolve("books_invalid.txt").toString(),
                new BorrowRecordService(tempDir.resolve("borrow_records_invalid_opt.txt").toString()),
                new FineService(tempDir.resolve("fines_invalid_opt.txt").toString()));

        int rc = runHandle("notanumber\n", service, auth, mem);
        assertEquals(0, rc);
    }
}
