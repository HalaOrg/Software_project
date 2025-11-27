package edu.library.service;

import edu.library.model.Book;
import edu.library.model.BorrowRecord;
import edu.library.model.Roles;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;

public class Member {
    /**
     * Handle member menu actions.
     * return 0 = stay logged in, 1 = logout, 2 = exit app
     */
    public static int handle(Scanner input, BookService service, AuthService auth, Roles user) {
        System.out.println("\n--- Member Session: " + user.getUsername() + " (" + user.getRoleName() + ") | " + user.getEmail() + " ---");
        System.out.println("1. Search Book");
        System.out.println("2. Borrow Book (28-day loan by ISBN)");
        System.out.println("3. Return Book (by ISBN)");
        System.out.println("4. Display All Books");
        System.out.println("5. Pay Fines");
        System.out.println("6. View Remaining Time for My Borrowed Books");
        System.out.println("7. Logout");
        System.out.println("8. Exit");
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
            case 1: {
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
            }
            case 2: {
                System.out.print("Enter ISBN to borrow: ");
                String isbnBorrow = input.nextLine();
                Book bookToBorrow = service.findBookByIsbn(isbnBorrow);
                if (bookToBorrow == null) {
                    System.out.println("Book not found.");
                    return 0;
                }
                if (!bookToBorrow.isAvailable()) {
                    System.out.println("Book is currently not available :(");
                    return 0;
                }

                if (service.borrowBook(bookToBorrow, user.getUsername())) {
                    System.out.println("✅ Book borrowed successfully for 28 days. Due date: " + bookToBorrow.getDueDate());
                }
                else {
                    System.out.println("Could not borrow book.");
                }
                return 0;
            }
            case 3: {
                System.out.print("Enter ISBN to return: ");
                String isbnReturn = input.nextLine();
                Book bookToReturn = service.findBookByIsbn(isbnReturn);
                if (bookToReturn == null) {
                    System.out.println("Book not found.");
                    return 0;
                }
                boolean hasActiveLoan = service.getActiveBorrowRecordsForUser(user.getUsername()).stream()
                        .anyMatch(r -> r.getIsbn().equalsIgnoreCase(isbnReturn));
                if (!hasActiveLoan) {
                    System.out.println("This book is not borrowed.");
                    return 0;
                }
                if (service.returnBook(bookToReturn, user.getUsername())) {
                    System.out.println("Book returned successfully.");
                    int outstanding = service.getOutstandingFine(user.getUsername());
                    if (outstanding > 0) {
                        System.out.println("You have outstanding fines: " + outstanding + " NIS.");
                    }
                } else {
                    System.out.println("Could not return book.");
                }
                return 0;
            }
            case 4: {
                service.displayBooks();
                return 0;
            }
            case 5: {
                int outstanding = service.getOutstandingFine(user.getUsername());
                if (outstanding == 0) {
                    System.out.println("You have no outstanding fines.");
                    return 0;
                }
                System.out.println("Your outstanding fines: " + outstanding + " NIS.");
                System.out.print("Enter amount to pay: ");
                String amtStr = input.nextLine();
                int amt;
                try {
                    amt = Integer.parseInt(amtStr.trim());
                } catch (Exception e) {
                    System.out.println("Invalid amount.");
                    return 0;
                }
                if (amt <= 0) {
                    System.out.println("Amount must be positive.");
                    return 0;
                }
                if (amt > outstanding) {
                    amt = outstanding; // pay only what is owed
                }
                service.payFine(user.getUsername(), amt);
                System.out.println("Paid " + amt + " NIS. Remaining fines: " + service.getOutstandingFine(user.getUsername()) + " NIS.");
                return 0;
            }
            case 6: {
                List<BorrowRecord> activeBorrows = service.getActiveBorrowRecordsForUser(user.getUsername());
                if (activeBorrows.isEmpty()) {
                    System.out.println("You have no active borrowed books.");
                    return 0;
                }
                for (BorrowRecord record : activeBorrows) {
                    long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), record.getDueDate());
                    if (daysRemaining < 0) {
                        System.out.println("ISBN: " + record.getIsbn() + " is overdue by " + Math.abs(daysRemaining) + " day(s).");
                    } else {
                        System.out.println("ISBN: " + record.getIsbn() + " - " + daysRemaining + " day(s) remaining.");
                    }
                }
                return 0;
            }
            case 7: {
                if (auth.logout()) {
                    System.out.println("✅ Logged out successfully.");
                    return 1;
                } else {
                    System.out.println("⚠️ No user is currently logged in.");
                    return 0;
                }
            }
            case 8: {
                System.out.println("👋 Exiting...");
                return 2;
            }
            default: {
                System.out.println("❌ Invalid option. Try again.");
                return 0;
            }
        }
    }
}
