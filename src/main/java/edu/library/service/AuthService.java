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
    private final FineService fineService;
    private Roles currentUser;

    public AuthService() {
        this(resolveDefault("users.txt"));
    }

    public AuthService(String filePath) {
        this(filePath, new FineService());
    }

    public AuthService(FineService fineService) {
        this(resolveDefault("users.txt"), fineService == null ? new FineService() : fineService);
    }

    public AuthService(String filePath, FineService fineService) {
        this.filePath = filePath;
        this.fineService = fineService == null ? new FineService() : fineService;
        loadUsersFromFile();
    }

    public Roles login(String username, String password) {
        if (username == null || password == null) return null;
        for (Roles user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                currentUser = user;
                persistOutstandingFines(user);
                return user;
            }
        }

        return null;
    }

    public Roles addUser(String username, String password, String roleName) {
        return addUser(username, password, roleName, null);
    }

    // new overload that accepts email
    public Roles addUser(String username, String password, String roleName, String email) {
        // require username/password/role and a non-blank email
        if (username == null || password == null || roleName == null) return null;
        if (email == null) return null;
        if (email.trim().isEmpty()) return null;
        Roles newUser = new Roles(username, password, roleName, email.trim());
        users.add(newUser);
        saveUsersToFile();
        return newUser;
    }

    // remove user by username
    public boolean removeUser(String username) {
        if (username == null) return false;
        Roles toRemove = null;
        for (Roles r : users) {
            if (r.getUsername().equalsIgnoreCase(username)) {
                toRemove = r;
                break;
            }
        }
        if (toRemove != null) {
            users.remove(toRemove);
            saveUsersToFile();
            // if removed user was currently logged in, clear
            if (currentUser != null && currentUser.getUsername().equalsIgnoreCase(username)) {
                currentUser = null;
            }
            return true;
        }
        return false;
    }

    public boolean userExists(String username) {
        if (username == null) return false;
        for (Roles r : users) if (r.getUsername().equalsIgnoreCase(username)) return true;
        return false;
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

    private void persistOutstandingFines(Roles user) {
        if (fineService == null || user == null) {
            return;
        }
        fineService.storeBalanceOnLogin(user.getUsername());
        // Ensure all known balances are flushed so the fines.txt file always exists after login
        fineService.saveBalances();
    }

    private void loadUsersFromFile() {
        users.clear();
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    System.out.println("Warning: could not create users file: " + filePath);
                }
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
                    String roleName = parts[2].trim();
                    String email = "";
                    if (parts.length >= 4) {
                        email = parts[3].trim();
                    }
                    users.add(new Roles(username, password, roleName, email));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        }
    }

    private void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Roles user : users) {
                // write username,password,role,email (email may be empty)
                writer.write(String.format("%s,%s,%s,%s%n", user.getUsername(), user.getPassword(), user.getRoleName(), user.getEmail() == null ? "" : user.getEmail()));
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private static String resolveDefault(String filename) {
        String base = System.getProperty("user.dir", "");
        return new File(base, filename).getPath();
    }
}
