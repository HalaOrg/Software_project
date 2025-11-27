package edu.library.service;

import edu.library.model.Book;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.time.LocalDate;
import java.util.List;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class BookServiceTest {
    private BookService service;
    private Book book1;
    private Book book2;

    @TempDir
    Path tempDir;

    private Path originalCwd;

    @BeforeEach
    void setup() {
        // switch working dir to temp so BookService writes to tempDir/books.txt
        originalCwd = Path.of(System.getProperty("user.dir"));
        System.setProperty("user.dir", tempDir.toString());

        service = new BookService();
        // ensure a clean books file
        try {
            java.nio.file.Files.deleteIfExists(tempDir.resolve("books.txt"));
        } catch (Exception ignored) {
        }
        service.loadBooksFromFile();

        book1 = new Book("The Hobbit", "Tolkien", "12345");
        book2 = new Book("LOTR", "Tolkien", "54321");
        service.addBook(book1);
        service.addBook(book2);
    }

    @AfterEach
    void cleanup() {
        // restore working dir
        System.setProperty("user.dir", originalCwd.toString());
        try {
            java.nio.file.Files.deleteIfExists(tempDir.resolve("books.txt"));
        } catch (Exception ignored) {
        }
    }

    @Test
    void testAddBook() {
        Book book = new Book("Clean Code", "Robert Martin", "999");
        int before = service.getBooks().size();
        service.addBook(book);
        // list size should increase by one and contain the new title
        assertEquals(before + 1, service.getBooks().size());
        assertTrue(service.getBooks().stream().anyMatch(b -> "Clean Code".equals(b.getTitle())));
    }

    @Test
    void testSearchBook() {
        List<Book> foundByAuthor = service.searchBook("Tolkien");
        // ensure both books authored by Tolkien are present
        assertTrue(foundByAuthor.stream().anyMatch(b -> "The Hobbit".equals(b.getTitle())));
        assertTrue(foundByAuthor.stream().anyMatch(b -> "LOTR".equals(b.getTitle())));

        List<Book> foundByTitle = service.searchBook("The Hobbit");
        // assert that at least one result has the expected title (tolerant to extra entries)
        assertTrue(foundByTitle.stream().anyMatch(b -> "The Hobbit".equals(b.getTitle())));
    }

    @Test
    void testBorrowBookSuccess() {
        boolean result = service.borrowBook(book1);
        assertTrue(result);
        assertFalse(book1.isAvailable());
        assertEquals(LocalDate.now().plusDays(28), book1.getDueDate());
    }

    @Test
    void testBorrowBookFailAlreadyBorrowed() {
        service.borrowBook(book1);
        boolean result = service.borrowBook(book1);
        assertFalse(result);
    }

    @Test
    void testReturnBookSuccess() {
        service.borrowBook(book1);
        boolean result = service.returnBook(book1);
        assertTrue(result);
        assertTrue(book1.isAvailable());
        assertNull(book1.getDueDate());
    }

    @Test
    void testReturnBookFailNotBorrowed() {
        boolean result = service.returnBook(book2);
        assertFalse(result);
    }

    @Test
    void testOverdueDetection() {
        book2.setAvailable(false);
        book2.setDueDate(LocalDate.now().minusDays(3));
        assertTrue(book2.isOverdue());
        assertTrue(service.getOverdueBooks().contains(book2));
    }

    @Test
    void testCalculateFine() {
        book2.setAvailable(false);
        book2.setDueDate(LocalDate.now().minusDays(3));
        int fine = service.calculateFine(book2);
        assertEquals(3 * 10, fine);
    }
}
