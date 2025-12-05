package edu.library.service;

import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import edu.library.model.Book;
import edu.library.model.CD;
import edu.library.model.BorrowRecord;
import edu.library.model.Media;
import edu.library.model.Roles;

public class Member {

    public static String fineFilePath = "fines.txt";


    public static int handle(Scanner input, MediaService service, AuthService auth, Roles user) {
        System.out.println("\n--- Member Session: " + user.getUsername() + " (" + user.getRoleName() + ") | " + user.getEmail() + " ---");
        System.out.println("1. Search Book");
        System.out.println("2. Borrow Book (28-day loan by ISBN)");
        System.out.println("3. Return Book (by ISBN)");
        System.out.println("4. Display All Books");
        System.out.println("5. Pay Fines");
        System.out.println("6. View Remaining Time for My Borrowed Books");
        System.out.println("7. Search CD");
        System.out.println("8. Borrow CD (7-day loan by ISBN)");
        System.out.println("9. Return CD (by ISBN)");
        System.out.println("10. Display All CDs");
        System.out.println("11. View Remaining Time for My Borrowed CDs");
        System.out.println("12. Logout");
        System.out.println("13. Exit");
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
            case 1 -> searchBook(input, service);
            case 2 -> borrowBook(input, service, user);
            case 3 -> returnBook(input, service, user);
            case 4 -> displayAllBooks(service);
            case 5 -> payFines(input, service, user);
            case 6 -> viewRemainingBooks(service, user);
            case 7 -> searchCD(input, service);
            case 8 -> borrowCD(input, service, user);
            case 9 -> returnCD(input, service, user);
            case 10 -> displayAllCDs(service);
            case 11 -> viewRemainingCDs(service, user);
            case 12 -> { // Logout
                if (auth.logout()) {
                    System.out.println("Logged out successfully.");
                    return 1;
                } else {
                    System.out.println("No user is currently logged in.");
                    return 0;
                }
            }
            case 13 -> {
                System.out.println("Exiting...");
                return 2;
            }
            default -> {
                System.out.println("Invalid option. Try again.");
                return 0;
            }
        }
        return 0;
    }


    private static int searchBook(Scanner input, MediaService service) {
        System.out.print("Enter title/author/ISBN to search: ");
        String keyword = input.nextLine();
        List<Book> foundBooks = service.searchBook(keyword);
        if (foundBooks.isEmpty()) {
            System.out.println("No matching books found!");
        } else {
            System.out.println("Found books:");
            for (Book b : foundBooks) System.out.println(b);
        }
        return 0;
    }

    private static int borrowBook(Scanner input, MediaService service, Roles user) {
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
            System.out.println("Book borrowed successfully for 28 days. Due date: " + bookToBorrow.getDueDate());
        } else {
            System.out.println("Could not borrow book.");
        }
        return 0;
    }

    private static int returnBook(Scanner input, MediaService service, Roles user) {
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

    private static void displayAllBooks(MediaService service) {
        displayMediaList(new ArrayList<>(service.getBooks()));
    }

    private static void payFines(Scanner input, MediaService service, Roles user) {
        handleFinePayment(input, service, user.getUsername());
    }

    private static void viewRemainingBooks(MediaService service, Roles user) {
        List<BorrowRecord> activeBorrows = service.getActiveBorrowRecordsForUser(user.getUsername());
        boolean anyBook = false;

        for (BorrowRecord record : activeBorrows) {
            Book book = service.findBookByIsbn(record.getIsbn());
            if (book != null) {
                anyBook = true;
                long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), record.getDueDate());
                if (daysRemaining < 0) {
                    System.out.println("Book ISBN: " + record.getIsbn() + " is overdue by " + Math.abs(daysRemaining) + " day(s).");
                } else {
                    System.out.println("Book ISBN: " + record.getIsbn() + " - " + daysRemaining + " day(s) remaining.");
                }
            }
        }

        if (!anyBook) System.out.println("You have no active borrowed books.");
    }


    private static int searchCD(Scanner input, MediaService service) {
        System.out.print("Enter title/author/ISBN to search: ");
        String keyword = input.nextLine();
        List<CD> foundCDs = service.searchCD(keyword);
        if (foundCDs.isEmpty()) {
            System.out.println("No matching CDs found!");
        } else {
            System.out.println("Found CDs:");
            for (CD cd : foundCDs) System.out.println(cd);
        }
        return 0;
    }

    private static int borrowCD(Scanner input, MediaService service, Roles user) {
        System.out.print("Enter CD ISBN to borrow: ");
        String cdIsbn = input.nextLine();

        CD cdToBorrow = service.findCDByIsbn(cdIsbn);
        if (cdToBorrow == null) {
            System.out.println("CD not found.");
            return 0;
        }

        if (!cdToBorrow.isAvailable()) {
            System.out.println("CD is currently not available :(");
            return 0;
        }

        if (service.borrowCD(cdToBorrow, user.getUsername())) {
            System.out.println("CD borrowed successfully for 7 days. Due date: " + cdToBorrow.getDueDate());
        } else {
            System.out.println("Could not borrow CD.");
        }
        return 0;
    }

    private static int returnCD(Scanner input, MediaService service, Roles user) {
        System.out.print("Enter ISBN to return: ");
        String isbnReturn = input.nextLine();

        CD cdToReturn = service.findCDByIsbn(isbnReturn);
        if (cdToReturn == null) {
            System.out.println("CD not found.");
            return 0;
        }

        boolean hasActiveLoan = service.getActiveBorrowRecordsForUser(user.getUsername())
                .stream()
                .anyMatch(r -> r.getIsbn().equalsIgnoreCase(isbnReturn));
        if (!hasActiveLoan) {
            System.out.println("This CD is not borrowed.");
            return 0;
        }

        if (service.returnCD(cdToReturn, user.getUsername())) {
            System.out.println("CD returned successfully.");

            int outstanding = service.getOutstandingFine(user.getUsername());

            if (outstanding > 0) {
                handleFinePayment(input, service, user.getUsername());
            } else {
                System.out.println("No outstanding fines for this user.");
            }

        } else {
            System.out.println("Could not return CD.");
        }
        return 0;
    }

    private static void displayAllCDs(MediaService service) {
        displayMediaList(new ArrayList<>(service.getCDs()));
    }

    private static void viewRemainingCDs(MediaService service, Roles user) {
        List<BorrowRecord> activeBorrows = service.getActiveBorrowRecordsForUser(user.getUsername());
        boolean anyCD = false;

        for (BorrowRecord record : activeBorrows) {
            CD cd = service.findCDByIsbn(record.getIsbn());
            if (cd != null) {
                anyCD = true;
                long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), record.getDueDate());
                if (daysRemaining < 0) {
                    System.out.println("CD ISBN: " + record.getIsbn() + " is overdue by " + Math.abs(daysRemaining) + " day(s).");
                } else {
                    System.out.println("CD ISBN: " + record.getIsbn() + " - " + daysRemaining + " day(s) remaining.");
                }
            }
        }

        if (!anyCD) {
            System.out.println("You have no active borrowed CDs.");
        }
    }


    public static int getOutstandingFineFromFile(String username) {
        File file = new File(fineFilePath);

        if (!file.exists()) return 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equalsIgnoreCase(username)) {
                    return Integer.parseInt(parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading fine file: " + e.getMessage());
        }
        return 0;
    }

    public static void updateFineFile(String username, int newFine) {
        File file = new File(fineFilePath);
        List<String> lines = new ArrayList<>();
        boolean userFound = false;

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating fine file: " + e.getMessage());
                return;
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equalsIgnoreCase(username)) {
                    line = username + "," + newFine;
                    userFound = true;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading fine file: " + e.getMessage());
            return;
        }

        if (!userFound) {
            lines.add(username + "," + newFine);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing fine file: " + e.getMessage());
        }
    }

    private static void displayMediaList(List<? extends Media> mediaList) {
        if (mediaList.isEmpty()) {
            System.out.println("No items available.");
            return;
        }

        for (Media media : mediaList) {
            String due = media.getDueDate() != null ? media.getDueDate().toString() : "None";
            System.out.printf("%s | ISBN: %s | Available: %d/%d | Due: %s%n",
                    media.getTitle(),
                    media.getIsbn(),
                    media.getAvailableCopies(),
                    media.getTotalCopies(),
                    due);
        }
    }

    private static void handleFinePayment(Scanner input, MediaService service, String username) {
        int outstanding = service.getOutstandingFine(username);
        if (outstanding == 0) {
            System.out.println("You have no outstanding fines.");
            return;
        }

        System.out.println("Your outstanding fines: " + outstanding + " NIS.");
        System.out.print("Enter amount to pay: ");
        int amt;
        try {
            amt = Integer.parseInt(input.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid amount.");
            return;
        }

        if (amt <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        if (amt > outstanding) amt = outstanding;

        service.payFine(username, amt);
        System.out.println("Paid " + amt + " NIS. Remaining fines: " + service.getOutstandingFine(username) + " NIS.");
    }
}
