
package edu.library;
import edu.library.fine.FineCalculator;
import edu.library.model.Book;
import edu.library.service.MediaService;
import edu.library.model.Roles;
import edu.library.service.AuthService;
import edu.library.service.BorrowRecordService;
import edu.library.service.Admin;
import edu.library.service.FineService;
import edu.library.service.Member;
import edu.library.service.Librarian;
import edu.library.service.ReminderService;
import edu.library.notification.EmailNotifier;
import edu.library.notification.SmtpEmailServer;
import edu.library.time.SystemTimeProvider;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        FineService fineService = new FineService();
        BorrowRecordService borrowRecordService = new BorrowRecordService();

        MediaService service = new MediaService(
                "media.txt",
                borrowRecordService,
                fineService,
                new SystemTimeProvider(),
                new FineCalculator()
        );

        service.updateFinesOnStartup();

        AuthService auth = new AuthService(fineService);;
        ReminderService reminderService = new ReminderService(borrowRecordService, auth, new edu.library.time.SystemTimeProvider());
        reminderService.addObserver(new EmailNotifier(new SmtpEmailServer()));
        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Welcome to the Library System ===");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");
            String line = input.nextLine();
            int choice;
            try {
                choice = Integer.parseInt(line.trim());
            } catch (Exception e) {
                System.out.println("Invalid input");
                continue;
            }

            if (choice == 1) {
                String email;
                while (true) {
                    System.out.print("Enter email (required): ");
                    email = input.nextLine().trim();
                    if (email.isEmpty()) {
                        System.out.println("Email is required and cannot be empty.");
                        continue;
                    }
                    break;
                }

                System.out.print("Enter username: ");
                String username = input.nextLine().trim();
                System.out.print("Enter password: ");
                String password = input.nextLine().trim();
                String role = "MEMBER";

                boolean exists = false;
                for (Roles r : auth.getUsers()) {
                    if (r.getUsername().equalsIgnoreCase(username)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    System.out.println("Username already exists. Choose another.");
                } else {
                    auth.addUser(username, password, role, email);
                    System.out.println("Registration successful. You can now login (role: MEMBER).");
                }
                continue;
            }

            if (choice == 2) {
                System.out.print("Enter username: ");
                String username = input.nextLine();
                System.out.print("Enter password: ");
                String password = input.nextLine();
                Roles user = auth.login(username, password);
                if (user == null) {
                    System.out.println("Invalid credentials.");
                    continue;
                }

                System.out.println("Logged in as: " + user.getUsername() + " (" + user.getRoleName() + ")");
                reminderService.sendReminderForUser(user);

                boolean sessionActive = true;
                while (sessionActive) {
                    int result;
                    if (user.isAdmin()) {
                        result = Admin.handle(input, service, auth, reminderService, user);
                    }
                    else if (user.isMember()) {
                        result = Member.handle(input, service, auth, user);
                    } else if (user.isLibrarian()) {
                        result = Librarian.handle(input, service, auth, user);
                    } else {
                        result = Member.handle(input, service, auth, user);
                    }

                    if (result == 1) {
                        sessionActive = false;
                    } else if (result == 2) {
                        System.out.println(" Exiting...");
                        return;
                    }
                }
            }

            if (choice == 3) {
                System.out.println("Exiting...");
                return;
            }
        }
    }

    private static Book findBookByIsbn(MediaService service, String isbn) {
        if (isbn == null) return null;
        for (Book b : service.getBooks()) {
            if (isbn.equalsIgnoreCase(b.getIsbn())) return b;
        }
        return null;
    }


}
