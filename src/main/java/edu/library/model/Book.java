package edu.library.model;
import java.time.LocalDate;

public class Book {
    private String title;
    private String author;
    private String isbn;
    private boolean available;
    private LocalDate dueDate; // جديد

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = true;
        this.dueDate = null;
    }

    public Book(String title, String author, String isbn, boolean available, LocalDate dueDate) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = available;
        this.dueDate = dueDate;
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

    public boolean isOverdue() {
        return !available && dueDate != null && LocalDate.now().isAfter(dueDate);
    }


    @Override
    public String toString() {
        return String.format("Title: %s | Author: %s | ISBN: %s | Available: %s | DueDate: %s",
                title, author, isbn, available ? "Yes" : "No", dueDate != null ? dueDate : "N/A");
    }
}
