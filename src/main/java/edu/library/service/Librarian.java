package edu.library.service;

import edu.library.model.Book;
import edu.library.model.Roles;
import edu.library.model.BorrowRecord;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Librarian {
    /**
     * Handle librarian menu actions.
     * return 0 = stay logged in, 1 = logout, 2 = exit app
     */
    public static int handle(Scanner input, BookService service, AuthService auth, Roles user) {
        System.out.println("\n--- Librarian Session: " + user.getUsername() + " (" + user.getRoleName() + ") ---");
        System.out.println("1. Add Book");
        System.out.println("2. Display All Books");
        System.out.println("3. Search Book");
        System.out.println("4. View Borrow Records & Overdue Status");
        System.out.println("5. View Fine Balances");
        System.out.println("6. Logout");
        System.out.println("7. Exit");
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
                displayBorrowRecords(service);
                return 0;
            case 5:
                displayFineBalances(service);
                return 0;
            case 6:
                if (auth.logout()) {
                    System.out.println("✅ Logged out successfully.");
                    return 1;
                } else {
                    System.out.println("⚠️ No user is currently logged in.");
                    return 0;
                }
            case 7:
                System.out.println("👋 Exiting...");
                return 2;
            default:
                System.out.println("❌ Invalid option. Try again.");
                return 0;
        }
    }
    private static void displayBorrowRecords(BookService service) {
        List<BorrowRecord> records = service.getBorrowRecords();
        if (records.isEmpty()) {
            System.out.println("No borrow records available.");
            return;
        }

        boolean anyOverdue = false;
        System.out.println("Borrow Records:");
        for (BorrowRecord record : records) {
            boolean overdue = !record.isReturned() && record.getDueDate() != null && LocalDate.now().isAfter(record.getDueDate());
            System.out.printf("User: %s | ISBN: %s | Due: %s | Returned: %s | ReturnDate: %s%n",
                    record.getUsername(), record.getIsbn(), record.getDueDate(), record.isReturned(), record.getReturnDate());
            if (overdue) {
                anyOverdue = true;
                long daysOverdue = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
                System.out.println("⚠️ Overdue by " + daysOverdue + " day(s). Loans beyond 28 days trigger fines.");
            }
        }

        if (!anyOverdue) {
            System.out.println("No overdue items detected (all within 28-day window).");
        }
    }

    private static void displayFineBalances(BookService service) {
        Map<String, Integer> fines = service.getAllFines();
        if (fines.isEmpty()) {
            System.out.println("No fines have been recorded.");
            return;
        }

        System.out.println("Outstanding fines:");
        for (Map.Entry<String, Integer> entry : fines.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " NIS");
        }
    }
}
