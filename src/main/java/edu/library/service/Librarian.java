package edu.library.service;

import edu.library.model.Book;
import edu.library.model.CD;
import edu.library.model.Media;
import edu.library.model.Roles;
import edu.library.model.BorrowRecord;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Librarian {


    public static int handle(Scanner input, MediaService service, AuthService auth, Roles user) {
        System.out.println("\n--- Librarian Session: " + user.getUsername() + " (" + user.getRoleName() + ") ---");
        System.out.println("1. Add Media (Book/CD)");
        System.out.println("2. Display All Media");
        System.out.println("3. Search Media");
        System.out.println("4. View Borrow Records & Overdue Status");
        System.out.println("5. View Fine Balances");
        System.out.println("6. Update Media Quantity");
        System.out.println("7. Delete Media");
        System.out.println("8. Logout");
        System.out.println("9. Exit");
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
                System.out.print("Enter media type (Book/CD): ");
                String type = input.nextLine().trim();
                System.out.print("Enter title: ");
                String title = input.nextLine();
                System.out.print("Enter author/artist: ");
                String author = input.nextLine();
                System.out.print("Enter ISBN: ");
                String isbn = input.nextLine();
                System.out.print("Enter quantity: ");
                int qty = readInt(input, 1);
                if (qty < 1) {
                    System.out.println("Quantity must be at least 1.");
                    return 0;
                }
                Media media;
                if (type.equalsIgnoreCase("CD")) {
                    media = new CD(title, author, isbn, qty);
                } else {
                    media = new Book(title, author, isbn, qty);
                }
                service.addMedia(media);
                System.out.println("Media added successfully!");
                return 0;

            case 2:
                service.displayMedia();
                return 0;

            case 3:
                System.out.print("Enter title/author/ISBN to search: ");
                String keyword = input.nextLine();
                List<Media> found = service.searchMedia(keyword);
                if (found.isEmpty()) {
                    System.out.println("No matching media found!");
                } else {
                    System.out.println("Found media:");
                    for (Media m : found) System.out.println(m);
                }
                return 0;

            case 4:
                displayBorrowRecords(service);
                return 0;

            case 5:
                displayFineBalances(service);
                return 0;

            case 6:
                System.out.print("Enter ISBN to update quantity: ");
                String isbnUpdate = input.nextLine();
                System.out.print("Enter new total quantity: ");
                int newQty = readInt(input, 0);
                if (service.updateMediaQuantity(isbnUpdate, newQty)) {
                    System.out.println("Quantity updated for ISBN " + isbnUpdate);
                } else {
                    System.out.println("Could not update quantity (media not found or invalid number).");
                }
                return 0;

            case 7:
                System.out.print("Enter ISBN to delete: ");
                String isbnDelete = input.nextLine();
                if (service.deleteMedia(isbnDelete)) {
                    System.out.println("Deleted media with ISBN: " + isbnDelete);
                } else {
                    System.out.println("Media not found.");
                }
                return 0;

            case 8:
                if (auth.logout()) {
                    System.out.println(" Logged out successfully.");
                    return 1;
                } else {
                    System.out.println(" No user is currently logged in.");
                    return 0;
                }

            case 9:
                System.out.println("Exiting...");
                return 2;

            default:
                System.out.println("Invalid option. Try again.");
                return 0;
        }
    }

    private static void displayBorrowRecords(MediaService service) {
        List<BorrowRecord> records = service.getBorrowRecordService().getAllRecords();
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
                System.out.println("Overdue by " + daysOverdue + " day(s). Loans beyond 28 days trigger fines.");
            }
        }

        if (!anyOverdue) {
            System.out.println("No overdue items detected (all within 28-day window).");
        }
    }

    private static void displayFineBalances(MediaService service) {
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

    private static int readInt(Scanner input, int minValue) {
        String value = input.nextLine();
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < minValue) return -1;
            return parsed;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}