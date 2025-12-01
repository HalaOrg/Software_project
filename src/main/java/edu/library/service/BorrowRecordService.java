package edu.library.service;

import edu.library.model.BorrowRecord;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordService {

    private final String filePath;
    private final List<BorrowRecord> records = new ArrayList<>();

    public BorrowRecordService() {
        this(resolveDefault("borrow_records.txt"));
    }

    public BorrowRecordService(String filePath) {
        this.filePath = filePath;
        loadRecords();
    }

    // -------------------------------------------------
    //      Existing methods
    // -------------------------------------------------
    public List<BorrowRecord> getRecords() {
        return new ArrayList<>(records);
    }

    public BorrowRecord findActiveBorrowRecord(String username, String isbn) {
        for (BorrowRecord record : records) {
            if (!record.isReturned()
                    && record.getUsername().equals(username)
                    && record.getIsbn().equals(isbn)) {
                return record;
            }
        }
        return null;
    }

    public List<BorrowRecord> getActiveBorrowRecordsForUser(String username) {
        List<BorrowRecord> result = new ArrayList<>();
        for (BorrowRecord record : records) {
            if (!record.isReturned() && record.getUsername().equals(username)) {
                result.add(record);
            }
        }
        return result;
    }

    public void recordBorrow(String username, String isbn, LocalDate dueDate) {
        BorrowRecord record = new BorrowRecord(username, isbn, dueDate, false, null);
        records.add(record);
        appendRecord(record);
    }

    public void recordReturn(String username, String isbn, LocalDate returnDate) {
        for (BorrowRecord record : records) {
            if (!record.isReturned()
                    && record.getUsername().equals(username)
                    && record.getIsbn().equals(isbn)) {

                record.markReturned(returnDate);
                saveAll();
                return;
            }
        }

        // fallback case
        BorrowRecord fallback = new BorrowRecord(username, isbn, returnDate, true, returnDate);
        records.add(fallback);
        saveAll();
    }

    // -------------------------------------------------
    //      Load & Save
    // -------------------------------------------------
    private void loadRecords() {
        records.clear();
        File file = new File(filePath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating borrow record file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");

                if (parts.length >= 5) {
                    String username = parts[0];
                    String isbn = parts[1];
                    LocalDate dueDate = parts[2].equals("null") ? null : LocalDate.parse(parts[2]);
                    boolean returned = Boolean.parseBoolean(parts[3]);
                    LocalDate returnDate = parts[4].equals("null") ? null : LocalDate.parse(parts[4]);

                    records.add(new BorrowRecord(username, isbn, dueDate, returned, returnDate));
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading borrow records: " + e.getMessage());
        }
    }

    private void appendRecord(BorrowRecord record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(String.format("%s,%s,%s,%s,%s%n",
                    record.getUsername(),
                    record.getIsbn(),
                    record.getDueDate() == null ? "null" : record.getDueDate(),
                    record.isReturned(),
                    record.getReturnDate() == null ? "null" : record.getReturnDate()));
        } catch (IOException e) {
            System.out.println("Error writing borrow record: " + e.getMessage());
        }
    }

    private void saveAll() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (BorrowRecord record : records) {
                writer.write(String.format("%s,%s,%s,%s,%s%n",
                        record.getUsername(),
                        record.getIsbn(),
                        record.getDueDate() == null ? "null" : record.getDueDate(),
                        record.isReturned(),
                        record.getReturnDate() == null ? "null" : record.getReturnDate()));
            }
        } catch (IOException e) {
            System.out.println("Error saving borrow records: " + e.getMessage());
        }
    }

    private static String resolveDefault(String filename) {
        String base = System.getProperty("user.dir", "");
        return new File(base, filename).getPath();
    }

    // -----------------------------------------------------------
    //      Helper Methods
    // -----------------------------------------------------------
    public boolean hasActiveBorrows(String username) {
        for (BorrowRecord record : records) {
            if (!record.isReturned() && record.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    // ✅ New method to fix Librarian.java error
    public List<BorrowRecord> getAllRecords() {
        return new ArrayList<>(records);
    }
}
