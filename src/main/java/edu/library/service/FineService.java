package edu.library.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FineService {
    private final String filePath;
    private final Map<String, Integer> balances = new HashMap<>();

    public FineService() {
        this("fines.txt");
    }

    public FineService(String filePath) {
        this.filePath = filePath;
        load();
    }

    public int getBalance(String username) {
        if (username == null) return 0;
        return balances.getOrDefault(username, 0);
    }

    public void addFine(String username, int amount) {
        if (username == null || amount <= 0) return;
        balances.put(username, getBalance(username) + amount);
        save();
    }

    public int payFine(String username, int amount) {
        if (username == null || amount <= 0) return getBalance(username);
        int current = getBalance(username);
        int updated = Math.max(0, current - amount);
        balances.put(username, updated);
        save();
        return updated;
    }

    private void load() {
        balances.clear();
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating fines file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String username = parts[0];
                    int balance = Integer.parseInt(parts[1]);
                    balances.put(username, balance);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading fines file: " + e.getMessage());
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, Integer> entry : balances.entrySet()) {
                writer.write(String.format("%s,%d%n", entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            System.out.println("Error saving fines: " + e.getMessage());
        }
    }
}