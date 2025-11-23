package edu.library;

import edu.library.model.Book;
import edu.library.service.BookService;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        BookService service = new BookService();
        Scanner input = new Scanner(System.in);

        service.loadBooksFromFile();

        while (true) {
            System.out.println("\n===== Library Menu =====");
            System.out.println("1. Add Book");
            System.out.println("2. Search Book");
            System.out.println("3. Display All Books");
            System.out.println("4. Borrow Book");
            System.out.println("5. Return Book");
            System.out.println("6. Exit");
            System.out.print("Choose option: ");
            int choice = input.nextInt();
            input.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter title: ");
                    String title = input.nextLine();
                    System.out.print("Enter author: ");
                    String author = input.nextLine();
                    System.out.print("Enter ISBN: ");
                    String isbn = input.nextLine();
                    Book newBook = new Book(title, author, isbn);
                    service.addBook(newBook);
                    break;

                case 2:
                    System.out.print("Enter title/author/ISBN to search: ");
                    String keyword = input.nextLine();
                    List<Book> foundBooks = service.searchBook(keyword);
                    if (foundBooks.isEmpty()) {
                        System.out.println(" No matching books found!");
                    } else {
                        System.out.println("Found books:");
                        for (Book b : foundBooks) {
                            System.out.println(b);
                        }
                    }
                    break;

                case 3:
                    service.displayBooks();
                    break;

                case 4:
                    System.out.print("Enter ISBN to borrow: ");
                    String isbnBorrow = input.nextLine();
                    Book bookToBorrow = service.searchBook(isbnBorrow).stream().findFirst().orElse(null);
                    if (bookToBorrow != null) {
                        if (service.borrowBook(bookToBorrow, 28)) {
                            System.out.println("Book borrowed successfully, Due in 28 days");
                        } else {
                            System.out.println("Cannot borrow this book (maybe already borrowed).");
                        }
                    } else {
                        System.out.println("Book not found!");
                    }
                    break;

                case 5:
                    System.out.print("Enter ISBN to return: ");
                    String isbnReturn = input.nextLine();
                    Book bookToReturn = service.searchBook(isbnReturn).stream().findFirst().orElse(null);
                    if (bookToReturn != null) {
                        if (service.returnBook(bookToReturn)) {
                            System.out.println("Book returned successfully");
                            if (bookToReturn.isOverdue()) {
                                int fine = service.calculateFine(bookToReturn);
                                System.out.println("Overdue fine: " + fine + " NIS");
                            }
                        } else {
                            System.out.println("This book is not borrowed");
                        }
                    } else {
                        System.out.println("Book not found");
                    }
                    break;

                case 6:
                    System.out.println("Exiting");
                    return;

                default:
                    System.out.println("Invalid option please Try again.");
            }
        }
    }
}
