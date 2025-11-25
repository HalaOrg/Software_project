package edu.library.service;

import edu.library.model.Roles;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private final String filePath;
    private final List<Roles> users = new ArrayList<>();
    private Roles currentUser;

    public AuthService() {
        this("users.txt");
    }

    public AuthService(String filePath) {
        this.filePath = filePath;
        loadUsersFromFile();
    }

    public Roles login(String username, String password) {
        if (username == null || password == null) return null;
        for (Roles user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                currentUser = user;
                return user;
            }
        }

        return null;
    }
    public Roles addUser(String username, String password, String roleName) {
        return register(username, password, "", roleName);
    }

    public Roles register(String username, String password, String email, String roleName) {
        if (username == null || password == null || roleName == null) return null;
        boolean exists = users.stream().anyMatch(u -> u.getUsername().equals(username));
        if (exists) {
            return null;
        }
        Roles newUser = new Roles(username, password, email, roleName);
        users.add(newUser);
        saveUsersToFile();
        return newUser;
    }

    public boolean logout() {
        if (currentUser == null) return false;
        currentUser = null;
        return true;
    }


    public Roles getCurrentAdmin() {
        return currentUser;
    }

    public Roles getCurrentUser() {
        return currentUser;
    }

    public List<Roles> getUsers() {
        return new ArrayList<>(users);
    }

    private void loadUsersFromFile() {
        users.clear();
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating users file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String roleName;
                    String email;
                    if (parts.length >= 4) {
                        email = parts[2].trim();
                        roleName = parts[3].trim();
                    } else {
                        email = "";
                        roleName = parts[2].trim();
                    }
                    users.add(new Roles(username, password, email, roleName));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        }
    }

    private void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Roles user : users) {
                writer.write(String.format("%s,%s,%s,%s%n", user.getUsername(), user.getPassword(), user.getEmail(), user.getRoleName()));
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
}
