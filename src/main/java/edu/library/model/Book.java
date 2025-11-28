package edu.library.model;
import java.time.LocalDate;

public class Book {
    private String title;
    private String author;
    private String isbn;
    private int totalCopies;
    private int availableCopies;
    private LocalDate dueDate;

    public Book(String title, String author, String isbn) {
        this(title, author, isbn, 1);
    }

    public Book(String title, String author, String isbn, int quantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = Math.max(0, quantity);
        this.availableCopies = this.totalCopies;
        this.dueDate = null;
    }

    public Book(String title, String author, String isbn, boolean available, LocalDate dueDate, int quantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = Math.max(0, quantity);
        this.availableCopies = available ? this.totalCopies : 0;
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
        return availableCopies > 0;
    }

    public void setAvailable(boolean available) {
        if (!available) {
            this.availableCopies = 0;
        } else if (this.availableCopies == 0) {
            this.availableCopies = Math.max(1, this.totalCopies);
            if (this.totalCopies == 0) this.totalCopies = this.availableCopies;
        }
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isOverdue() {
        return !isAvailable() && dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = Math.max(0, totalCopies);
        if (availableCopies > this.totalCopies) {
            availableCopies = this.totalCopies;
        }
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = Math.max(0, Math.min(availableCopies, totalCopies));
    }


    @Override
    public String toString() {
        return String.format("Title: %s | Author: %s | ISBN: %s | Available Copies: %d/%d",
                title, author, isbn, availableCopies, totalCopies);
    }
}
