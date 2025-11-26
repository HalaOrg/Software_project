package edu.library.service;

import edu.library.model.Book;
import edu.library.model.Roles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    void optionAddBook_addsBookToService() throws IOException {
        BookService service = new BookService();
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
    void optionSearch_noResults_returns0() throws IOException {
        BookService service = new BookService();
        AuthService auth = new AuthService(tempDir.resolve("u_search1.txt").toString());
        Roles lib = new Roles("lib","pwd","LIBRARIAN","lib@example.com");

        String input = "3\nNoSuchBook\n";
        int rc = runHandle(input, service, auth, lib);
        assertEquals(0, rc);
    }

    @Test
    void optionSearch_withResults_returns0() throws IOException {
        BookService service = new BookService();
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

        BookService service = new BookService();
        String input = "4\n";
        int rc = runHandle(input, service, auth, r);
        assertEquals(1, rc);
        assertNull(auth.getCurrentUser());
    }

    @Test
    void optionExit_returns2() {
        AuthService auth = new AuthService(tempDir.resolve("u_exit.txt").toString());
        Roles r = new Roles("x","p","LIBRARIAN","x@example.com");
        BookService service = new BookService();

        int rc = runHandle("5\n", service, auth, r);
        assertEquals(2, rc);
    }

    @Test
    void invalidOption_returns0() {
        AuthService auth = new AuthService(tempDir.resolve("u_inv.txt").toString());
        Roles r = new Roles("x","p","LIBRARIAN","x@example.com");
        BookService service = new BookService();

        int rc = runHandle("notanumber\n", service, auth, r);
        assertEquals(0, rc);
    }
}
