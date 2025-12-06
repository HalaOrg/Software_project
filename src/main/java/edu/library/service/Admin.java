package edu.library.service;

import edu.library.model.Book;
import edu.library.model.CD;
import edu.library.model.Media;
import edu.library.model.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Admin {

    private Admin() {
        // prevent instantiation
    }
    public static int handle(Scanner input,
                             MediaService service,
                             AuthService auth,
                             ReminderService reminderService,
                             Roles user) {

        System.out.println("\n--- Admin Session: " + user.getUsername() +
                " (" + user.getRoleName() + ") | " + user.getEmail() + " ---");

        System.out.println("1. Add Book");
        System.out.println("2. Search Book");
        System.out.println("3. Display All Books");
        System.out.println("4. Add Member");
        System.out.println("5. Add Librarian");
        System.out.println("6. Remove User");
        System.out.println("7. List Users");
        System.out.println("8. Send overdue reminders");
        System.out.println("9. Logout");
        System.out.println("10. Exit");

        System.out.println("11. Add CD");
        System.out.println("12. Search CD");
        System.out.println("13. Display All CDs");

        System.out.print("Choose option: ");

        String opt = input.nextLine();
        int optInt;
        try { optInt = Integer.parseInt(opt.trim()); }
        catch (Exception e) { System.out.println("Invalid option"); return 0; }

        switch (optInt) {

            case 1:
                System.out.print("Enter title: ");
                String title = input.nextLine();
                System.out.print("Enter author: ");
                String author = input.nextLine();
                System.out.print("Enter ISBN: ");
                String isbn = input.nextLine();

                service.addMedia(new Book(title, author, isbn));
                System.out.println("Book added successfully!");
                return 0;

            case 2:
                System.out.print("Enter title/author/ISBN to search: ");
                String keyword = input.nextLine();

                List<Media> found = service.searchMedia(keyword);
                List<Book> foundBooks = new ArrayList<>();
                for (Media m : found) if (m instanceof Book) foundBooks.add((Book)m);

                if (foundBooks.isEmpty()) System.out.println(" No matching books found!");
                else {
                    System.out.println("Found books:");
                    for (Book b : foundBooks) System.out.println(b);
                }
                return 0;

            case 3:
                System.out.println("All Books:");
                for (Media m : service.getAllMedia()) {
                    if (m instanceof Book) System.out.println(m);
                }
                return 0;

            case 4:
                String memEmail;
                while (true) {
                    System.out.print("Enter new member's email: ");
                    memEmail = input.nextLine().trim();
                    if (memEmail.isEmpty()) {
                        System.out.println("Email is required and cannot be empty.");
                        continue;
                    }
                    break;
                }

                System.out.print("Enter new member's username: ");
                String memUser = input.nextLine();
                if (auth.userExists(memUser)) {
                    System.out.println(" User already exists: " + memUser);
                    return 0;
                }

                System.out.print("Enter new member's password: ");
                String memPass = input.nextLine();

                auth.addUser(memUser, memPass, "MEMBER", memEmail);
                System.out.println(" Member added: " + memUser);
                return 0;

            case 5:
                String libEmail;
                while (true) {
                    System.out.print("Enter new librarian's email (required): ");
                    libEmail = input.nextLine().trim();
                    if (libEmail.isEmpty()) {
                        System.out.println(" Email is required and cannot be empty.");
                        continue;
                    }
                    break;
                }

                System.out.print("Enter new librarian's username: ");
                String libUser = input.nextLine();
                if (auth.userExists(libUser)) {
                    System.out.println("User already exists: " + libUser);
                    return 0;
                }

                System.out.print("Enter new librarian's password: ");
                String libPass = input.nextLine();

                auth.addUser(libUser, libPass, "LIBRARIAN", libEmail);
                System.out.println("Librarian added: " + libUser);
                return 0;

            case 6:
                System.out.print("Enter username to remove: ");
                String rem = input.nextLine();

                if (rem.equalsIgnoreCase(user.getUsername())) {
                    System.out.println(" You cannot remove yourself.");
                    return 0;
                }

                if (!user.isAdmin()) {
                    System.out.println("Only administrators can unregister users.");
                    return 0;
                }

                if (service.hasActiveBorrowRecords(rem)) {
                    System.out.println(" Cannot unregister user with active loans.");
                    return 0;
                }

                if (service.getOutstandingFine(rem) > 0) {
                    System.out.println(" Cannot unregister user with unpaid fines.");
                    return 0;
                }

                if (auth.removeUserWithRestrictions(rem, service.getBorrowRecordService()))
                    System.out.println("Removed user: " + rem);
                else
                    System.out.println(" User not found or cannot be removed: " + rem);

                return 0;

            case 7:
                System.out.println("Users in system:");
                for (Roles r : auth.getUsers()) {
                    System.out.println(" " + r.getUsername() + " (" + r.getRoleName() + ")");
                }
                return 0;

            case 8:
                reminderService.sendReminders();
                System.out.println("Reminders sent.");
                return 0;

            case 9:
                if (auth.logout()) {
                    System.out.println("Logged out successfully.");
                    return 1;
                } else {
                    System.out.println(" No user is currently logged in.");
                    return 0;
                }

            case 10:
                System.out.println("Exiting...");
                return 2;

            case 11:
                System.out.print("Enter CD title: ");
                String cdTitle = input.nextLine();

                System.out.print("Enter artist: ");
                String artist = input.nextLine();

                System.out.print("Enter ISBN: ");
                String cdIsbn = input.nextLine();

                service.addMedia(new CD(cdTitle, artist, cdIsbn));
                System.out.println("CD added successfully!");
                return 0;

            case 12:
                System.out.print("Enter title/artist/ISBN to search CD: ");
                String key = input.nextLine();

                List<Media> foundCD = service.searchMedia(key);
                List<CD> foundCDs = new ArrayList<>();
                for (Media m : foundCD) if (m instanceof CD) foundCDs.add((CD)m);

                if (foundCDs.isEmpty()) System.out.println("No matching CDs found!");
                else {
                    System.out.println(" Found CDs:");
                    for (CD c : foundCDs) System.out.println(c);
                }
                return 0;

            case 13:
                System.out.println("All CDs:");
                for (Media m : service.getAllMedia()) {
                    if (m instanceof CD) System.out.println(m);
                }
                return 0;

            default:
                System.out.println("Invalid option. Try again.");
                return 0;
        }
    }
}