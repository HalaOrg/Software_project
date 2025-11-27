package edu.library.service;

import edu.library.model.Book;
import edu.library.model.BorrowRecord;
import java.time.temporal.ChronoUnit;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookService {
    private final String bookFilePath;
    private static final int STANDARD_LOAN_DAYS = 28;
    private final BorrowRecordService borrowRecordService;
    private final FineService fineService;
    private List<Book> books = new ArrayList<>();

    public BookService() {
        this("books.txt", new BorrowRecordService(), new FineService());
    }
    public BookService(String bookFilePath, BorrowRecordService borrowRecordService, FineService fineService) {
        this.bookFilePath = bookFilePath;
        this.borrowRecordService = borrowRecordService;
        this.fineService = fineService;
    }
    public void loadBooksFromFile() {
        books.clear();
        File file = new File(bookFilePath);
        if (!file.exists()) {
            System.out.println("No book file found, creating new one");
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(bookFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String title = parts[0];
                    String author = parts[1];
                    String isbn = parts[2];
                    boolean available = Boolean.parseBoolean(parts[3]);
                    LocalDate dueDate = parts[4].equals("null") ? null : LocalDate.parse(parts[4]);
                    int quantity = parts.length >= 6 ? parseQuantity(parts[5]) : 1;
                    if (quantity <= 0) {
                        available = false;
                    }
                    books.add(new Book(title, author, isbn, available, dueDate, quantity));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public void saveBookToFile(Book book) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(bookFilePath, true))) {
            writer.write(String.format("%s,%s,%s,%s,%s,%d%n",
                    book.getTitle(), book.getAuthor(), book.getIsbn(),
                    book.isAvailable(), book.getDueDate() != null ? book.getDueDate() : "null", book.getQuantity()));
        } catch (IOException e) {
            System.out.println("Error saving book: " + e.getMessage());
        }
    }

    public void saveAllBooksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(bookFilePath))) {
            for (Book b : books) {
                writer.write(String.format("%s,%s,%s,%s,%s,%d%n",
                        b.getTitle(), b.getAuthor(), b.getIsbn(),
                        b.isAvailable(), b.getDueDate() != null ? b.getDueDate() : "null", b.getQuantity()));
            }
        } catch (IOException e) {
            System.out.println("Error saving books: " + e.getMessage());
        }

    }

    public void addBook(Book book) {
        books.add(book);
        saveBookToFile(book);
        System.out.println("Book added successfully: " + book.getTitle());
    }

    public boolean updateQuantity(String isbn, int newQuantity) {
        Book target = findByIsbn(isbn);
        if (target == null || newQuantity < 0) {
            return false;
        }
        target.setQuantity(newQuantity);
        target.setAvailable(newQuantity > 0);
        if (newQuantity == 0) {
            target.setDueDate(null);
        }
        saveAllBooksToFile();
        return true;
    }

    public boolean deleteBook(String isbn) {
        Book target = findByIsbn(isbn);
        if (target == null) return false;
        if (hasActiveBorrow(isbn)) {
            System.out.println("Cannot delete a book that is currently borrowed.");
            return false;
        }
        books.remove(target);
        saveAllBooksToFile();
        return true;
    }

    public List<Book> searchBook(String keyword) {
        List<Book> results = new ArrayList<>();
        for (Book b : books) {
            if (b.getTitle().equalsIgnoreCase(keyword)
                    || b.getAuthor().equalsIgnoreCase(keyword)
                    || b.getIsbn().equalsIgnoreCase(keyword)) {
                results.add(b);
            }
        }
        return results;
    }

    public void displayBooks() {
        if (books.isEmpty()) {
            System.out.println(" No books available yet ");
        } else {
            System.out.println("\n Books in Library:");
            for (Book b : books) {
                System.out.println(b);
            }
        }
    }
    public boolean borrowBook(Book book, String username) {
        if (book == null || username == null) return false;
        if (fineService.getBalance(username) > 0) {
            System.out.println("Outstanding fines must be paid before borrowing.");
            return false;
        }
        if (hasActiveBorrowByUser(username, book.getIsbn())) {
            System.out.println("You already borrowed this book.");
            return false;
        }
        if (!book.isAvailable() || book.getQuantity() <= 0) {
            System.out.println("Book is currently borrowed by someone else.");
            return false;
        }
        LocalDate dueDate = LocalDate.now().plusDays(STANDARD_LOAN_DAYS);
        book.setQuantity(book.getQuantity() - 1);
        book.setAvailable(book.getQuantity() > 0);
        book.setDueDate(dueDate);
        saveAllBooksToFile();
        borrowRecordService.recordBorrow(username, book.getIsbn(), dueDate);
        return true;
    }

    public boolean returnBook(Book book, String username) {
        if (book == null || book.isAvailable()) return false;
        LocalDate originalDue = book.getDueDate();
        LocalDate returnDate = LocalDate.now();
        book.setQuantity(book.getQuantity() + 1);
        book.setAvailable(book.getQuantity() > 0);
        book.setDueDate(null);
        saveAllBooksToFile();
        borrowRecordService.recordReturn(username, book.getIsbn(), returnDate);
        if (originalDue != null && returnDate.isAfter(originalDue)) {
            int fine = (int) ChronoUnit.DAYS.between(originalDue, returnDate) * 10;
            fineService.addFine(username, fine);
        }
        return true;
    }

    public int calculateFine(Book book) {
        if (!book.isOverdue()) return 0;
        return (int) ChronoUnit.DAYS.between(book.getDueDate(), LocalDate.now()) * 10;
    }

    public List<Book> getOverdueBooks() {
        List<Book> overdue = new ArrayList<>();
        for (Book book : books) {
            if (book.isOverdue()) {
                overdue.add(book);
            }
        }
        return overdue;
    }


    public List<Book> getBooks() {
        return books;
    }

    public List<BorrowRecord> getBorrowRecordsForUser(String username) {
        List<BorrowRecord> matches = new ArrayList<>();
        for (BorrowRecord record : borrowRecordService.getRecords()) {
            if (record.getUsername().equals(username)) {
                matches.add(record);
            }
        }
        return matches;
    }

    public List<BorrowRecord> getBorrowRecords() {
        return borrowRecordService.getRecords();
    }

    public int getOutstandingFine(String username) {
        return fineService.getBalance(username);
    }

    public Map<String, Integer> getAllFines() {
        return fineService.getAllBalances();
    }

    public int payFine(String username, int amount) {
        return fineService.payFine(username, amount);
    }

    private Book findByIsbn(String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equalsIgnoreCase(isbn)) {
                return b;
            }
        }
        return null;
    }

    private boolean hasActiveBorrow(String isbn) {
        for (BorrowRecord record : borrowRecordService.getRecords()) {
            if (record.getIsbn().equalsIgnoreCase(isbn) && !record.isReturned()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasActiveBorrowByUser(String username, String isbn) {
        for (BorrowRecord record : borrowRecordService.getRecords()) {
            if (!record.isReturned() && record.getUsername().equals(username)
                    && record.getIsbn().equalsIgnoreCase(isbn)) {
                return true;
            }
        }
        return false;
    }

    private int parseQuantity(String value) {
        try {
            int qty = Integer.parseInt(value.trim());
            return Math.max(0, qty);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}
