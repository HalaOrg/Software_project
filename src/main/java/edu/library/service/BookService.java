package edu.library.service;

import edu.library.model.Book;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookService {
    private static final String FILE_PATH = "books.txt";
    private static final int STANDARD_LOAN_DAYS = 28;
    private List<Book> books = new ArrayList<>();

    public void loadBooksFromFile() {
        books.clear();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("No book file found, creating new one");
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String title = parts[0];
                    String author = parts[1];
                    String isbn = parts[2];
                    boolean available = Boolean.parseBoolean(parts[3]);
                    LocalDate dueDate = parts[4].equals("null") ? null : LocalDate.parse(parts[4]);
                    books.add(new Book(title, author, isbn, available, dueDate));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public void saveBookToFile(Book book) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(String.format("%s,%s,%s,%s,%s%n",
                    book.getTitle(), book.getAuthor(), book.getIsbn(),
                    book.isAvailable(), book.getDueDate() != null ? book.getDueDate() : "null"));
        } catch (IOException e) {
            System.out.println("Error saving book: " + e.getMessage());
        }
    }

    public void saveAllBooksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Book b : books) {
                writer.write(String.format("%s,%s,%s,%s,%s%n",
                        b.getTitle(), b.getAuthor(), b.getIsbn(),
                        b.isAvailable(), b.getDueDate() != null ? b.getDueDate() : "null"));
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

    public boolean borrowBook(Book book) {
        if (!book.isAvailable()) return false;
        book.setAvailable(false);
        book.setDueDate(LocalDate.now().plusDays(STANDARD_LOAN_DAYS));
        saveAllBooksToFile();
        return true;
    }

    public boolean returnBook(Book book) {
        if (book.isAvailable()) return false;
        book.setAvailable(true);
        book.setDueDate(null);
        saveAllBooksToFile();
        return true;
    }

    public int calculateFine(Book book) {
        if (!book.isOverdue()) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(book.getDueDate(), LocalDate.now()) * 10;
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
}
