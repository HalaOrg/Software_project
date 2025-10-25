package edu.library.service;

import edu.library.model.Book;
import java.util.ArrayList;
import java.util.List;

public class BookService {
    private List<Book> books = new ArrayList<>();

    // إضافة كتاب جديد
    public void addBook(Book book) {
        books.add(book);
        System.out.println(" Book added successfully: " + book.getTitle());
    }

    // البحث عن كتاب حسب العنوان أو المؤلف أو ISBN
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

    // عرض جميع الكتب
    public void displayBooks() {
        if (books.isEmpty()) {
            System.out.println("⚠️ No books available yet!");
        } else {
            System.out.println("\n📚 Books in Library:");
            for (Book b : books) {
                System.out.println(b);
            }
        }
    }
}


