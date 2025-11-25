package edu.library;

import edu.library.model.Book;
import edu.library.service.BookService;
import edu.library.model.Roles;
import edu.library.service.AuthService;
import edu.library.service.Admin;
import edu.library.service.Member;
import edu.library.service.Librarian;
import java.util.List;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        BookService service = new BookService();
        service.loadBooksFromFile();
        AuthService auth = new AuthService();
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
                System.out.print("Enter email: ");
                String email = input.nextLine().trim();
                System.out.print("Enter username: ");
                String username = input.nextLine().trim();
                System.out.print("Enter password: ");
                String password = input.nextLine().trim();
                System.out.print("Enter role (ADMIN/MEMBER/LIBRARIAN): ");
                String role = input.nextLine().trim().toUpperCase();
                if (!role.equals("ADMIN") && !role.equals("MEMBER") && !role.equals("LIBRARIAN")) {
                    System.out.println("Invalid role. Defaulting to MEMBER.");
                    role = "MEMBER";
                }
                // check if username exists
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
                    System.out.println("Registration successful. You can now login.");
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
                    System.out.println("❌ Invalid credentials.");
                    continue;
                }

                System.out.println("✅ Logged in as: " + user.getUsername() + " (" + user.getRoleName() + ")");

                // role-based menu loop
                boolean sessionActive = true;
                while (sessionActive) {
                    int result = 0;
                    if (user.isAdmin()) {
                        result = Admin.handle(input, service, auth, user);
                    } else if ("MEMBER".equalsIgnoreCase(user.getRoleName())) {
                        result = Member.handle(input, service, auth, user);
                    } else {
                        result = Librarian.handle(input, service, auth, user);
                    }

                    if (result == 1) { // logout
                        sessionActive = false;
                    } else if (result == 2) { // exit app
                        System.out.println("👋 Exiting...");
                        return;
                    }
                }
            }

            if (choice == 3) {
                System.out.println("👋 Exiting...");
                return;
            }
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