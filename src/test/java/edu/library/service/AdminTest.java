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
        when(authService.userExists("memberUser")).thenReturn(true);

        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(0, result);
        verify(authService, never()).addUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testAddMemberSuccess() {
        provideInput("4\nmember@example.com\nmemberUser\npassword\n");
        when(authService.userExists("memberUser")).thenReturn(false);

        int result = Admin.handle(new java.util.Scanner(System.in), mediaService, authService, reminderService, admin);
        assertEquals(0, result);
        verify(authService).addUser("memberUser","password","MEMBER","member@example.com");
    }

    @Test
    void testAddLibrarianSuccess() {
        provideInput("5\nlibrarian@example.com\nlibUser\nlibpass\n");
        when(authService.userExists("libUser")).thenReturn(false);

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
        when(authService.logout()).thenReturn(true);
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

}
