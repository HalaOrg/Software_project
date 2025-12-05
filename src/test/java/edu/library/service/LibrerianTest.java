package edu.library.service;

import edu.library.model.Roles;
import edu.library.model.Media;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibrarianTest {

    private MediaService mediaService;
    private AuthService authService;
    private Roles user;

    @BeforeEach
    void setUp() {
        mediaService = mock(MediaService.class);
        authService = mock(AuthService.class);
        user = new Roles("librarian", "pass", "LIBRARIAN", "lib@example.com");
    }

    @Test
    void testHandle_AddMedia_BookAndCD() {
        Scanner scannerBook = new Scanner("""
                1
                Book
                MyBook
                Author
                12345
                5
                9
                """);

        int resultBook = Librarian.handle(scannerBook, mediaService, authService, user);
        ArgumentCaptor<Media> captorBook = ArgumentCaptor.forClass(Media.class);
        verify(mediaService).addMedia(captorBook.capture());
        assertEquals("MyBook", captorBook.getValue().getTitle());
        assertEquals(0, resultBook);


        Scanner scannerCD = new Scanner("""
                1
                CD
                MyCD
                Artist
                54321
                3
                9
                """);

        int resultCD = Librarian.handle(scannerCD, mediaService, authService, user);
        ArgumentCaptor<Media> captorCD = ArgumentCaptor.forClass(Media.class);
        verify(mediaService, times(2)).addMedia(captorCD.capture());
        assertEquals("MyCD", captorCD.getAllValues().get(1).getTitle());
    }

    @Test
    void testHandle_SearchMedia_FoundAndNotFound() {
        when(mediaService.searchMedia("Mock")).thenReturn(List.of(mock(Media.class)));
        when(mediaService.searchMedia("Nothing")).thenReturn(List.of());

        Scanner scanFound = new Scanner("""
                3
                Mock
                9
                """);
        int resultFound = Librarian.handle(scanFound, mediaService, authService, user);
        assertEquals(0, resultFound);

        Scanner scanNotFound = new Scanner("""
                3
                Nothing
                9
                """);
        int resultNotFound = Librarian.handle(scanNotFound, mediaService, authService, user);
        assertEquals(0, resultNotFound);
    }

    @Test
    void testHandle_UpdateAndDeleteMedia() {
        when(mediaService.updateMediaQuantity("123", 10)).thenReturn(true);
        when(mediaService.updateMediaQuantity("999", 5)).thenReturn(false);
        when(mediaService.deleteMedia("123")).thenReturn(true);
        when(mediaService.deleteMedia("999")).thenReturn(false);

        Scanner scannerUpdateSuccess = new Scanner("""
                6
                123
                10
                9
                """);
        int resUpdateSuccess = Librarian.handle(scannerUpdateSuccess, mediaService, authService, user);
        assertEquals(0, resUpdateSuccess);

        Scanner scannerUpdateFail = new Scanner("""
                6
                999
                5
                9
                """);
        int resUpdateFail = Librarian.handle(scannerUpdateFail, mediaService, authService, user);
        assertEquals(0, resUpdateFail);

        Scanner scannerDeleteSuccess = new Scanner("""
                7
                123
                9
                """);
        int resDeleteSuccess = Librarian.handle(scannerDeleteSuccess, mediaService, authService, user);
        assertEquals(0, resDeleteSuccess);

        Scanner scannerDeleteFail = new Scanner("""
                7
                999
                9
                """);
        int resDeleteFail = Librarian.handle(scannerDeleteFail, mediaService, authService, user);
        assertEquals(0, resDeleteFail);
    }

    @Test
    void testHandle_InvalidOption_Logout_Exit() {
        Scanner scanInvalid = new Scanner("abc\n9\n");
        int resInvalid = Librarian.handle(scanInvalid, mediaService, authService, user);
        assertEquals(0, resInvalid);

        when(authService.logout()).thenReturn(true);
        Scanner scanLogout = new Scanner("8\n");
        int resLogout = Librarian.handle(scanLogout, mediaService, authService, user);
        assertEquals(1, resLogout);

        Scanner scanExit = new Scanner("9\n");
        int resExit = Librarian.handle(scanExit, mediaService, authService, user);
        assertEquals(2, resExit);
    }

    @Test
    void testHandle_DisplayBorrowRecordsAndFines() {
        BorrowRecordService borrowService = mock(BorrowRecordService.class);
        when(mediaService.getBorrowRecordService()).thenReturn(borrowService);
        when(borrowService.getAllRecords()).thenReturn(List.of());

        Scanner scanBorrow = new Scanner("4\n9\n");
        int resBorrow = Librarian.handle(scanBorrow, mediaService, authService, user);
        assertEquals(0, resBorrow);

        when(mediaService.getAllFines()).thenReturn(Map.of());
        Scanner scanFines = new Scanner("5\n9\n");
        int resFines = Librarian.handle(scanFines, mediaService, authService, user);
        assertEquals(0, resFines);
    }
}
