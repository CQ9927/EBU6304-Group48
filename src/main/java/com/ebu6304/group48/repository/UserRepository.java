package com.ebu6304.group48.repository;

import com.ebu6304.group48.model.User;
import com.ebu6304.group48.util.PasswordHash;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    /**
     * Ensures {@code users.json} exists. Seeds from bundled {@code data/users.json} when empty.
     * When the file already has users, syncs bundled demo accounts by {@code username} (case-insensitive):
     * missing usernames are appended; existing demo rows are updated from the bundle (fixes wrong password hashes).
     */
    public void ensureStorage() throws IOException {
        synchronized (FILE_LOCK) {
            Files.createDirectories(usersFile.getParent());
            List<User> bundled = loadBundledDemoUsers();
            if (!Files.exists(usersFile) || isEmptyOrBlankArray(usersFile)) {
                Files.writeString(usersFile, GSON.toJson(bundled), StandardCharsets.UTF_8);
                return;
            }
            String json = Files.readString(usersFile, StandardCharsets.UTF_8);
            List<User> existing = GSON.fromJson(json, LIST_TYPE);
            if (existing == null) {
                existing = new ArrayList<>();
            }
            if (syncBundledDemoAccounts(existing, bundled)) {
                Files.writeString(usersFile, GSON.toJson(existing), StandardCharsets.UTF_8);
            }
        }
    }

    private static boolean isEmptyOrBlankArray(Path file) throws IOException {
        String s = Files.readString(file, StandardCharsets.UTF_8).trim();
        return s.isEmpty() || "[]".equals(s);
    }

    /** Demo accounts from classpath {@code data/users.json} (same file as repo {@code data/users.json}). */
    private static List<User> loadBundledDemoUsers() {
        ClassLoader cl = UserRepository.class.getClassLoader();
        try (InputStream in = cl != null ? cl.getResourceAsStream("data/users.json") : null) {
            if (in == null) {
                return fallbackBundledDemoUsers();
            }
            String s = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            List<User> list = GSON.fromJson(s, LIST_TYPE);
            if (list == null || list.isEmpty()) {
                return fallbackBundledDemoUsers();
            }
            return new ArrayList<>(list);
        } catch (IOException e) {
            return fallbackBundledDemoUsers();
        }
    }

    /** Last resort if {@code data/users.json} is not on the classpath (should not happen in packaged WAR). */
    private static List<User> fallbackBundledDemoUsers() {
        List<User> list = new ArrayList<>();
        list.add(seed("U-DEMO-TA", "ta_demo", "TA", "2d34b116983a0624d54b569bef06385437e76ad5c35081252278961101a15f50", "2026-03-01T00:00:00Z"));
        list.add(seed("U-DEMO-TA2", "ta_li", "TA", "f40c0dd739c8f2859ff71f5a083d564594270af77873dfe000ff8448718160ec", "2026-03-15T10:00:00Z"));
        list.add(seed("U-DEMO-MO", "mo_demo", "MO", "0cb41bae797d8065c90cb2bde250ee34e7619773be553bcd9a5dc2a660dd2977", "2026-03-01T00:00:00Z"));
        list.add(seed("U-DEMO-ADMIN", "admin_demo", "ADMIN", "b9864ab7ebd8da8f17b5551edaab0a72bc5a185be3cdb4a4a6a2fbb503a1676a", "2026-03-01T00:00:00Z"));
        return list;
    }

    /**
     * For each bundled demo user: update an existing row with the same username, or append if absent.
     * Keeps non-bundled registrations untouched.
     */
    private static boolean syncBundledDemoAccounts(List<User> existing, List<User> bundled) {
        boolean changed = false;
        for (User d : bundled) {
            if (d.getUsername() == null || d.getUsername().isBlank()) {
                continue;
            }
            String un = d.getUsername().trim();
            int index = -1;
            for (int i = 0; i < existing.size(); i++) {
                User e = existing.get(i);
                if (e.getUsername() != null && un.equalsIgnoreCase(e.getUsername().trim())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                if (!bundledAccountMatches(existing.get(index), d)) {
                    existing.set(index, copyOf(d));
                    changed = true;
                }
            } else {
                existing.add(copyOf(d));
                changed = true;
            }
        }
        return changed;
    }

    private static boolean bundledAccountMatches(User runtime, User bundle) {
        return Objects.equals(runtime.getUserId(), bundle.getUserId())
                && Objects.equals(runtime.getPasswordHash(), bundle.getPasswordHash())
                && Objects.equals(runtime.getRole(), bundle.getRole())
                && runtime.getUsername() != null
                && bundle.getUsername() != null
                && runtime.getUsername().trim().equalsIgnoreCase(bundle.getUsername().trim());
    }

    private static User copyOf(User from) {
        User u = new User();
        u.setUserId(from.getUserId());
        u.setUsername(from.getUsername());
        u.setPasswordHash(from.getPasswordHash());
        u.setRole(from.getRole());
        u.setCreatedAt(from.getCreatedAt());
        return u;
    }

    /** Password for bundled accounts: {@code demo123} (see README). */
    private static User seed(String userId, String username, String role, String passwordHash, String createdAtIso) {
        User u = new User();
        u.setUserId(userId);
        u.setUsername(username);
        u.setPasswordHash(passwordHash);
        u.setRole(role);
        u.setCreatedAt(createdAtIso);
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
