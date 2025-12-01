package edu.library.service;

import edu.library.fine.FineCalculator;
import edu.library.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberTest {

    private MediaService mediaService;
    private AuthService authService;
    private Roles user;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService("test_media.txt",
                new BorrowRecordService(),
                new FineService(),
                () -> LocalDate.of(2025, 12, 1),
                new FineCalculator());

        authService = mock(AuthService.class);
        when(authService.logout()).thenReturn(true);

        user = new Roles("testUser", "member", "test@example.com");

        // إضافة كتب و CDs
        mediaService.addMedia(new Book("Java Programming", "Author A", "B001", 2, 2));
        mediaService.addMedia(new Book("Python Basics", "Author B", "B002", 1, 1));
        mediaService.addMedia(new CD("Best of 2020", "DJ C", "C001", 1, 1));
        mediaService.addMedia(new CD("Top Hits", "DJ D", "C002", 2, 2));
    }

    @Test
    void testSearchBookOption() {
        String input = "1\nJava\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Member.handle(scanner, mediaService, authService, user);
        assertEquals(0, result); // stay logged in

        List<Book> books = mediaService.searchBook("Java");
        assertEquals(1, books.size());
        assertEquals("B001", books.get(0).getIsbn());
    }

    @Test
    void testBorrowAndReturnBookOption() {
        String input = "2\nB001\n3\nB001\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        // Borrow Book
        int borrowResult = Member.handle(scanner, mediaService, authService, user);
        assertEquals(0, borrowResult);
        Book book = mediaService.findBookByIsbn("B001");
        assertEquals(1, book.getAvailableCopies());

        // Return Book
        int returnResult = Member.handle(scanner, mediaService, authService, user);
        assertEquals(0, returnResult);
        assertEquals(2, book.getAvailableCopies());
        assertNull(book.getDueDate());
    }

    @Test
    void testSearchCDOption() {
        String input = "7\nBest\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Member.handle(scanner, mediaService, authService, user);
        assertEquals(0, result);

        List<CD> cds = mediaService.searchCD("Best");
        assertEquals(1, cds.size());
        assertEquals("C001", cds.get(0).getIsbn());
    }

    @Test
    void testBorrowAndReturnCDOption() {
        String input = "8\nC002\n9\nC002\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        // Borrow CD
        int borrowResult = Member.handle(scanner, mediaService, authService, user);
        assertEquals(0, borrowResult);
        CD cd = mediaService.findCDByIsbn("C002");
        assertEquals(1, cd.getAvailableCopies());

        // Return CD
        int returnResult = Member.handle(scanner, mediaService, authService, user);
        assertEquals(0, returnResult);
        assertEquals(2, cd.getAvailableCopies());
        assertNull(cd.getDueDate());
    }

    @Test
    void testLogoutOption() {
        String input = "12\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Member.handle(scanner, mediaService, authService, user);
        assertEquals(1, result); // logged out
        verify(authService, times(1)).logout();
    }

    @Test
    void testExitOption() {
        String input = "13\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Member.handle(scanner, mediaService, authService, user);
        assertEquals(2, result); // exit app
    }
}
