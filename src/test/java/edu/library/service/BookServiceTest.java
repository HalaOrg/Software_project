package edu.library.service;

import edu.library.model.Book;
import org.junit.jupiter.api.*;
import edu.library.service.BorrowRecordService;
import edu.library.service.FineService;
import org.junit.jupiter.api.io.TempDir;
import java.time.LocalDate;
import java.util.List;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class BookServiceTest {
    private BookService service;
    private BorrowRecordService borrowRecordService;
    private Book book1;
    private Book book2;

    @TempDir
    Path tempDir;

    private Path originalCwd;

    @BeforeEach
    void setup() {
        originalCwd = Path.of(System.getProperty("user.dir"));
        System.setProperty("user.dir", tempDir.toString());

        borrowRecordService = new BorrowRecordService(tempDir.resolve("borrow_records.txt").toString());
        service = new BookService(tempDir.resolve("books.txt").toString(),
                borrowRecordService,
                new FineService(tempDir.resolve("fines.txt").toString()));
        service.loadBooksFromFile();

        book1 = new Book("The Hobbit", "Tolkien", "12345", 1);
        book2 = new Book("LOTR", "Tolkien", "54321", 2);
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
        Book book = new Book("Clean Code", "Robert Martin", "999", 3);
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
        boolean result = service.borrowBook(book1, "member");
        assertTrue(result);
        assertFalse(book1.isAvailable());
        assertEquals(0, book1.getAvailableCopies());
    }

    @Test
    void testBorrowBookFailAlreadyBorrowed() {
        service.borrowBook(book1, "member");
        boolean result = service.borrowBook(book1, "member");
        assertFalse(result);
    }

    @Test
    void testBorrowBookFailWhenNoCopiesForDifferentUser() {
        service.borrowBook(book1, "member1");
        boolean result = service.borrowBook(book1, "member2");
        assertFalse(result);
        assertEquals(0, book1.getAvailableCopies());
    }

    @Test
    void testReturnBookSuccess() {
        service.borrowBook(book1, "member");
        boolean result = service.returnBook(book1, "member");
        assertTrue(result);
        assertTrue(book1.isAvailable());
        assertNull(book1.getDueDate());
    }

    @Test
    void testReturnBookFailNotBorrowed() {
        boolean result = service.returnBook(book2, "member");
        assertFalse(result);
    }

    @Test
    void testOverdueDetection() {
        borrowRecordService.recordBorrow("member2", book2.getIsbn(), LocalDate.now().minusDays(3));
        assertTrue(service.getOverdueBooks().contains(book2));
    }

    @Test
    void testCalculateFine() {
        borrowRecordService.recordBorrow("member2", book2.getIsbn(), LocalDate.now().minusDays(3));
        int fine = service.calculateFine(book2);
        assertEquals(3 * 10, fine);
    }

    @Test
    void updateQuantityAndDeleteBook() {
        assertTrue(service.updateBookQuantity(book2.getIsbn(), 5));
        assertEquals(5, service.searchBook(book2.getIsbn()).get(0).getTotalCopies());

        assertTrue(service.deleteBook(book2.getIsbn()));
        assertFalse(service.searchBook(book2.getIsbn()).stream().findFirst().isPresent());
    }
}
