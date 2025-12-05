package edu.library.service;

import edu.library.model.Book;
import edu.library.model.CD;
import edu.library.model.Roles;
import org.junit.jupiter.api.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberTest {

    private MediaService service;
    private AuthService auth;
    private Roles user;

    private File fineFile;

    @BeforeEach
    void setup() throws IOException {
        service = new MediaService();
        auth = mock(AuthService.class);
        user = new Roles("testuser", "Test User", "test@example.com", "Member");

        service.addMedia(new Book("Java Programming", "Author A", "ISBN001", 1));
        service.addMedia(new Book("Python Basics", "Author B", "ISBN002", 1));
        service.addMedia(new CD("Java CD", "Author A", "CD001", 1));
        service.addMedia(new CD("Python CD", "Author B", "CD002", 1));

        when(auth.logout()).thenReturn(true);

        fineFile = File.createTempFile("fines", ".txt");
        fineFile.deleteOnExit();
        Member.fineFilePath = fineFile.getAbsolutePath();
    }

    @AfterEach
    void cleanup() {
        fineFile.delete();
    }

    private int runMemberWithInput(String inputStr) {
        return Member.handle(new java.util.Scanner(new ByteArrayInputStream(inputStr.getBytes())), service, auth, user);
    }

    private void writeFine(String username, int amount) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fineFile))) {
            bw.write(username + "," + amount);
        }
    }

    @Test
    void testSearchBookFoundAndNotFound() {
        assertEquals(0, runMemberWithInput("1\nJava\n"));
        assertEquals(0, runMemberWithInput("1\nNonExisting\n"));
    }

    @Test
    void testBorrowBook() {
        assertEquals(0, runMemberWithInput("2\nISBN001\n"));
        assertFalse(service.findBookByIsbn("ISBN001").isAvailable());

        assertEquals(0, runMemberWithInput("2\nINVALID\n"));

        assertEquals(0, runMemberWithInput("2\nISBN001\n"));
    }


    @Test
    void testDisplayAllBooks() {
        assertEquals(0, runMemberWithInput("4\n"));
    }


    @Test
    void testPayFines() throws IOException {
        assertEquals(0, runMemberWithInput("5\n"));

        writeFine("testuser", 50);
        String input = "5\n30\n";
        assertEquals(0, runMemberWithInput(input));
    }


    @Test
    void testRemainingTimeBooks() {
        service.borrowBook(service.findBookByIsbn("ISBN001"), user.getUsername());
        assertEquals(0, runMemberWithInput("6\n"));
    }


    @Test
    void testSearchCD() {
        assertEquals(0, runMemberWithInput("7\nJava\n"));
        assertEquals(0, runMemberWithInput("7\nNonExist\n"));
    }


    @Test
    void testBorrowCD() {
        assertEquals(0, runMemberWithInput("8\nCD001\n"));
        assertFalse(service.findCDByIsbn("CD001").isAvailable());

        assertEquals(0, runMemberWithInput("8\nINVALID\n"));

        assertEquals(0, runMemberWithInput("8\nCD001\n"));
    }


    @Test
    void testDisplayAllCDs() {
        assertEquals(0, runMemberWithInput("10\n"));
    }

    @Test
    void testRemainingTimeCDs() {
        service.borrowCD(service.findCDByIsbn("CD001"), user.getUsername());
        assertEquals(0, runMemberWithInput("11\n"));
    }


    @Test
    void testLogout() {
        assertEquals(1, runMemberWithInput("12\n"));
        verify(auth, times(1)).logout();
    }


    @Test
    void testExit() {
        assertEquals(2, runMemberWithInput("13\n"));
    }


    @Test
    void testInvalidOptionAndNonNumber() {
        assertEquals(0, runMemberWithInput("99\n"));
        assertEquals(0, runMemberWithInput("abc\n"));
    }
}
