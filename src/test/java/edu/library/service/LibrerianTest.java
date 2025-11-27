package edu.library.service;

import edu.library.model.Book;
import edu.library.model.Roles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class LibrerianTest {

    @TempDir
    Path tempDir;

    private int runHandle(String inputLines, BookService service, AuthService auth, Roles user) {
        Scanner scanner = new Scanner(inputLines);
        return Librarian.handle(scanner, service, auth, user);
    }

    @Test
    void optionAddBook_addsBookToService() {
        BookService service = new BookService(tempDir.resolve("books_lib.txt").toString(), new BorrowRecordService(tempDir.resolve("borrow_records_lib.txt").toString()), new FineService(tempDir.resolve("fines_lib.txt").toString()));
        AuthService auth = new AuthService(tempDir.resolve("u_add.txt").toString());
        Roles lib = new Roles("lib","pwd","LIBRARIAN","lib@example.com");

        String input = "1\nMy Book\nSome Author\nISBN-1\n";
        int rc = runHandle(input, service, auth, lib);
        assertEquals(0, rc);

        List<Book> books = service.getBooks();
        assertFalse(books.isEmpty());
        Book b = books.get(0);
        assertEquals("My Book", b.getTitle());
        assertEquals("Some Author", b.getAuthor());
        assertEquals("ISBN-1", b.getIsbn());
    }

    @Test
    void optionSearch_noResults_returns0() {
        BookService service = new BookService(tempDir.resolve("books_lib.txt").toString(), new BorrowRecordService(tempDir.resolve("borrow_records_lib.txt").toString()), new FineService(tempDir.resolve("fines_lib.txt").toString()));
        AuthService auth = new AuthService(tempDir.resolve("u_search1.txt").toString());
        Roles lib = new Roles("lib","pwd","LIBRARIAN","lib@example.com");

        String input = "3\nNoSuchBook\n";
        int rc = runHandle(input, service, auth, lib);
        assertEquals(0, rc);
    }

    @Test
    void optionSearch_withResults_returns0() {
        BookService service = new BookService(tempDir.resolve("books_lib.txt").toString(), new BorrowRecordService(tempDir.resolve("borrow_records_lib.txt").toString()), new FineService(tempDir.resolve("fines_lib.txt").toString()));
        // add a book directly to the service
        service.addBook(new Book("FoundTitle","Auth","ISBN-FOUND"));

        AuthService auth = new AuthService(tempDir.resolve("u_search2.txt").toString());
        Roles lib = new Roles("lib","pwd","LIBRARIAN","lib@example.com");

        String input = "3\nFoundTitle\n";
        int rc = runHandle(input, service, auth, lib);
        assertEquals(0, rc);
        // ensure search would find it
        List<Book> results = service.searchBook("FoundTitle");
        assertFalse(results.isEmpty());
    }

    @Test
    void optionLogout_returns1_and_clearsCurrentUser() throws IOException {
        Path usersFile = tempDir.resolve("u_logout.txt");
        Files.write(usersFile, Collections.singletonList("libr,lpwd,LIBRARIAN,libr@example.com"));
        AuthService auth = new AuthService(usersFile.toString());
        Roles r = auth.login("libr","lpwd");
        assertNotNull(r);

        BookService service = new BookService(tempDir.resolve("books_lib.txt").toString(), new BorrowRecordService(tempDir.resolve("borrow_records_lib.txt").toString()), new FineService(tempDir.resolve("fines_lib.txt").toString()));
        String input = "6\n";
        int rc = runHandle(input, service, auth, r);
        assertEquals(1, rc);
        assertNull(auth.getCurrentUser());
    }

    @Test
    void optionExit_returns2() {
        AuthService auth = new AuthService(tempDir.resolve("u_exit.txt").toString());
        Roles r = new Roles("x","p","LIBRARIAN","x@example.com");
        BookService service = new BookService(tempDir.resolve("books_lib.txt").toString(), new BorrowRecordService(tempDir.resolve("borrow_records_lib.txt").toString()), new FineService(tempDir.resolve("fines_lib.txt").toString()));

        int rc = runHandle("7\n", service, auth, r);
        assertEquals(2, rc);
    }

    @Test
    void invalidOption_returns0() {
        AuthService auth = new AuthService(tempDir.resolve("u_inv.txt").toString());
        Roles r = new Roles("x","p","LIBRARIAN","x@example.com");
        BookService service = new BookService(tempDir.resolve("books_lib.txt").toString(), new BorrowRecordService(tempDir.resolve("borrow_records_lib.txt").toString()), new FineService(tempDir.resolve("fines_lib.txt").toString()));

        int rc = runHandle("notanumber\n", service, auth, r);
        assertEquals(0, rc);
    }
    @Test
    void viewBorrowRecords_showsOverdueFlag() {
        BorrowRecordService borrowRecordService = new BorrowRecordService(tempDir.resolve("borrow_records_lib.txt").toString());
        FineService fineService = new FineService(tempDir.resolve("fines_lib.txt").toString());
        BookService service = new BookService(tempDir.resolve("books_lib.txt").toString(), borrowRecordService, fineService);

        borrowRecordService.recordBorrow("member1", "ISBN-OVERDUE", LocalDate.now().minusDays(29));

        AuthService auth = new AuthService(tempDir.resolve("u_overdue.txt").toString());
        Roles lib = new Roles("lib","pwd","LIBRARIAN","lib@example.com");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try {
            int rc = runHandle("4\n", service, auth, lib);
            assertEquals(0, rc);
        } finally {
            System.setOut(originalOut);
        }

        String output = out.toString();
        assertTrue(output.contains("Overdue"));
        assertTrue(output.contains("ISBN-OVERDUE"));
    }

    @Test
    void viewFineBalances_listsCurrentFines() {
        BorrowRecordService borrowRecordService = new BorrowRecordService(tempDir.resolve("borrow_records_lib.txt").toString());
        FineService fineService = new FineService(tempDir.resolve("fines_lib.txt").toString());
        BookService service = new BookService(tempDir.resolve("books_lib.txt").toString(), borrowRecordService, fineService);

        fineService.addFine("member1", 40);
        fineService.addFine("member2", 10);

        AuthService auth = new AuthService(tempDir.resolve("u_fines.txt").toString());
        Roles lib = new Roles("lib","pwd","LIBRARIAN","lib@example.com");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try {
            int rc = runHandle("5\n", service, auth, lib);
            assertEquals(0, rc);
        } finally {
            System.setOut(originalOut);
        }

        String output = out.toString();
        assertTrue(output.contains("member1: 40"));
        assertTrue(output.contains("member2: 10"));
    }
}
