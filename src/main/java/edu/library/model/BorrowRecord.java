package edu.library.model;

import java.time.LocalDate;

public class BorrowRecord {
    private final String username;
    private final String isbn;
    private final LocalDate dueDate;
    private boolean returned;
    private LocalDate returnDate;

    public BorrowRecord(String username, String isbn, LocalDate dueDate, boolean returned, LocalDate returnDate) {
        this.username = username;
        this.isbn = isbn;
        this.dueDate = dueDate;
        this.returned = returned;
        this.returnDate = returnDate;
    }
    public BorrowRecord(String isbn, String username, LocalDate dueDate) {
        this.isbn = isbn;
        this.username = username;
        this.dueDate = dueDate;
    }

    public String getUsername() {
        return username;
    }

    public String getIsbn() {
        return isbn;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void markReturned(LocalDate returnDate) {
        this.returned = true;
        this.returnDate = returnDate;
    }



}