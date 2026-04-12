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

    public void ensureStorage() throws IOException {
        synchronized (FILE_LOCK) {
            Files.createDirectories(usersFile.getParent());
            if (!Files.exists(usersFile) || isEmptyOrBlankArray(usersFile)) {
                Files.writeString(usersFile, GSON.toJson(defaultSeedUsers()), StandardCharsets.UTF_8);
                return;
            }
            String json = Files.readString(usersFile, StandardCharsets.UTF_8);
            List<User> users = GSON.fromJson(json, LIST_TYPE);
            if (users == null) {
                users = new ArrayList<>();
            }
            if (syncBundledDemoAccounts(users, defaultSeedUsers())) {
                Files.writeString(usersFile, GSON.toJson(users), StandardCharsets.UTF_8);
            }
        }
    }

    private static boolean isEmptyOrBlankArray(Path file) throws IOException {
        String s = Files.readString(file, StandardCharsets.UTF_8).trim();
        return s.isEmpty() || "[]".equals(s);
    }

    private static List<User> defaultSeedUsers() {
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
                    User merged = copyOf(d);
                    User cur = existing.get(index);
                    merged.setBanned(cur.getBanned());
                    merged.setBanReason(cur.getBanReason());
                    merged.setAppealMessage(cur.getAppealMessage());
                    merged.setAppealSubmittedAt(cur.getAppealSubmittedAt());
                    existing.set(index, merged);
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
        u.setBanned(from.getBanned() != null ? from.getBanned() : Boolean.FALSE);
        u.setBanReason(from.getBanReason());
        u.setAppealMessage(from.getAppealMessage());
        u.setAppealSubmittedAt(from.getAppealSubmittedAt());
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
        u.setBanned(Boolean.FALSE);
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
            u.setBanned(Boolean.FALSE);
            u.setCreatedAt(Instant.now().toString());
            users.add(u);
            saveAll(users);
        }
    }

    public Optional<User> findByUserId(String userId) throws IOException {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        for (User u : findAll()) {
            if (userId.equals(u.getUserId())) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    /**
     * Ban or unban. When {@code banned} is {@code true}, {@code banReasonWhenBanning} is stored (trimmed);
     * blank becomes a default placeholder. Clears any previous appeal. When unbanning, clears ban reason and appeal fields.
     */
    public boolean setBanned(String userId, boolean banned, String banReasonWhenBanning) throws IOException {
        synchronized (FILE_LOCK) {
            ensureStorage();
            List<User> users = readUsersList();
            boolean changed = false;
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                if (userId.equals(u.getUserId())) {
                    u.setBanned(banned);
                    if (banned) {
                        String reason = banReasonWhenBanning == null ? "" : banReasonWhenBanning.trim();
                        if (reason.isEmpty()) {
                            reason = "(No reason provided by administrator)";
                        }
                        u.setBanReason(reason);
                        u.setAppealMessage(null);
                        u.setAppealSubmittedAt(null);
                    } else {
                        u.setBanReason(null);
                        u.setAppealMessage(null);
                        u.setAppealSubmittedAt(null);
                    }
                    users.set(i, u);
                    changed = true;
                    break;
                }
            }
            if (changed) {
                Files.writeString(usersFile, GSON.toJson(users), StandardCharsets.UTF_8);
            }
            return changed;
        }
    }

    /** Saves appeal text for a banned user. Returns false if user not found or not banned. */
    public boolean submitAppeal(String userId, String appealText) throws IOException {
        if (appealText == null || appealText.isBlank()) {
            return false;
        }
        synchronized (FILE_LOCK) {
            ensureStorage();
            List<User> users = readUsersList();
            boolean changed = false;
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                if (userId.equals(u.getUserId())) {
                    if (!Boolean.TRUE.equals(u.getBanned())) {
                        return false;
                    }
                    u.setAppealMessage(appealText.trim());
                    u.setAppealSubmittedAt(Instant.now().toString());
                    users.set(i, u);
                    changed = true;
                    break;
                }
            }
            if (changed) {
                Files.writeString(usersFile, GSON.toJson(users), StandardCharsets.UTF_8);
            }
            return changed;
        }
    }

    public boolean resetPassword(String userId, String newPlainPassword) throws IOException {
        if (newPlainPassword == null || newPlainPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        synchronized (FILE_LOCK) {
            ensureStorage();
            List<User> users = readUsersList();
            boolean changed = false;
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                if (userId.equals(u.getUserId())) {
                    u.setPasswordHash(PasswordHash.hash(u.getUsername(), newPlainPassword));
                    users.set(i, u);
                    changed = true;
                    break;
                }
            }
            if (changed) {
                Files.writeString(usersFile, GSON.toJson(users), StandardCharsets.UTF_8);
            }
            return changed;
        }
    }

    private List<User> readUsersList() throws IOException {
        String json = Files.readString(usersFile, StandardCharsets.UTF_8);
        List<User> users = GSON.fromJson(json, LIST_TYPE);
        return users != null ? new ArrayList<>(users) : new ArrayList<>();
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
