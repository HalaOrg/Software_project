package edu.library.model;

import java.time.LocalDate;

public abstract class Media {
    private String title;
    private String author;
    private String isbn;
    private int totalCopies;
    private int availableCopies;
    private LocalDate dueDate;

    public Media(String title, String author, String isbn, int quantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = Math.max(0, quantity);
        this.availableCopies = this.totalCopies;
        this.dueDate = null;
    }

    public Media(String title, String author, String isbn, boolean available, LocalDate dueDate, int quantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = Math.max(0, quantity);
        this.availableCopies = available ? this.totalCopies : 0;
        this.dueDate = dueDate;
    }

    public Media(String title, String author, String isbn, int available, int total) {
        this.title=title;
        this.author=author;
        this.isbn = isbn;
        this.availableCopies=available;
        this.totalCopies=total;

    }

    // -----------------------------
    //       Getters + Setters
    // -----------------------------
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) {
        this.totalCopies = Math.max(0, totalCopies);
        // تعديل availableCopies إذا كانت أكبر من totalCopies
        if (availableCopies > this.totalCopies) {
            availableCopies = this.totalCopies;
        }
    }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = Math.max(0, Math.min(availableCopies, totalCopies));
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public void borrowOne() {
        if (availableCopies > 0) availableCopies--;
    }

    public void returnOne() {
        if (availableCopies < totalCopies) availableCopies++;
    }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public boolean isOverdue() {
        return !isAvailable() && dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    // كل نوع media عنده مدة استلاف مختلفة
    public abstract int getBorrowDurationDays();

    // كل نوع له غرامة مختلفة
    public abstract int getDailyFine();

    @Override
    public String toString() {
        return String.format("Title: %s | Author: %s | ISBN: %s | Available: %d/%d",
                title, author, isbn, availableCopies, totalCopies);
    }
}
