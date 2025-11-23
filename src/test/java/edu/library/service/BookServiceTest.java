package edu.library.service;

import edu.library.model.Book;
import org.junit.jupiter.api.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookServiceTest {

    private static BookService service;
    private static Book book1;
    private static Book book2;

    @BeforeAll
    static void setup() {
        service = new BookService();
        File f = new File("books.txt");
        if (f.exists()) f.delete();
        service.loadBooksFromFile();

        book1 = new Book("The Hobbit", "J.R.R. Tolkien", "12345");
        book2 = new Book("LOTR", "Tolkien", "54321");
        service.addBook(book1);
        service.addBook(book2);
    }

    @Test
    @Order(1)
    void testAddBook() {
        Book book = new Book("Clean Code", "Robert Martin", "999");
        service.addBook(book);
        List<Book> results = service.searchBook("Clean Code");
        assertEquals(1, results.size());
        assertEquals("Clean Code", results.get(0).getTitle());
    }

    @Test
    @Order(2)
    void testSearchBook() {
        List<Book> foundByAuthor = service.searchBook("Tolkien");
        assertEquals(2, foundByAuthor.size());

        List<Book> foundByTitle = service.searchBook("The Hobbit");
        assertEquals(1, foundByTitle.size());
        assertEquals("The Hobbit", foundByTitle.get(0).getTitle());
    }

    @Test
    @Order(3)
    void testBorrowBookSuccess() {
        boolean result = service.borrowBook(book1, 28);
        assertTrue(result);
        assertFalse(book1.isAvailable());
        assertEquals(LocalDate.now().plusDays(28), book1.getDueDate());
    }

    @Test
    @Order(4)
    void testBorrowBookFailAlreadyBorrowed() {
        boolean result = service.borrowBook(book1, 28);
        assertFalse(result);
    }

    @Test
    @Order(5)
    void testReturnBookSuccess() {
        boolean result = service.returnBook(book1);
        assertTrue(result);
        assertTrue(book1.isAvailable());
        assertNull(book1.getDueDate());
    }

    @Test
    @Order(6)
    void testReturnBookFailNotBorrowed() {
        boolean result = service.returnBook(book2);
        assertFalse(result);
    }

    @Test
    @Order(7)
    void testOverdueDetection() {
        service.borrowBook(book2, -3);
        assertTrue(book2.isOverdue());
    }

    @Test
    @Order(8)
    void testCalculateFine() {
        int fine = service.calculateFine(book2);
        assertEquals(3 * 10, fine);
    }

    @AfterAll
    static void cleanup() {
        File file = new File("books.txt");
        if (file.exists()) file.delete();
    }
}
