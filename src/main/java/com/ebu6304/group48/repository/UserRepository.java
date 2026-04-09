package com.ebu6304.group48.repository;

import com.ebu6304.group48.model.User;
import com.ebu6304.group48.util.PasswordHash;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepository {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<User>>() { }.getType();
    private static final Object FILE_LOCK = new Object();

    private final Path usersFile;

    public UserRepository(String dataDirectory) {
        this.usersFile = Path.of(dataDirectory, "users.json");
    }

    public void ensureStorage() throws IOException {
        synchronized (FILE_LOCK) {
            Files.createDirectories(usersFile.getParent());
            if (!Files.exists(usersFile) || isEmptyOrBlankArray(usersFile)) {
                Files.writeString(usersFile, GSON.toJson(defaultSeedUsers()), StandardCharsets.UTF_8);
            }
        }
    }

    private static boolean isEmptyOrBlankArray(Path file) throws IOException {
        String s = Files.readString(file, StandardCharsets.UTF_8).trim();
        return s.isEmpty() || "[]".equals(s);
    }

    private static List<User> defaultSeedUsers() {
        List<User> list = new ArrayList<>();
        list.add(seed("U-DEMO-TA", "ta_demo", "TA", "2d34b116983a0624d54b569bef06385437e76ad5c35081252278961101a15f50"));
        list.add(seed("U-DEMO-MO", "mo_demo", "MO", "0cb41bae797d8065c90cb2bde250ee34e7619773be553bcd9a5dc2a660dd2977"));
        list.add(seed("U-DEMO-ADMIN", "admin_demo", "ADMIN", "b9864ab7ebd8da8f17b5551edaab0a72bc5a185be3cdb4a4a6a2fbb503a1676a"));
        return list;
    }

    /** Password for all three: {@code demo123} (documented in README). */
    private static User seed(String userId, String username, String role, String passwordHash) {
        User u = new User();
        u.setUserId(userId);
        u.setUsername(username);
        u.setPasswordHash(passwordHash);
        u.setRole(role);
        u.setCreatedAt(Instant.parse("2026-03-01T00:00:00Z").toString());
        return u;
    }

    public List<User> findAll() throws IOException {
        synchronized (FILE_LOCK) {
            ensureStorage();
            String json = Files.readString(usersFile, StandardCharsets.UTF_8);
            List<User> users = GSON.fromJson(json, LIST_TYPE);
            return users != null ? new ArrayList<>(users) : new ArrayList<>();
        }
    }

    public void saveAll(List<User> users) throws IOException {
        synchronized (FILE_LOCK) {
            Files.createDirectories(usersFile.getParent());
            Files.writeString(usersFile, GSON.toJson(users), StandardCharsets.UTF_8);
        }
    }

    public Optional<User> findByUsername(String username) throws IOException {
        if (username == null) {
            return Optional.empty();
        }
        String key = username.trim();
        for (User u : findAll()) {
            if (key.equalsIgnoreCase(u.getUsername())) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    public boolean usernameExists(String username) throws IOException {
        return findByUsername(username).isPresent();
    }

    public void register(String username, String plainPassword, String role) throws IOException {
        synchronized (FILE_LOCK) {
            ensureStorage();
            List<User> users = findAll();
            if (findByUsername(username).isPresent()) {
                throw new IllegalStateException("Username already taken");
            }
            User u = new User();
            u.setUserId("U-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
            u.setUsername(username.trim());
            u.setPasswordHash(PasswordHash.hash(u.getUsername(), plainPassword));
            u.setRole(role);
            u.setCreatedAt(Instant.now().toString());
            users.add(u);
            saveAll(users);
        }
    }

    public Optional<User> authenticate(String username, String plainPassword) throws IOException {
        Optional<User> u = findByUsername(username);
        if (u.isEmpty()) {
            return Optional.empty();
        }
        String expected = u.get().getPasswordHash();
        String actual = PasswordHash.hash(u.get().getUsername(), plainPassword);
        if (expected != null && expected.equalsIgnoreCase(actual)) {
            return u;
        }
        return Optional.empty();
    }
}
