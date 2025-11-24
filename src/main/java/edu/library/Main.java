package edu.library;

import edu.library.model.Book;
import edu.library.service.BookService;
import java.util.List;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    //test comment
    public static void main(String[] args) {
        BookService service = new BookService();
        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== Library Menu =====");
            System.out.println("1. Add Book");
            System.out.println("2. Search Book");
            System.out.println("3. Display All Books");
            System.out.println("4. Exit");
            System.out.print("Choose option: ");
            int choice = input.nextInt();
            input.nextLine(); // fix for nextLine issue

            switch (choice) {
                case 1:
                    System.out.print("Enter title: ");
                    String title = input.nextLine();
                    System.out.print("Enter author: ");
                    String author = input.nextLine();
                    System.out.print("Enter ISBN: ");
                    String isbn = input.nextLine();
                    service.addBook(new Book(title, author, isbn));
                    break;

                case 2:
                    System.out.print("Enter title/author/ISBN to search: ");
                    String keyword = input.nextLine();
                    List<Book> foundBooks = service.searchBook(keyword);
                    if (foundBooks.isEmpty()) {
                        System.out.println("❌ No matching books found!");
                    } else {
                        System.out.println("✅ Found books:");
                        for (Book b : foundBooks) {
                            System.out.println(b);
                        }
                    }
                    break;

                case 3:
                    service.displayBooks();
                    break;

                case 4:
                    System.out.println("👋 Exiting...");
                    return;

                default:
                    System.out.println("❌ Invalid option. Try again.");
            }
        }
    }


}