package edu.library.service;

import edu.library.domain.model.Roles;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AuthService {

    private final String filePath;
    private final List<Roles> users = new ArrayList<>();
    private final FineService fineService;
    private Roles currentUser;

    public AuthService() {
        this(resolveDefault("users.txt"), new FineService());
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
            if (user.getUsername().equals(username) &&
                    user.getPassword().equals(password)) {

                currentUser = user;
                persistOutstandingFines(user);
                return user;
            }
        }
        return null;
    }



    public Roles addUser(String username, String password, String roleName, String email) {
        if (username == null || password == null || roleName == null || email == null)
            return null;

        if (email.trim().isEmpty()) return null;

        Roles newUser = new Roles(username, password, roleName, email.trim());
        users.add(newUser);
        saveUsersToFile();
        return newUser;
    }

    public Roles addUser(String username, String password, String roleName) {
        return addUser(username, password, roleName, "");
    }



    public boolean removeUser(String username) {
        if (username == null) return false;

        Roles toRemove = users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);

        if (toRemove == null) return false;

        users.remove(toRemove);
        saveUsersToFile();

        if (currentUser != null && currentUser.getUsername().equalsIgnoreCase(username)) {
            currentUser = null;
        }

        return true;
    }



    public boolean removeUserWithRestrictions(String username, BorrowRecordService borrowRecordService) {

        if (currentUser == null || !currentUser.isAdmin()) return false;

        if (username == null || username.equalsIgnoreCase(currentUser.getUsername()))
            return false;

        if (fineService.getBalance(username) > 0)
            return false;

        if (borrowRecordService != null &&
                !borrowRecordService.getActiveBorrowRecordsForUser(username).isEmpty())
            return false;

        return removeUser(username);
    }



    public boolean userExists(String username) {
        if (username == null) return false;

        return users.stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    public boolean logout() {
        if (currentUser == null) return false;
        currentUser = null;
        return true;
    }

    public Roles getCurrentUser() {
        return currentUser;
    }

    public Roles getCurrentAdmin() {
        return currentUser;
    }

    public List<Roles> getUsers() {
        return new ArrayList<>(users);
    }



    private void persistOutstandingFines(Roles user) {
        if (fineService == null) return;
        fineService.storeBalanceOnLogin(user.getUsername());
        fineService.saveBalances();
    }

    private void loadUsersFromFile() {
        users.clear();
        File file = new File(filePath);

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating users file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader r = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length < 3) continue;

                String username = p[0].trim();
                String password = p[1].trim();
                String roleName = p[2].trim();
                String email = p.length > 3 ? p[3].trim() : "";

                users.add(new Roles(username, password, roleName, email));
            }

        } catch (IOException e) {
            System.out.println("Error reading users: " + e.getMessage());
        }
    }


    void saveUsersToFile() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(filePath))) {
            for (Roles u : users) {
                w.write(String.format("%s,%s,%s,%s%n",
                        u.getUsername(),
                        u.getPassword(),
                        u.getRoleName(),
                        u.getEmail() == null ? "" : u.getEmail()));
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private static String resolveDefault(String filename) {
        return new File(System.getProperty("user.dir", ""), filename).getPath();
    }
}