package edu.library.service;

import edu.library.model.Book;
import edu.library.model.BorrowRecord;
import java.time.temporal.ChronoUnit;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

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
                    int totalCopies = parts.length >= 6 ? Integer.parseInt(parts[5]) : 1;
                    int availableCopies = parts.length >= 7 ? Integer.parseInt(parts[6]) : (available ? totalCopies : 0);
                    Book book = new Book(title, author, isbn, available, dueDate, totalCopies);
                    book.setAvailableCopies(availableCopies);
                    books.add(book);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public void saveBookToFile(Book book) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(bookFilePath, true))) {
            writer.write(String.format("%s,%s,%s,%s,%s,%d,%d%n",
                    book.getTitle(), book.getAuthor(), book.getIsbn(),
                    book.isAvailable(), book.getDueDate() != null ? book.getDueDate() : "null",
                    book.getTotalCopies(), book.getAvailableCopies()));
        } catch (IOException e) {
            System.out.println("Error saving book: " + e.getMessage());
        }
    }

    public void saveAllBooksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(bookFilePath))) {
            for (Book b : books) {
                writer.write(String.format("%s,%s,%s,%s,%s,%d,%d%n",
                        b.getTitle(), b.getAuthor(), b.getIsbn(),
                        b.isAvailable(), b.getDueDate() != null ? b.getDueDate() : "null",
                        b.getTotalCopies(), b.getAvailableCopies()));
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

    public boolean updateBookQuantity(String isbn, int newQuantity) {
        Book book = findBookByIsbn(isbn);
        if (book == null || newQuantity < 0) return false;
        book.setTotalCopies(newQuantity);
        if (book.getAvailableCopies() > newQuantity) {
            book.setAvailableCopies(newQuantity);
        }
        saveAllBooksToFile();
        return true;
    }

    public boolean deleteBook(String isbn) {
        Book book = findBookByIsbn(isbn);
        if (book == null) return false;
        books.remove(book);
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
        if (!book.isAvailable()) {
            System.out.println("Book is currently borrowed by someone else.");
            return false;
        }
        LocalDate dueDate = LocalDate.now().plusDays(STANDARD_LOAN_DAYS);
        book.setAvailableCopies(Math.max(0, book.getAvailableCopies() - 1));
        book.setDueDate(dueDate);
        saveAllBooksToFile();
        borrowRecordService.recordBorrow(username, book.getIsbn(), dueDate);
        return true;
    }

    public boolean returnBook(Book book, String username) {
        if (book == null) return false;
        BorrowRecord active = borrowRecordService.findActiveBorrowRecord(username, book.getIsbn());
        if (active == null) return false;
        LocalDate originalDue = active.getDueDate();
        LocalDate returnDate = LocalDate.now();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            book.setDueDate(null);
        }
        saveAllBooksToFile();
        borrowRecordService.recordReturn(username, book.getIsbn(), returnDate);
        if (originalDue != null && returnDate.isAfter(originalDue)) {
            int fine = (int) ChronoUnit.DAYS.between(originalDue, returnDate) * 10;
            fineService.addFine(username, fine);
        }
        return true;
    }

    public int calculateFine(Book book) {
        BorrowRecord overdueRecord = null;
        for (BorrowRecord record : borrowRecordService.getRecords()) {
            if (!record.isReturned() && record.getIsbn().equals(book.getIsbn()) && record.getDueDate() != null && LocalDate.now().isAfter(record.getDueDate())) {
                overdueRecord = record;
                break;
            }
        }
        if (overdueRecord == null) return 0;
        return (int) ChronoUnit.DAYS.between(overdueRecord.getDueDate(), LocalDate.now()) * 10;
    }

    public List<Book> getOverdueBooks() {
        List<Book> overdue = new ArrayList<>();
        for (BorrowRecord record : borrowRecordService.getRecords()) {
            if (!record.isReturned() && record.getDueDate() != null && LocalDate.now().isAfter(record.getDueDate())) {
                Book book = findBookByIsbn(record.getIsbn());
                if (book != null && !overdue.contains(book)) {
                    overdue.add(book);
                }
            }
        }
        return overdue;
    }


    public List<Book> getBooks() {
        return books;
    }

    public List<BorrowRecord> getBorrowRecords() {
        return borrowRecordService.getRecords();
    }

    public List<BorrowRecord> getActiveBorrowRecordsForUser(String username) {
        return borrowRecordService.getActiveBorrowRecordsForUser(username);
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

    public Book findBookByIsbn(String isbn) {
        if (isbn == null) return null;
        for (Book b : books) {
            if (isbn.equalsIgnoreCase(b.getIsbn())) return b;
        }
        return null;
    }
}
