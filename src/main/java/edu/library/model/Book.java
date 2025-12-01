package edu.library.model;
import java.time.LocalDate;

public class Book extends Media {

    public Book(String title, String author, String isbn) {
        super(title, author, isbn, 1);
    }

    public Book(String title, String author, String isbn, int quantity) {
        super(title, author, isbn, quantity);
    }

    public Book(String title, String author, String isbn, boolean available, java.time.LocalDate dueDate, int quantity) {
        super(title, author, isbn, available, dueDate, quantity);
    }

    public Book(String title, String author, String isbn, int available, int total) {
        super(title,author,isbn,available,total);
    }

    @Override
    public int getBorrowDurationDays() {
        return 28; // مدة الكتاب
    }

    @Override
    public int getDailyFine() {
        return 10; // غرامة الكتاب
    }
}
