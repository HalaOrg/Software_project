package edu.library.service;

import edu.library.model.Book;
import edu.library.model.Roles;

import java.util.List;
import java.util.Scanner;

public class Librarian {
    /**
     * Handle librarian menu actions.
     * return 0 = stay logged in, 1 = logout, 2 = exit app
     */
    public static int handle(Scanner input, BookService service, AuthService auth, Roles user) {
        System.out.println("--- Librarian Session: " + user.getUsername() + " (" + user.getRoleName() + ") ---");
        System.out.println("1. Add Book");
        System.out.println("2. Display All Books");
        System.out.println("3. Search Book");
        System.out.println("4. Logout");
        System.out.println("5. Exit");
        System.out.print("Choose option: ");
        String opt = input.nextLine();
        int optInt;
        try { optInt = Integer.parseInt(opt.trim()); } catch (Exception e) { System.out.println("Invalid option"); return 0; }
        switch (optInt) {
            case 1:
                System.out.print("Enter title: ");
                String title = input.nextLine();
                System.out.print("Enter author: ");
                String author = input.nextLine();
                System.out.print("Enter ISBN: ");
                String isbn = input.nextLine();
                service.addBook(new Book(title, author, isbn));
                return 0;
            case 2:
                service.displayBooks();
                return 0;
            case 3:
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
            case 4:
                if (auth.logout()) {
                    System.out.println("✅ Logged out successfully.");
                    return 1;
                } else {
                    System.out.println("⚠️ No user is currently logged in.");
                    return 0;
                }
            case 5:
                System.out.println("👋 Exiting...");
                return 2;
            default:
                System.out.println("❌ Invalid option. Try again.");
                return 0;
        }
    }
}
