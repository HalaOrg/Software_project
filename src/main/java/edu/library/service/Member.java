package edu.library.service;

import edu.library.model.Book;
import edu.library.model.Roles;

import java.util.List;
import java.util.Scanner;

public class Member {
    /**
     * Handle member menu actions.
     * return 0 = stay logged in, 1 = logout, 2 = exit app
     */
    public static int handle(Scanner input, BookService service, AuthService auth, Roles user) {
        System.out.println("1. Search Book");
        System.out.println("2. Borrow Book (by ISBN)");
        System.out.println("3. Return Book (by ISBN)");
        System.out.println("4. Display All Books");
        System.out.println("5. Logout");
        System.out.println("6. Exit");
        System.out.print("Choose option: ");
        String opt = input.nextLine();
        int optInt;
        try {
            optInt = Integer.parseInt(opt.trim());
        } catch (Exception e) {
            System.out.println("Invalid option");
            return 0;
        }
        switch (optInt) {
            case 1:
                System.out.print("Enter title/author/ISBN to search: ");
                String keyword = input.nextLine();
                List<Book> foundBooks = service.searchBook(keyword);
                if (foundBooks.isEmpty()) {
                    System.out.println("❌ No matching books found!");
                } else {
                    System.out.println("✅ Found books:");
                    for (Book b : foundBooks) System.out.println(b);
                }
                return 0;
            case 2:
                System.out.print("Enter ISBN to borrow: ");
                String isbnBorrow = input.nextLine();
                Book bookToBorrow = findBookByIsbn(service, isbnBorrow);
                if (bookToBorrow == null) {
                    System.out.println("Book not found.");
                } else if (!bookToBorrow.isAvailable()) {
                    System.out.println("Book is currently not available.");
                } else {
                    System.out.print("For how many days? ");
                    String daysStr = input.nextLine();
                    int days = 0;
                    try {
                        days = Integer.parseInt(daysStr.trim());
                    } catch (Exception e) {
                        System.out.println("Invalid number");
                        break;
                    }
                    if (service.borrowBook(bookToBorrow, days)) System.out.println("Book borrowed successfully.");
                    else System.out.println("Could not borrow book.");
                }
                return 0;
            case 3:
                System.out.print("Enter ISBN to return: ");
                String isbnReturn = input.nextLine();
                Book bookToReturn = findBookByIsbn(service, isbnReturn);
                if (bookToReturn == null) {
                    System.out.println("Book not found.");
                } else if (bookToReturn.isAvailable()) {
                    System.out.println("This book is not borrowed.");
                } else {
                    if (service.returnBook(bookToReturn)) System.out.println("Book returned successfully.");
                    else System.out.println("Could not return book.");
                }
                return 0;
            case 4:
                service.displayBooks();
                return 0;
            case 5:
                if (auth.logout()) {
                    System.out.println("✅ Logged out successfully.");
                    return 1;
                } else {
                    System.out.println("⚠️ No user is currently logged in.");
                    return 0;
                }
            case 6:
                System.out.println("👋 Exiting...");
                return 2;
            default:
                System.out.println("❌ Invalid option. Try again.");
                return 0;
        }
    }


    private static Book findBookByIsbn(BookService service, String isbn) {
        if (isbn == null) return null;
        for (Book b : service.getBooks()) {
            if (isbn.equalsIgnoreCase(b.getIsbn())) return b;
        }
        return null;
    }
}
