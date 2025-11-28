package edu.library.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class FineService {
    private final Path filePath;
    private final Map<String, Integer> balances = new HashMap<>();

    public FineService() {
        this(resolveDefault("fines.txt"));
    }

    public FineService(String filePath) {
        this(Path.of(filePath));
    }

    public FineService(Path filePath) {
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
    public Map<String, Integer> getAllBalances() {
        return new HashMap<>(balances);
    }

    private void load() {
        balances.clear();
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error ensuring fines file exists", e);
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
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
            throw new RuntimeException("Error reading fines file", e);
        }
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE)) {
            for (Map.Entry<String, Integer> entry : balances.entrySet()) {
                writer.write(String.format("%s,%d%n", entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving fines", e);
        }
    }

    private static String resolveDefault(String filename) {
        String base = System.getProperty("user.dir", "");
        return Path.of(base).resolve(filename).toString();
    }
}
