package edu.library.service;

import edu.library.domain.model.Book;
import edu.library.domain.model.BorrowRecord;
import edu.library.domain.model.CD;
import edu.library.domain.model.Media;
import edu.library.domain.fine.FineCalculator;
import edu.library.domain.time.TimeProvider;
import edu.library.domain.time.SystemTimeProvider;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class MediaService {

    private final String filePath;
    private final BorrowRecordService borrowRecordService;
    private final FineService fineService;
    private final TimeProvider timeProvider;
    private final FineCalculator fineCalculator;

    private List<Media> items = new ArrayList<>();


    public MediaService() {
        this("media.txt",
                new BorrowRecordService(),
                new FineService(),
                new SystemTimeProvider(),
                new FineCalculator());
    }

    public MediaService(String filePath,
                        BorrowRecordService borrowRecordService,
                        FineService fineService,
                        TimeProvider timeProvider,
                        FineCalculator fineCalculator) {

        this.filePath = filePath;
        this.borrowRecordService = borrowRecordService;
        this.fineService = fineService;
        this.timeProvider = timeProvider;
        this.fineCalculator = fineCalculator;

        loadMediaFromFile(filePath);
    }

    public MediaService(String filePath,
                        BorrowRecordService borrowRecordService,
                        FineService fineService) {
        this(filePath, borrowRecordService, fineService, new SystemTimeProvider(), new FineCalculator());
    }
    private void loadMediaFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) return;

        items.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {

                line = line.trim();
                // تجاهل الأسطر الفاضية
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(";", -1);
                if (parts.length != 7) {
                    // سطر مش على الشكل الصحيح → تجاهله
                    continue;
                }

                String type   = parts[0];
                String title  = parts[1];
                String author = parts[2];
                String isbn   = parts[3];

                int total;
                int available;
                try {
                    total     = Integer.parseInt(parts[4].trim());
                    available = Integer.parseInt(parts[5].trim());
                } catch (NumberFormatException e) {
                    // لو الأرقام خربانة ما نطيّر البرنامج كله
                    continue;
                }

                LocalDate dueDate = null;
                String dueRaw = parts[6].trim();
                if (!dueRaw.equalsIgnoreCase("null") && !dueRaw.isBlank()) {
                    dueDate = LocalDate.parse(dueRaw);
                }

                Media m = null;

                if (type.equalsIgnoreCase("BOOK")) {
                    // نستخدم الكونستركتور اللي ياخذ total فقط
                    Book b = new Book(title, author, isbn, total);
                    b.setAvailableCopies(available);
                    b.setDueDate(dueDate);
                    m = b;
                } else if (type.equalsIgnoreCase("CD")) {
                    CD cd = new CD(title, author, isbn, total);
                    cd.setAvailableCopies(available);
                    cd.setDueDate(dueDate);
                    m = cd;
                }

                if (m != null) {
                    items.add(m);
                }
            }

        } catch (IOException e) {
            System.out.println("Error loading media: " + e.getMessage());
        }
    }



    public void saveAllMediaToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Media m : items) {
                writer.write(String.format("%s;%s;%s;%s;%d;%d;%s%n",
                        (m instanceof Book) ? "BOOK" : "CD",
                        m.getTitle(),
                        m.getAuthor(),
                        m.getIsbn(),
                        m.getTotalCopies(),
                        m.getAvailableCopies(),
                        (m.getDueDate() != null ? m.getDueDate() : "null")
                ));
            }
        } catch (IOException e) {
            System.out.println("Error saving media: " + e.getMessage());
        }
    }


    public void addMedia(Media media) {
        items.add(media);
        saveAllMediaToFile();
        System.out.println("Added: " + media.getTitle());
    }

    public boolean deleteMedia(String isbn) {
        Media m = findByIsbn(isbn);
        if (m == null) return false;

        items.remove(m);
        saveAllMediaToFile();
        return true;
    }

    public boolean updateMediaQuantity(String isbn, int newQty) {
        Media m = findByIsbn(isbn);
        if (m == null || newQty < 0) return false;

        m.setTotalCopies(newQty);
        if (m.getAvailableCopies() > newQty) m.setAvailableCopies(newQty);

        saveAllMediaToFile();
        return true;
    }

    public List<Media> searchMedia(String keyword) {
        List<Media> result = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) return result;

        String k = keyword.toLowerCase();
        for (Media m : items) {
            if (m.getTitle().toLowerCase().contains(k) ||
                    m.getAuthor().toLowerCase().contains(k) ||
                    m.getIsbn().toLowerCase().contains(k)) {
                result.add(m);
            }
        }
        return result;
    }


    public boolean borrow(Media m, String username) {
        if (m == null) {
            System.out.println("Item not available.");
            return false;
        }


        if (m.getAvailableCopies() <= 0) {
            m.setAvailable(false);
            System.out.println("Item not available.");
            return false;
        }


        if (fineService.getBalance(username) > 0) {
            System.out.println("Pay fines before borrowing.");
            return false;
        }


        // Prevent borrowing when the user already has an overdue active record.
        LocalDate today = timeProvider.today();
        for (BorrowRecord record : borrowRecordService.getActiveBorrowRecordsForUser(username)) {
            if (record.getDueDate() != null && today != null && today.isAfter(record.getDueDate())) {
                System.out.println("Cannot borrow with overdue items.");
                return false;
            }
        }


        LocalDate dueDate = timeProvider.today().plusDays(m.getBorrowDurationDays());


        m.borrowOne();



        m.setDueDate(dueDate);

        borrowRecordService.recordBorrow(username, m.getIsbn(), dueDate);

        saveAllMediaToFile();
        return true;
    }


    public boolean returnMedia(Media m, String username) {
        BorrowRecord active = borrowRecordService.findActiveBorrowRecord(username, m.getIsbn());
        if (active == null) return false;

        LocalDate originalDue = active.getDueDate();
        LocalDate returnDate = timeProvider.today();

        m.returnOne();
        if (m.getAvailableCopies() == m.getTotalCopies()) m.setDueDate(null);

        saveAllMediaToFile();
        borrowRecordService.recordReturn(username, m.getIsbn(), returnDate);

        if (returnDate.isAfter(originalDue)) {
            int overdueDays = (int) ChronoUnit.DAYS.between(originalDue, returnDate);
            int ratePerDay = (m instanceof Book) ? 10 : (m instanceof CD) ? 20 : 0;
            int fineAmount = overdueDays * ratePerDay;
            int currentBalance = fineService.getBalance(username);

            if (fineAmount > currentBalance) {
                int diff = fineAmount - currentBalance;
                fineService.addFine(username, diff);
            }
        }

        return true;
    }

    // -----------------------------
    //          HELPERS
    // -----------------------------
    public Media findByIsbn(String isbn) {
        for (Media m : items) {
            if (m.getIsbn().equalsIgnoreCase(isbn)) return m;
        }
        return null;
    }

    public void payFine(String username, int amount) {
        if (username == null || amount <= 0) return;
        fineService.payFine(username, amount);
    }

    public List<Media> getItems() {
        return items;
    }

    public BorrowRecordService getBorrowRecordService() {
        return borrowRecordService;
    }

    public int getOutstandingFine(String username) {
        return fineService.getBalance(username);
    }

    public boolean hasActiveBorrowRecords(String username) {
        return borrowRecordService.hasActiveBorrows(username);
    }

    // -----------------------------
    //    Admin & Librarian
    // -----------------------------
    public List<Media> getAllMedia() {
        return new ArrayList<>(items);
    }

    public Map<String, Integer> getAllFines() {
        return fineService.getAllBalances(); // ✅ استخدام FineService مباشرة
    }

    public List<Book> getBooks() {
        List<Book> books = new ArrayList<>();
        for (Media m : items) if (m instanceof Book) books.add((Book) m);
        return books;
    }

    public List<CD> getCDs() {
        List<CD> cds = new ArrayList<>();
        for (Media m : items) if (m instanceof CD) cds.add((CD) m);
        return cds;
    }

    public List<Book> searchBook(String keyword) {
        List<Book> result = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) return result;

        String k = keyword.toLowerCase();
        for (Media m : items) {
            if (m instanceof Book b) {
                if (b.getTitle().toLowerCase().contains(k) ||
                        b.getAuthor().toLowerCase().contains(k) ||
                        b.getIsbn().toLowerCase().contains(k)) result.add(b);
            }
        }
        return result;
    }

    public Book findBookByIsbn(String isbn) {
        for (Media m : items) {
            if (m instanceof Book b && b.getIsbn().equalsIgnoreCase(isbn)) return b;
        }
        return null;
    }

    public boolean borrowBook(Book book, String username) {
        return borrow(book, username);
    }

    public boolean returnBook(Book book, String username) {
        return returnMedia(book, username);
    }

    public List<BorrowRecord> getActiveBorrowRecordsForUser(String username) {
        List<BorrowRecord> active = new ArrayList<>();
        for (BorrowRecord r : borrowRecordService.getRecords()) {
            if (r.getUsername().equalsIgnoreCase(username) && !r.isReturned()) active.add(r);
        }
        return active;
    }

    public List<CD> searchCD(String keyword) {
        List<CD> result = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) return result;

        String k = keyword.toLowerCase();
        for (Media m : items) {
            if (m instanceof CD cd) {
                if (cd.getTitle().toLowerCase().contains(k) ||
                        cd.getAuthor().toLowerCase().contains(k) ||
                        cd.getIsbn().toLowerCase().contains(k)) {
                    result.add(cd);
                }
            }
        }
        return result;
    }

    public CD findCDByIsbn(String isbn) {
        for (Media m : items) {
            if (m instanceof CD cd && cd.getIsbn().equalsIgnoreCase(isbn)) return cd;
        }
        return null;
    }

    public boolean borrowCD(CD cd, String username) {
        return borrow(cd, username);
    }

    public boolean returnCD(CD cd, String username) {
        return returnMedia(cd, username);
    }

    public void displayMedia() {
        if (items.isEmpty()) {
            System.out.println("No media available.");
            return;
        }
        for (Media m : items) {
            System.out.printf("%s | %s | ISBN: %s | Available: %d/%d | Due: %s%n",
                    (m instanceof Book) ? "[Book]" : "[CD]",
                    m.getTitle(),
                    m.getIsbn(),
                    m.getAvailableCopies(),
                    m.getTotalCopies(),
                    (m.getDueDate() != null ? m.getDueDate() : "None")
            );
        }
    }
    public void updateFinesOnStartup() {

        LocalDate today = timeProvider.today();

        Map<String, Integer> recalculatedFines = new HashMap<>();

        for (BorrowRecord record : borrowRecordService.getRecords()) {

            if (record.isReturned()
                    || record.getDueDate() == null
                    || !today.isAfter(record.getDueDate())) {
                continue;
            }

            Media media = findByIsbn(record.getIsbn());
            if (media == null) {
                continue;
            }

            int overdueDays = (int) ChronoUnit.DAYS.between(
                    record.getDueDate(),
                    today
            );

            int ratePerDay;
            if (media instanceof Book) {
                ratePerDay = 10;   // book rate (10/day)
            } else if (media instanceof CD) {
                ratePerDay = 20;   // CD rate (20/day)
            } else {
                continue;
            }

            int newFineAmount = overdueDays * ratePerDay;

            String username = record.getUsername();
            recalculatedFines.merge(username, newFineAmount, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : recalculatedFines.entrySet()) {
            String username = entry.getKey();
            int recalculatedTotal = entry.getValue();
            int currentBalance = fineService.getBalance(username);

            if (recalculatedTotal > currentBalance) {
                int diff = recalculatedTotal - currentBalance;
                fineService.addFine(username, diff);
            }
        }
    }


    public String getFilePath() {
        return filePath;
    }
    public FineService getFineService() {
        return fineService;
    }

    public TimeProvider getSystemTimeProvider() {
        return timeProvider;
    }

    public FineCalculator getFineCalculator() {
        return fineCalculator;
    }


    public String getMediaFilePath() {
        return filePath;
    }

    public boolean isMediaActive(String isbn) {
        Media m = findByIsbn(isbn); // تبحث عن الكتاب/الـCD بالـISBN
        if (m == null) return false; // إذا مش موجود → false
        return true;                 // إذا موجود → true
    }

    public void resetDueDateIfAllAvailable(Media m) {
        if (m == null) return;
        if (m.getAvailableCopies() == m.getTotalCopies()) {
            m.setDueDate(null);
        }
    }

    public void addBook(String title, String author, String isbn, int totalCopies) {
        if (title == null || title.isBlank() || isbn == null || isbn.isBlank()) return;

        Book newBook = new Book(title, author, isbn, totalCopies);
        newBook.setAvailableCopies(totalCopies); // كل النسخ متاحة أولاً
        items.add(newBook);

        saveAllMediaToFile(); // تحفظ التغييرات في الملف
    }


}