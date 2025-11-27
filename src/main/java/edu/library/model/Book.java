package edu.library.model;
import java.time.LocalDate;

public class Book {
    private String title;
    private String author;
    private String isbn;
    private boolean available;
    private LocalDate dueDate;
    private int quantity;

    public Book(String title, String author, String isbn) {
        this(title, author, isbn, true, null, 1);
    }

    public Book(String title, String author, String isbn, boolean available, LocalDate dueDate) {
        this(title, author, isbn, available, dueDate, 1);
    }

    public Book(String title, String author, String isbn, boolean available, LocalDate dueDate, int quantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = available;
        this.dueDate = dueDate;
        this.quantity = Math.max(0, quantity);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public boolean isOverdue() {
        return !available && dueDate != null && LocalDate.now().isAfter(dueDate);
    }


    @Override
    public String toString() {
        return String.format("Title: %s | Author: %s | ISBN: %s | Qty: %d | Available: %s | DueDate: %s",
                title, author, isbn, quantity, available ? "Yes" : "No", dueDate != null ? dueDate : "N/A");
    }
}
