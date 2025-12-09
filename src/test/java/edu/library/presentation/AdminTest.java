package edu.library.presentation;

import edu.library.domain.model.Book;
import edu.library.domain.model.CD;
import edu.library.domain.model.Media;
import edu.library.domain.model.Roles;
import edu.library.service.AuthService;
import edu.library.service.MediaService;
import edu.library.service.ReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
        System.setIn(new ByteArrayInputStream(data.getBytes()));
    }

    @Test
    void testAddBook() {
        provideInput("1\nBook Title\nAuthor Name\n123456\n");
        int result = Admin.handle(new Scanner(System.in), mediaService, authService, reminderService, admin);

        assertEquals(0, result);
        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaService).addMedia(captor.capture());
        Media added = captor.getValue();
        assertTrue(added instanceof Book);
        assertEquals("Book Title", ((Book) added).getTitle());
    }

    @Test
    void testSearchBook_FoundBooks() {
        provideInput("2\nkeyword\n");
        Book book1 = new Book("Java Basics", "Author A", "B001");
        Book book2 = new Book("Advanced Java", "Author B", "B002");
        when(mediaService.searchMedia("keyword")).thenReturn(List.of(book1, book2));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        int result = Admin.handle(new Scanner(System.in), mediaService, authService, reminderService, admin);

        System.setOut(System.out);
        String output = outContent.toString();

        assertTrue(output.contains("Found books:"));
        assertTrue(output.contains("Java Basics"));
        assertTrue(output.contains("Advanced Java"));
        assertEquals(0, result);
    }

    @Test
    void testSearchBook_NoBooksFound() {
        provideInput("2\nkeyword\n");
        when(mediaService.searchMedia("keyword")).thenReturn(List.of());

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        int result = Admin.handle(new Scanner(System.in), mediaService, authService, reminderService, admin);

        System.setOut(System.out);
        assertTrue(outContent.toString().contains("No books found."));
        assertEquals(0, result);
    }

    @Test
    void testSearchCD_FoundCDs() {
        provideInput("12\nkeyword\n");
        CD cd1 = new CD("Best Hits", "Artist A", "CD001");
        CD cd2 = new CD("Top Charts", "Artist B", "CD002");
        when(mediaService.searchMedia("keyword")).thenReturn(List.of(cd1, cd2));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        int result = Admin.handle(new Scanner(System.in), mediaService, authService, reminderService, admin);

        System.setOut(System.out);
        String output = outContent.toString();
        assertTrue(output.contains("Found CDs:"));
        assertTrue(output.contains("Best Hits"));
        assertTrue(output.contains("Top Charts"));
        assertEquals(0, result);
    }

    @Test
    void testSearchCD_FilterOnlyCDs() {
        provideInput("12\nkeyword\n");
        CD cd = new CD("Top Hits", "Artist A", "CD001");
        Book book = new Book("Java Basics", "Author A", "B001");
        when(mediaService.searchMedia("keyword")).thenReturn(List.of(cd, book));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        int result = Admin.handle(new Scanner(System.in), mediaService, authService, reminderService, admin);

        System.setOut(System.out);
        String output = outContent.toString();
        assertTrue(output.contains("Found CDs:"));
        assertTrue(output.contains("Top Hits"));
        assertFalse(output.contains("Java Basics"));
        assertEquals(0, result);
    }

    @Test
    void testDisplayAllBooks() {
        provideInput("3\n");
        when(mediaService.getAllMedia()).thenReturn(List.of(
                new Book("B1","A1","1"),
                new CD("C1","Artist1","2")
        ));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        int result = Admin.handle(new Scanner(System.in), mediaService, authService, reminderService, admin);

        System.setOut(System.out);
        String output = outContent.toString();
        assertTrue(output.contains("B1"));
        assertFalse(output.contains("C1"));
        assertEquals(0, result);
    }

    @Test
    void testDisplayAllCDs() {
        provideInput("13\n");
        when(mediaService.getAllMedia()).thenReturn(List.of(
                new CD("C1","Artist1","2"),
                new Book("B1","A1","1")
        ));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        int result = Admin.handle(new Scanner(System.in), mediaService, authService, reminderService, admin);

        System.setOut(System.out);
        String output = outContent.toString();
        assertTrue(output.contains("C1"));
        assertFalse(output.contains("B1"));
        assertEquals(0, result);
    }
}
