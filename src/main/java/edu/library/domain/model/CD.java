package edu.library.domain.model;

public class CD extends Media {

    public CD(String title, String author, String isbn) {
        super(title, author, isbn, 1);
    }

    public CD(String title, String author, String isbn, int quantity) {
        super(title, author, isbn, quantity);
    }

    public CD(String title, String author, String isbn, boolean available, java.time.LocalDate dueDate, int quantity) {
        super(title, author, isbn, available, dueDate, quantity);
    }

    public CD(String title, String author, String isbn, int available, int total) {
        super(title,author,isbn,available,total);
    }


    @Override
    public int getBorrowDurationDays() {
        return 7;
    }

    @Override
    public int getDailyFine() {
        return 20;
    }
}